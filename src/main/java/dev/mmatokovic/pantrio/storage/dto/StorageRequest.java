package dev.mmatokovic.pantrio.storage.dto;

public record StorageRequest(
    String name,
    String description,
    boolean disabled
) { }
