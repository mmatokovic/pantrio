package dev.mmatokovic.pantrio.domain.storage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StorageRequest(
    @NotBlank
    @Size(max = 100)
    String name,
    @Size(max = 500)
    String description,
    Boolean disabled
) { }
