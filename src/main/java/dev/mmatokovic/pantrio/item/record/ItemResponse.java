package dev.mmatokovic.pantrio.item.record;

public record ItemResponse(
        Long id,
        String name,
        String remarks,
        String pictureUrl,
        Boolean disabled
) { }
