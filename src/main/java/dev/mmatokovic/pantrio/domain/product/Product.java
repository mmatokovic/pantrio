package dev.mmatokovic.pantrio.domain.product;

public record Product(
    String code,
    String productName,
    Long quantity,
    String unit,
    String imageUrl
) { }
