package dev.mmatokovic.pantrio.infra.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    LocalDateTime timestamp,
    Integer status,
    String message,
    List<Errors> errors,
    String trace
) {
    public record Errors(
        String field,
        String detail
    ) { }

    public ErrorResponse(LocalDateTime timestamp, int status, String message) {
        this(timestamp, status, message, null, null);
    }

    public ErrorResponse {
        if (errors != null && errors.isEmpty()) {
            errors = null;
        }
    }

    public ErrorResponse addValidationError(String field, String message) {
        List<Errors> newErrors =
            errors == null
                ? new ArrayList<>()
                : new ArrayList<>(errors);

        newErrors.add(new Errors(field, message));

        return new ErrorResponse(
            this.timestamp,
            this.status,
            this.message,
            newErrors,
            this.trace
        );
    }

    public ErrorResponse withTrace(String trace) {
        return new ErrorResponse(
            this.timestamp,
            this.status,
            this.message,
            this.errors,
            trace
        );
    }
}
