package dev.mmatokovic.pantrio.domain.product;

public interface OpenFoodFactsPort {

    /**
     * Retrieve information for a product with a specific barcode.
     *
     * @param code product barcode
     * @return product details
     */
    OpenFoodFactsProductResponse getProductByBarcode(String code);
}
