package dev.mmatokovic.pantrio.infra.error;

import dev.mmatokovic.pantrio.infra.exception.ConflictException;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorHandlerShould {

    @Test
    void handleNotFound_return404WithExceptionMessage() {
        var errorHandler = new ErrorHandler();

        var response = errorHandler.handleNotFound(new EntityNotFoundException("Item 1 not found"), new ServletWebRequest(new MockHttpServletRequest()));

        assertThat(response).isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, "Item 1 not found", null)));
    }

    @Test
    void handleNotFound_return404WithDefaultMessage_whenExceptionHasNoMessage() {
        var errorHandler = new ErrorHandler();

        var response = errorHandler.handleNotFound(new NoSuchElementFoundException(null), new ServletWebRequest(new MockHttpServletRequest()));

        assertThat(response).isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, "Resource not found", null)));
    }

    @Test
    void handleConflictException_return409WithExceptionMessage() {
        var errorHandler = new ErrorHandler();

        var response = errorHandler.handleConflictException(
            new ConflictException("Storage with name Fridge already exists"), new ServletWebRequest(new MockHttpServletRequest()));

        assertThat(response).isEqualTo(ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(409, "Storage with name Fridge already exists", null)));
    }

    @Test
    void handleDataIntegrityViolation_mapKnownConstraintToMessage() {
        var errorHandler = new ErrorHandler();
        var cause = new ConstraintViolationException("unique constraint violated", new SQLException("ORA-00001"), "PANTRIO.IDX_STORAGE_NAME");

        var response = errorHandler.handleDataIntegrityViolation(
            new DataIntegrityViolationException("could not execute statement", cause), new ServletWebRequest(new MockHttpServletRequest()));

        assertThat(response).isEqualTo(ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(409, "Storage with this name already exists", null)));
    }

    @Test
    void handleDataIntegrityViolation_returnGenericMessage_whenCauseIsNotAConstraintViolation() {
        var errorHandler = new ErrorHandler();

        var response = errorHandler.handleDataIntegrityViolation(
            new DataIntegrityViolationException("value too large"), new ServletWebRequest(new MockHttpServletRequest()));

        assertThat(response).isEqualTo(ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, "Data integrity violation", null)));
    }

    @Test
    void handleUpstreamServerError_return502() {
        var errorHandler = new ErrorHandler();

        var response = errorHandler.handleUpstreamServerError(
            new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR), new ServletWebRequest(new MockHttpServletRequest()));

        assertThat(response).isEqualTo(ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(new ErrorResponse(502, "External service returned an error", null)));
    }

    @Test
    void handleResourceAccessError_return503() {
        var errorHandler = new ErrorHandler();

        var response = errorHandler.handleResourceAccessError(
            new ResourceAccessException("Connection refused"), new ServletWebRequest(new MockHttpServletRequest()));

        assertThat(response).isEqualTo(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorResponse(503, "External service is unreachable", null)));
    }

    @Test
    void handleGlobalException_return500WithoutExposingMessage() {
        var errorHandler = new ErrorHandler();

        var response = errorHandler.handleGlobalException(new IllegalStateException("secret detail"), new ServletWebRequest(new MockHttpServletRequest()));

        assertThat(response).isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(500, "Something went wrong", null)));
    }

    @Test
    void handleExceptionInternal_mapSpringProblemDetailToErrorResponse() {
        var errorHandler = new ErrorHandler();
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, "Method 'PATCH' is not supported.");

        var response = errorHandler.handleExceptionInternal(new IllegalStateException("unsupported"), problemDetail, new HttpHeaders(),
            HttpStatus.METHOD_NOT_ALLOWED, new ServletWebRequest(new MockHttpServletRequest()));

        assertThat(response).isEqualTo(ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(new ErrorResponse(405, "Method 'PATCH' is not supported.", null)));
    }

    @Test
    void handleExceptionInternal_useExceptionReason_whenProblemDetailHasNoDetail() {
        var errorHandler = new ErrorHandler();

        var response = errorHandler.handleExceptionInternal(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid API version: '2.0.0'."),
            ProblemDetail.forStatus(HttpStatus.BAD_REQUEST), new HttpHeaders(), HttpStatus.BAD_REQUEST, new ServletWebRequest(new MockHttpServletRequest()));

        assertThat(response).isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(400, "Invalid API version: '2.0.0'.", null)));
    }

    @Test
    void handleExceptionInternal_returnGenericErrorResponse_whenBodyIsMissing() {
        var errorHandler = new ErrorHandler();

        var response = errorHandler.handleExceptionInternal(new IllegalStateException("unsupported"), null, new HttpHeaders(),
            HttpStatus.BAD_REQUEST, new ServletWebRequest(new MockHttpServletRequest()));

        assertThat(response).isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(400, "An unexpected error occurred", null)));
    }
}
