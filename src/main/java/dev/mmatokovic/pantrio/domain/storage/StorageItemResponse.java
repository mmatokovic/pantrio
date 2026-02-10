package dev.mmatokovic.pantrio.domain.storage;

import dev.mmatokovic.pantrio.domain.item.ItemResponse;

public record StorageItemResponse(
    ItemResponse item,
    Long quantity,
    Storage storage
) {
    public record Storage(
        Long id,
        String name) { }
}
