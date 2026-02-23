package dev.mmatokovic.pantrio.infra.error;

import dev.mmatokovic.pantrio.domain.product.ProductNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.UnknownHttpStatusCodeException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

@RestControllerAdvice
public class ErrorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("error.logger");

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        LOGGER.error(ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse("Something went wrong");

        return ResponseEntity.internalServerError().body(error);
    }

    @ExceptionHandler({NoHandlerFoundException.class, EntityNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex) {
        ErrorResponse error = new ErrorResponse(404, ex.getMessage() != null ? ex.getMessage() : "Resource not found");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        LOGGER.error(ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(422, "Data integrity violation");

        return ResponseEntity.unprocessableContent().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintValidationExceptions(ConstraintViolationException ex) {
        List<ErrorResponse.Errors> violations = ex.getConstraintViolations()
            .stream()
            .map(cv -> new ErrorResponse.Errors(cv.getPropertyPath().toString(), cv.getMessage()))
            .toList();

        ErrorResponse error = new ErrorResponse(400, "Validation Failed", violations);

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ErrorResponse.Errors> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> new ErrorResponse.Errors(err.getField(), err.getDefaultMessage()))
            .toList();

        ErrorResponse error = new ErrorResponse(400, "Validation Failed", errors);

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        ErrorResponse.Errors fieldError = new ErrorResponse.Errors(ex.getParameterName(), "Parameter is required");
        ErrorResponse error = new ErrorResponse(400, "Missing required parameter", List.of(fieldError));

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ErrorResponse error = new ErrorResponse(400, "Malformed JSON: " + ex.getMostSpecificCause().getMessage());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ErrorResponse error = new ErrorResponse(405, "HTTP method not allowed: " + ex.getMethod());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleClientError(HttpClientErrorException ex) {
        int status = ex.getStatusCode().value();
        String message = ex.getResponseBodyAsString();

        return ResponseEntity.status(status).body(new ErrorResponse(status, message));
    }

    @ExceptionHandler({UnknownHttpStatusCodeException.class, HttpServerErrorException.class})
    public ResponseEntity<ErrorResponse> handleUnknownStatus() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(502, "Unexpected external service error"));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleExternalNotFound(Exception ex) {
        ErrorResponse error = new ErrorResponse(404, ex.getMessage() != null ? ex.getMessage() : "Resource not found");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
