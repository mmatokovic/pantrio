package dev.mmatokovic.pantrio.item.dto;

public record ItemRequest(
        String name,
        String remarks,
        String pictureUrl,
        Boolean disabled
) { }
