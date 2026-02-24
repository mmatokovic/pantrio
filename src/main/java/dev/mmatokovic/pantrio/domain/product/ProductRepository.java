package dev.mmatokovic.pantrio.domain.product;

import java.util.Optional;

public interface ProductRepository {

    /**
     * Retrieve information for a product with a specific barcode.
     *
     * @param code product barcode
     * @return {@link Product} details
     */
    Optional<Product> getProductByBarcode(String code);
}
