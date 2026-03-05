package dev.mmatokovic.pantrio.domain.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.mmatokovic.pantrio.domain.item.ItemResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StorageItemResponse(
    Long id,
    Long quantity,
    @JsonProperty("expiry_date")
    LocalDate expiryDate,
    ItemResponse item,
    StorageResponse storage,
    @JsonProperty("created_at")
    LocalDateTime createdAt,
    @JsonProperty("updated_at")
    LocalDateTime updatedAt
) { }
