package dev.mmatokovic.pantrio.domain.storage;

public record StorageResponse(
    Long id,
    String name,
    String description,
    boolean disabled
) { }
