package dev.mmatokovic.pantrio.infra.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@Slf4j(topic = "error.logger")
@RestControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error(ex.getMessage(), ex);

        final var error = buildErrorResponse(ex, "Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);

        return ResponseEntity.internalServerError().body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        final var error = buildErrorResponse(ex, "Data integrity violation", HttpStatus.BAD_REQUEST);

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(NoSuchElementFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementFoundException(NoSuchElementFoundException ex) {
        final var error = buildErrorResponse(ex, ex.getMessage(), HttpStatus.NOT_FOUND);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        List<ErrorResponse.ErrorInfo> violations = ex.getConstraintViolations()
            .stream()
            .map(cv -> new ErrorResponse.ErrorInfo(cv.getPropertyPath().toString(), cv.getMessage()))
            .toList();

        final var error = buildErrorResponse(ex, "Validation failed", HttpStatus.UNPROCESSABLE_CONTENT, violations);

        return ResponseEntity.unprocessableContent().body(error);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        List<ErrorResponse.ErrorInfo> errors = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(fieldError -> new ErrorResponse.ErrorInfo(fieldError.getField(), fieldError.getDefaultMessage()))
            .toList();

        final var error = buildErrorResponse(ex, "Validation failed", HttpStatus.UNPROCESSABLE_CONTENT, errors);

        return ResponseEntity.unprocessableContent().body(error);
    }

    private ErrorResponse buildErrorResponse(Exception ex, String message, HttpStatusCode status) {
        return buildErrorResponse(ex, message, status, null);
    }

    @SuppressWarnings("unused")
    private ErrorResponse buildErrorResponse(Exception ex, String message, HttpStatusCode status, List<ErrorResponse.ErrorInfo> errors) {
        return new ErrorResponse(status.value(), message, errors);
    }

    @Override
    public ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex,
                                                          Object body,
                                                          @NonNull HttpHeaders headers,
                                                          @NonNull HttpStatusCode status,
                                                          @NonNull WebRequest request) {

        final var errorResponse = buildErrorResponse(ex, ex.getMessage(), status);

        return super.handleExceptionInternal(ex, errorResponse, headers, status, request);
    }
}
