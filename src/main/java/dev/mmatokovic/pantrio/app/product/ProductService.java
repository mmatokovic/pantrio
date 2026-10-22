package dev.mmatokovic.pantrio.app.product;

import dev.mmatokovic.pantrio.domain.product.ProductResponse;

public interface ProductService {

    ProductResponse findProductByBarcode(String barcode);
}
