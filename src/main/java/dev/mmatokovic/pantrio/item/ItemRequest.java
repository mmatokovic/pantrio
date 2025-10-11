package dev.mmatokovic.pantrio.item;

public record ItemRequest(
        String name,
        String remarks,
        String pictureUrl,
        String status,
        boolean disabled
) {}
