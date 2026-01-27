package dev.mmatokovic.pantrio.domain.item;

public record ItemResponse(
        Long id,
        String name,
        String remarks,
        String pictureUrl,
        Boolean disabled
) { }
