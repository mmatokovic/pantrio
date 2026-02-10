package dev.mmatokovic.pantrio.domain.product;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenFoodFactsProductResponse(
    String code,
    Product product,
    String status,
    @JsonProperty("status_verbose")
    String statusVerbose
) {
    public record Product(
       @JsonProperty("product_name")
       String productName,
       @JsonProperty("product_quantity")
       String quantity,
       @JsonProperty("product_quantity_unit")
       String unit,
       @JsonProperty("image_url")
       String imageUrl
   ) { }
}
