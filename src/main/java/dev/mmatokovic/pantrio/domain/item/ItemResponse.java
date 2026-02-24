package dev.mmatokovic.pantrio.domain.item;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ItemResponse(
        Long id,
        String name,
        String remarks,
        String barcode,
        String unit,
        @JsonProperty("picture_url")
        String pictureUrl,
        Boolean disabled
) { }
