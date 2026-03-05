package dev.mmatokovic.pantrio.domain.item;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record ItemResponse(
        Long id,
        String name,
        String remarks,
        String barcode,
        String unit,
        @JsonProperty("picture_url")
        String pictureUrl,
        Boolean disabled,
        @JsonProperty("created_at")
        LocalDateTime createdAt,
        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) { }
