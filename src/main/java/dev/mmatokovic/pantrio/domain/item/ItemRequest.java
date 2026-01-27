package dev.mmatokovic.pantrio.domain.item;

public record ItemRequest(
        String name,
        String remarks,
        String pictureUrl,
        Boolean disabled
) { }
