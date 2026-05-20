package dev.mmatokovic.pantrio.app.product;

import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import dev.mmatokovic.pantrio.domain.product.ProductResponse;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repo;
    private final ProductMapper mapper;

    @Override
    public ProductResponse findProductByBarcode(String barcode) {
        Product product = repo.getProductByBarcode(barcode);
        if (product == null) {
            throw new NoSuchElementFoundException("Product not found for barcode: " + barcode);
        }
        return mapper.toResponse(product);
    }
}
