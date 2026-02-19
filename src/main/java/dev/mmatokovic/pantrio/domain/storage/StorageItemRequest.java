package dev.mmatokovic.pantrio.domain.storage;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StorageItemRequest(
    @NotNull
    Long itemId,
    @NotNull
    @Min(1)
    Long quantity,
    LocalDate expiryDate
) { }
