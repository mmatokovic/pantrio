package dev.mmatokovic.pantrio.infra.error;

import dev.mmatokovic.pantrio.infra.exception.ConflictException;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    private static final Map<String, String> CONSTRAINT_MESSAGES = Map.of(
        "pantrio.idx_items_barcode", "Item with same barcode already exists",
        "pantrio.idx_storage_name", "Storage with this name already exists",
        "pantrio.fk_item_instance_storage", "Referenced storage does not exist",
        "pantrio.fk_item_instance_item", "Referenced item does not exist",
        "pantrio.uk_item_instance_storage_item", "Item instance with same storage and item already exists"
    );

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        return respond(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        var message = "Data integrity violation";
        if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException cve && cve.getConstraintName() != null) {
            message = CONSTRAINT_MESSAGES.getOrDefault(cve.getConstraintName().toLowerCase(Locale.ROOT), constraintFallback(cve.getKind()));
        }
        return respond(ex, HttpStatus.CONFLICT, message, request);
    }

    @ExceptionHandler({NoSuchElementFoundException.class, EntityNotFoundException.class})
    ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {
        return respond(ex, HttpStatus.NOT_FOUND, ex.getMessage() != null ? ex.getMessage() : "Resource not found", request);
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<Object> handleConflictException(ConflictException ex, WebRequest request) {
        return respond(ex, HttpStatus.CONFLICT, ex.getMessage() != null ? ex.getMessage() : "Duplicate resource", request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        var errors = ex.getConstraintViolations().stream()
            .map(violation -> new ErrorResponse.ErrorInfo(violation.getPropertyPath().toString(), violation.getMessage()))
            .toList();
        var status = HttpStatus.UNPROCESSABLE_CONTENT;
        return handleExceptionInternal(ex, new ErrorResponse(status.value(), "Validation failed", errors), new HttpHeaders(), status, request);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    ResponseEntity<Object> handleUpstreamServerError(HttpServerErrorException ex, WebRequest request) {
        return respond(ex, HttpStatus.BAD_GATEWAY, "External service returned an error", request);
    }

    @ExceptionHandler(ResourceAccessException.class)
    ResponseEntity<Object> handleResourceAccessError(ResourceAccessException ex, WebRequest request) {
        return respond(ex, HttpStatus.SERVICE_UNAVAILABLE, "External service is unreachable", request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                            @NonNull HttpHeaders headers,
                                                                            @NonNull HttpStatusCode status,
                                                                            @NonNull WebRequest request) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> new ErrorResponse.ErrorInfo(fieldError.getField(), fieldError.getDefaultMessage()))
            .toList();
        return handleExceptionInternal(ex, new ErrorResponse(status.value(), "Validation failed", errors), headers, status, request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMissingServletRequestParameter(@NonNull MissingServletRequestParameterException ex,
                                                                                    @NonNull HttpHeaders headers,
                                                                                    @NonNull HttpStatusCode status,
                                                                                    @NonNull WebRequest request) {
        var errors = List.of(new ErrorResponse.ErrorInfo(ex.getParameterName(), "Parameter is required"));
        return handleExceptionInternal(ex, new ErrorResponse(status.value(), "Missing request parameter", errors), headers, status, request);
    }

    @Override
    public ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex,
                                                          @Nullable Object body,
                                                          @NonNull HttpHeaders headers,
                                                          @NonNull HttpStatusCode status,
                                                          @NonNull WebRequest request) {
        (status.is5xxServerError() ? log.atError() : log.atDebug())
            .setCause(ex)
            .addKeyValue("status_code", status.value())
            .addKeyValue("error.type", ex.getClass().getName())
            .log("Request failed: {}", request.getDescription(false));

        Object response = switch (body) {
            case ErrorResponse errorResponse -> errorResponse;
            case ProblemDetail problemDetail when problemDetail.getDetail() != null ->
                new ErrorResponse(status.value(), problemDetail.getDetail(), null);
            case null, default -> new ErrorResponse(status.value(), "An unexpected error occurred", null);
        };
        return super.handleExceptionInternal(ex, response, headers, status, request);
    }

    private ResponseEntity<Object> respond(Exception ex, HttpStatus status, String message, WebRequest request) {
        return handleExceptionInternal(ex, new ErrorResponse(status.value(), message, null), new HttpHeaders(), status, request);
    }

    private static String constraintFallback(@Nullable ConstraintKind kind) {
        return kind == null ? "Data constraint violation" : "Data constraint violation " + kind.name().toLowerCase(Locale.ROOT);
    }
}
