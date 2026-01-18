package dev.mmatokovic.pantrio.storage.dto;

public record StorageResponse(
    Long id,
    String name,
    String description,
    boolean disabled
) { }
