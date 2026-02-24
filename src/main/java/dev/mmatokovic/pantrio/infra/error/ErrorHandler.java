package dev.mmatokovic.pantrio.infra.error;

import dev.mmatokovic.pantrio.app.product.ProductNotFoundException;
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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.UnknownHttpStatusCodeException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class ErrorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("error.logger");

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        LOGGER.error(ex.getMessage(), ex);

        return ResponseEntity.internalServerError()
            .body(new ErrorResponse("Something went wrong"));
    }

    @ExceptionHandler({NoHandlerFoundException.class, EntityNotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, ex.getMessage() != null ? ex.getMessage() : "Resource not found"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        LOGGER.error(ex.getMessage(), ex);

        return ResponseEntity.unprocessableContent()
            .body(new ErrorResponse(422, "Data integrity violation"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintValidationExceptions(ConstraintViolationException ex) {
        List<ErrorResponse.Errors> violations = ex.getConstraintViolations()
            .stream()
            .map(cv -> new ErrorResponse.Errors(cv.getPropertyPath().toString(), cv.getMessage()))
            .toList();

        return ResponseEntity.badRequest()
            .body(new ErrorResponse(400, "Validation Failed", violations));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ErrorResponse.Errors> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> new ErrorResponse.Errors(err.getField(), err.getDefaultMessage()))
            .toList();

        return ResponseEntity.badRequest()
            .body(new ErrorResponse(400, "Validation Failed", errors));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        ErrorResponse.Errors fieldError = new ErrorResponse.Errors(ex.getParameterName(), "Parameter is required");

        return ResponseEntity.badRequest()
            .body(new ErrorResponse(400, "Missing required parameter", List.of(fieldError)));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(400, "Malformed JSON: " + ex.getMostSpecificCause().getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(new ErrorResponse(405, "HTTP method not allowed: " + ex.getMethod()));
    }

    @ExceptionHandler({UnknownHttpStatusCodeException.class, HttpServerErrorException.class})
    public ResponseEntity<ErrorResponse> handleExternalUnknownStatus() {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(new ErrorResponse(502, "Unexpected external service error"));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleExternalNotFound(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, ex.getMessage() != null ? ex.getMessage() : "Resource not found"));
    }
}
