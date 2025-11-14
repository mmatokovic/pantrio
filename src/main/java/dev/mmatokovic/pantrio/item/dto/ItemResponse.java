package dev.mmatokovic.pantrio.item.dto;

public record ItemResponse(
        Long id,
        String name,
        String remarks,
        String pictureUrl,
        Boolean disabled
) { }
