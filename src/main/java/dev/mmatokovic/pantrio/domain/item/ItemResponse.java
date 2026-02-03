package dev.mmatokovic.pantrio.domain.item;

public record ItemResponse(
        Long id,
        String name,
        String remarks,
        String barcode,
        String unit,
        String pictureUrl,
        Boolean disabled
) { }
