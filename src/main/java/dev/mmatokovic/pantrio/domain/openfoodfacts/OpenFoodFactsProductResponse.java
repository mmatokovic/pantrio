package dev.mmatokovic.pantrio.domain.openfoodfacts;

public record OpenFoodFactsProductResponse(
    String status,
    String status_verbose,
    Product product
) {
   public record Product(
       String code,
       String product_name,
       String brands,
       String quantity,
       String image_url
   ) {}
}
