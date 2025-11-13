package dev.mmatokovic.pantrio.item.record;

public record ItemRequest(
        String name,
        String remarks,
        String pictureUrl,
        boolean disabled
) { }
