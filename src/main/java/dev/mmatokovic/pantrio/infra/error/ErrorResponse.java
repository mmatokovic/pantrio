package dev.mmatokovic.pantrio.infra.error;

import java.util.List;

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
