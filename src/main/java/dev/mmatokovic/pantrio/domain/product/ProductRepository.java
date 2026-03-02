package dev.mmatokovic.pantrio.domain.product;

public interface ProductRepository {

    /**
     * Retrieve information for a product with a specific barcode.
     *
     * @param code product barcode
     * @return {@link Product} details
     */
    Product getProductByBarcode(String code);
}
