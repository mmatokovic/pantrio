package dev.mmatokovic.pantrio.domain.product;

public interface ProductRepository {

    Product getProductByBarcode(String code);
}
