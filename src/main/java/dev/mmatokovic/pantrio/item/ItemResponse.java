package dev.mmatokovic.pantrio.item;

public record ItemResponse(
        Long id,
        String name,
        String remarks,
        String pictureUrl,
        String status,
        Boolean disabled
) {}