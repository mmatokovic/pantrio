package dev.mmatokovic.pantrio.domain.storage;

import dev.mmatokovic.pantrio.domain.item.ItemResponse;

import java.time.LocalDate;

public record StorageItemResponse(
    Long id,
    Long quantity,
    LocalDate expiryDate,
    ItemResponse item,
    Storage storage
) {
    public record Storage(
        Long id,
        String name) { }
}
