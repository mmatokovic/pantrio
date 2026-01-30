package dev.mmatokovic.pantrio.infra.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    Integer status;
    String message;
    private List<Errors> errors;

    public record Errors(String field, String detail) {

    }

    public ErrorResponse(String message) {
        this.message = message;
    }

    public ErrorResponse(int code, String message) {
        this.status = code;
        this.message = message;
    }

    public ErrorResponse(int code, String message, List<Errors> errors) {
        this.status = code;
        this.message = message;
        this.errors = errors != null ? errors : Collections.emptyList();
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<Errors> getErrors() {
        return errors;
    }
}
