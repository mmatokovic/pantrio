package dev.mmatokovic.pantrio.infra.error;

import org.jspecify.annotations.Nullable;

import java.util.List;

public record ErrorResponse(
    Integer status,
    String message,
    @Nullable List<ErrorInfo> errors
) {

    public record ErrorInfo(
        String field,
        @Nullable String detail
    ) { }
}
