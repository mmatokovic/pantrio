package dev.mmatokovic.pantrio.domain.openfoodfacts;

public interface OpenFoodFactsRepo {

    /**
     * Retrieve information for a product with a specific barcode.
     */
    OpenFoodFactsProductResponse indexProductByBarcode(String code);
}
