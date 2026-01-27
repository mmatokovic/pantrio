package dev.mmatokovic.pantrio.domain.openfoodfacts;

import java.util.Optional;

public interface OpenFoodFactsRepo {

    /**
     * Retrieve information for a product with a specific barcode.
     */
    OpenFoodFactsProductResponse indexProductByBarcode(String code);
}
