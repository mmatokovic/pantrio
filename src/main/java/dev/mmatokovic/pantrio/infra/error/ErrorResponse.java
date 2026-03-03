package dev.mmatokovic.pantrio.infra.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    Integer status,
    String message,
    List<ErrorInfo> errors
) {
    public record ErrorInfo(
        String field,
        String detail
    ) { }
}
