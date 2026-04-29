package dev.mmatokovic.pantrio.infra.logging;

import java.time.Duration;
import java.time.Instant;

public record LogContext(
    String requestId,
    String method,
    String uri,
    String source,
    Instant startTime
) {
    long elapsedMs() {
        return Duration.between(startTime, Instant.now()).toMillis();
    }
}
