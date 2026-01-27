package dev.mmatokovic.pantrio.domain.storage;

public record StorageRequest(
    String name,
    String description,
    Boolean disabled
) { }
