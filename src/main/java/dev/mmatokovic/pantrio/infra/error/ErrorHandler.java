package dev.mmatokovic.pantrio.infra.error;

import dev.mmatokovic.pantrio.infra.exception.ConflictException;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Gracefully handles exception occurred during REST API call execution
 */
@Slf4j(topic = "error.logger")
@RestControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    private ErrorResponse buildErrorResponse(Exception ex, String message, HttpStatusCode status) {
        return buildErrorResponse(ex, message, status, null);
    }

    private ErrorResponse buildErrorResponse(Exception ex, String message, HttpStatusCode status, List<ErrorResponse.ErrorInfo> errors) {
        if (status.is5xxServerError()) {
            log.error(ex.getMessage(), ex);
        }
        return new ErrorResponse(status.value(), message, errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        final var error = buildErrorResponse(ex, "Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        return ResponseEntity.internalServerError().body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Data integrity violation";
        if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException cve) {
            String constraint = cve.getConstraintName();
            if (constraint != null) {
                message = switch (constraint.toLowerCase()) {
                    case "pantrio.idx_items_barcode" -> "Item with same barcode already exists";
                    case "pantrio.idx_storage_name" -> "Storage with this name already exists";
                    default -> "Data integrity violation";
                };
            }
        }

        final var error = buildErrorResponse(ex, message, HttpStatus.CONFLICT);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler({NoSuchElementFoundException.class, EntityNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex) {
        final var error = buildErrorResponse(ex, ex.getMessage() != null ? ex.getMessage() : "Resource not found", HttpStatus.NOT_FOUND);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex) {
        final var error = buildErrorResponse(ex, ex.getMessage() != null ? ex.getMessage() : "Duplicate resource", HttpStatus.CONFLICT);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
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

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleUpstreamServerError(HttpServerErrorException ex) {
        final var error = buildErrorResponse(ex, "External service returned an error", HttpStatus.BAD_GATEWAY);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleResourceAccessError(ResourceAccessException ex) {
        final var error = buildErrorResponse(ex, "External service is unreachable", HttpStatus.SERVICE_UNAVAILABLE);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpRequestMethodNotSupported(@NonNull HttpRequestMethodNotSupportedException ex,
                                                                                   @NonNull HttpHeaders headers,
                                                                                   @NonNull HttpStatusCode status,
                                                                                   @NonNull WebRequest request) {
        final var error = buildErrorResponse(ex, "HTTP method not allowed: " + ex.getMethod(), status);
        return handleExceptionInternal(ex, error, headers, status, request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMissingServletRequestParameter(@NonNull MissingServletRequestParameterException ex,
                                                                                    @NonNull HttpHeaders headers,
                                                                                    @NonNull HttpStatusCode status,
                                                                                    @NonNull WebRequest request) {
        List<ErrorResponse.ErrorInfo> errors = List.of(
            new ErrorResponse.ErrorInfo(ex.getParameterName(), "Parameter is required")
        );

        final var error = buildErrorResponse(ex, "Missing request parameter", status, errors);
        return handleExceptionInternal(ex, error, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        List<ErrorResponse.ErrorInfo> errors = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(fieldError -> new ErrorResponse.ErrorInfo(fieldError.getField(), fieldError.getDefaultMessage()))
            .toList();

        final var error = buildErrorResponse(ex, "Validation failed", status, errors);
        return handleExceptionInternal(ex, error, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(@NonNull NoHandlerFoundException ex,
                                                                   @NonNull HttpHeaders headers,
                                                                   @NonNull HttpStatusCode status,
                                                                   @NonNull WebRequest request) {
        final var error = buildErrorResponse(ex, ex.getMessage() != null ? ex.getMessage() : "Resource not found", status);
        return handleExceptionInternal(ex, error, headers, status, request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleNoResourceFoundException(@NonNull NoResourceFoundException ex,
                                                                              @NonNull HttpHeaders headers,
                                                                              @NonNull HttpStatusCode status,
                                                                              @NonNull WebRequest request) {
        final var error = buildErrorResponse(ex, "Not Found", status);
        return handleExceptionInternal(ex, error, headers, status, request);
    }

    @Override
    public ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex,
                                                          Object body,
                                                          @NonNull HttpHeaders headers,
                                                          @NonNull HttpStatusCode status,
                                                          @NonNull WebRequest request) {
        if (!(body instanceof ErrorResponse)) {
            body = buildErrorResponse(ex, "An unexpected error occurred", status);
        }

        return super.handleExceptionInternal(ex, body, headers, status, request);
    }
}
