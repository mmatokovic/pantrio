package dev.mmatokovic.pantrio.domain.storage;

import dev.mmatokovic.pantrio.domain.item.ItemResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StorageItemResponse(
    Long id,
    Long quantity,
    LocalDate expiryDate,
    ItemResponse item,
    StorageResponse storage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) { }
