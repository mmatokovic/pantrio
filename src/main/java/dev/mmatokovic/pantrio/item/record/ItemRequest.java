package dev.mmatokovic.pantrio.item.record;

public record ItemRequest(
        String name,
        String remarks,
        String pictureUrl,
        String status,
        boolean disabled
) { }
