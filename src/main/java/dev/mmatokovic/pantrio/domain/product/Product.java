package dev.mmatokovic.pantrio.domain.product;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Product(
    String code,
    @JsonProperty("product_name")
    String productName,
    @JsonProperty("product_quantity")
    String quantity,
    @JsonProperty("product_quantity_unit")
    String unit,
    @JsonProperty("image_url")
    String imageUrl
) { }
