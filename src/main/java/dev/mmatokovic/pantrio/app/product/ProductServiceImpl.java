package dev.mmatokovic.pantrio.app.product;

import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductCatalogPort;
import dev.mmatokovic.pantrio.domain.product.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductCatalogPort port;
    private final ProductMapper mapper;

    @Override
    public ProductResponse findProductByBarcode(String barcode) {
        Product response = port.getProductByBarcode(barcode);

        return mapper.toResponse(response);
    }
}
