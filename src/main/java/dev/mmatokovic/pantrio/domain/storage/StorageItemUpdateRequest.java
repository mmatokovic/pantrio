package dev.mmatokovic.pantrio.domain.storage;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StorageItemUpdateRequest(
    @NotNull @Min(1)
    Integer quantity,
    LocalDate expiryDate
) {}
