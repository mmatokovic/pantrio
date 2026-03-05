package dev.mmatokovic.pantrio.domain.storage;

import java.time.LocalDateTime;

public record StorageResponse(
    Long id,
    String name,
    String description,
    boolean disabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) { }
