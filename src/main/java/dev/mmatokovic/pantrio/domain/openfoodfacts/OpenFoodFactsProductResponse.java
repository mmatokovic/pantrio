package dev.mmatokovic.pantrio.domain.openfoodfacts;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenFoodFactsProductResponse(
    String status,
    @JsonProperty("status_verbose")
    String statusVerbose,
    Product product
) {
   public record Product(
       @JsonProperty("product_name")
       String productName,
       String brands,
       String quantity,
       @JsonProperty("image_url")
       String imageUrl
   ) { }
}
