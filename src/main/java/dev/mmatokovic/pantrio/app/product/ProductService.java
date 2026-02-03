package dev.mmatokovic.pantrio.app.product;

import dev.mmatokovic.pantrio.domain.product.ProductResponse;

public interface ProductService {

    /**
     * Retrieves a single product by its barcode.
     *
     * @param barcode the barcode of the item
     * @return the product associated with the given barcode
     */
    ProductResponse findProductByBarcode(String barcode);
}
