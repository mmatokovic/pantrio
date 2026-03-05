package dev.mmatokovic.pantrio.domain.product;

public record ProductResponse(
    String name,
    String barcode,
    Long quantity,
    String unit,
    String pictureUrl
) { }
