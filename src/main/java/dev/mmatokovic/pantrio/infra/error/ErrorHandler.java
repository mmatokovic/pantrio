package dev.mmatokovic.pantrio.infra.error;

import dev.mmatokovic.pantrio.infra.exception.ConflictException;
import dev.mmatokovic.pantrio.infra.exception.InvalidRequestException;
import dev.mmatokovic.pantrio.infra.exception.JobManagementException;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.resilience.InvocationRejectedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    private static final int ORA_CHILD_RECORD_FOUND = 2292;

    private static final Map<String, String> CONSTRAINT_MESSAGES = Map.of(
        "idx_items_barcode", "Item with same barcode already exists",
        "idx_storage_name", "Storage with this name already exists",
        "fk_item_instance_storage", "Referenced storage does not exist",
        "fk_item_instance_item", "Referenced item does not exist",
        "uk_item_instance_storage_item", "Item instance with same storage and item already exists"
    );

    private static final Map<String, String> CHILD_RECORD_MESSAGES = Map.of(
        "fk_item_instance_storage", "Storage still holds stock",
        "fk_item_instance_item", "Item is still stocked in a storage"
    );

    @ExceptionHandler(Exception.class)
    @Nullable ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        return respond(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @Nullable ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        var message = "Data integrity violation";
        if (ex.getCause() instanceof ConstraintViolationException cve && cve.getConstraintName() != null) {
            var constraintName = withoutSchema(cve.getConstraintName());
            var messages = cve.getErrorCode() == ORA_CHILD_RECORD_FOUND ? CHILD_RECORD_MESSAGES : CONSTRAINT_MESSAGES;
            message = messages.getOrDefault(constraintName, constraintFallback(cve.getKind()));
        }
        return respond(ex, HttpStatus.CONFLICT, message, request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    @Nullable ResponseEntity<Object> handleOptimisticLockingFailure(OptimisticLockingFailureException ex, WebRequest request) {
        return respond(ex, HttpStatus.CONFLICT, "Resource was modified concurrently, reload it and try again", request);
    }

    @ExceptionHandler({NoSuchElementFoundException.class, EntityNotFoundException.class})
    @Nullable ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {
        return respond(ex, HttpStatus.NOT_FOUND, Objects.requireNonNullElse(ex.getMessage(), "Resource not found"), request);
    }

    @ExceptionHandler(InvalidRequestException.class)
    @Nullable ResponseEntity<Object> handleInvalidRequest(InvalidRequestException ex, WebRequest request) {
        return respond(ex, HttpStatus.BAD_REQUEST, Objects.requireNonNullElse(ex.getMessage(), "Invalid request"), request);
    }

    @ExceptionHandler(JobManagementException.class)
    @Nullable ResponseEntity<Object> handleJobManagement(JobManagementException ex, WebRequest request) {
        return respond(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Job management failed", request);
    }

    @ExceptionHandler(ConflictException.class)
    @Nullable ResponseEntity<Object> handleConflictException(ConflictException ex, WebRequest request) {
        return respond(ex, HttpStatus.CONFLICT, Objects.requireNonNullElse(ex.getMessage(), "Duplicate resource"), request);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    @Nullable ResponseEntity<Object> handleUpstreamServerError(HttpServerErrorException ex, WebRequest request) {
        return respond(ex, HttpStatus.BAD_GATEWAY, "External service returned an error", request);
    }

    @ExceptionHandler(InvocationRejectedException.class)
    @Nullable ResponseEntity<Object> handleInvocationRejected(InvocationRejectedException ex, WebRequest request) {
        return respond(ex, HttpStatus.SERVICE_UNAVAILABLE, "External service is busy, try again later", request);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    @Nullable ResponseEntity<Object> handlePropertyReference(PropertyReferenceException ex, WebRequest request) {
        return respond(ex, HttpStatus.BAD_REQUEST, "Unknown sort property: " + ex.getPropertyName(), request);
    }

    @ExceptionHandler(ResourceAccessException.class)
    @Nullable ResponseEntity<Object> handleResourceAccessError(ResourceAccessException ex, WebRequest request) {
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
    protected @Nullable ResponseEntity<Object> handleHandlerMethodValidationException(@NonNull HandlerMethodValidationException ex,
                                                                                      @NonNull HttpHeaders headers,
                                                                                      @NonNull HttpStatusCode status,
                                                                                      @NonNull WebRequest request) {
        var errors = ex.getParameterValidationResults().stream()
            .flatMap(result -> result.getResolvableErrors().stream()
                .map(error -> new ErrorResponse.ErrorInfo(Objects.requireNonNullElse(result.getMethodParameter().getParameterName(), "parameter"), error.getDefaultMessage())))
            .toList();
        var unprocessable = HttpStatus.UNPROCESSABLE_CONTENT;
        return handleExceptionInternal(ex, new ErrorResponse(unprocessable.value(), "Validation failed", errors), headers, unprocessable, request);
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
    public @Nullable ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex,
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
            case ProblemDetail problemDetail ->
                new ErrorResponse(status.value(), Objects.requireNonNullElse(problemDetail.getDetail(), fallbackMessage(ex)), null);
            case null, default -> new ErrorResponse(status.value(), fallbackMessage(ex), null);
        };
        return super.handleExceptionInternal(ex, response, headers, status, request);
    }

    private @Nullable ResponseEntity<Object> respond(Exception ex, HttpStatus status, String message, WebRequest request) {
        return handleExceptionInternal(ex, new ErrorResponse(status.value(), message, null), new HttpHeaders(), status, request);
    }

    private static String fallbackMessage(Exception ex) {
        var reason = ex instanceof ResponseStatusException responseStatusException ? responseStatusException.getReason() : null;
        return Objects.requireNonNullElse(reason, "An unexpected error occurred");
    }

    private static String withoutSchema(String constraintName) {
        return constraintName.substring(constraintName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private static String constraintFallback(@Nullable ConstraintKind kind) {
        return kind == null ? "Data constraint violation" : "Data constraint violation " + kind.name().toLowerCase(Locale.ROOT);
    }
}
