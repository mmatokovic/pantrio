package dev.mmatokovic.pantrio.domain.product;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductResponse(
    String name,
    String barcode,
    Long quantity,
    String unit,
    @JsonProperty("picture_url")
    String pictureUrl
) { }
