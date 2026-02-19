package dev.mmatokovic.pantrio.domain.storage;

import java.time.LocalDate;

public record StorageItemResponse(
    Long id,
    Long itemId,
    Long storageId,
    Long quantity,
    LocalDate expiryDate
) { }
