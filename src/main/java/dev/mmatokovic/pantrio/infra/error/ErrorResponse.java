package dev.mmatokovic.pantrio.infra.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    Integer status,
    String message,
    List<Errors> errors
) {
    public record Errors(
        String field,
        String detail) { }

    public ErrorResponse(String message) {
        this(null, message, null);
    }

    public ErrorResponse(int status, String message) {
        this(status, message, null);
    }

    public ErrorResponse {
        if (errors != null && errors.isEmpty()) {
            errors = null;
        }
    }
}
