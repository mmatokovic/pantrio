package dev.mmatokovic.pantrio.domain.storage;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record StorageResponse(
    Long id,
    String name,
    String description,
    boolean disabled,
    @JsonProperty("created_at")
    LocalDateTime createdAt,
    @JsonProperty("updated_at")
    LocalDateTime updatedAt
) { }
