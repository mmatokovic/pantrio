package dev.mmatokovic.pantrio.domain.item;

import java.time.LocalDateTime;

public record ItemResponse(
    Long id,
    String name,
    String remarks,
    String barcode,
    String unit,
    String pictureUrl,
    Boolean disabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) { }
