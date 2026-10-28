package dev.mmatokovic.pantrio.app.product;

import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import dev.mmatokovic.pantrio.domain.product.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse findProductByBarcode(String barcode) {
        return productMapper.toResponse(productRepository.getProductByBarcode(barcode));
    }
}
