package dev.mmatokovic.pantrio.app.product;

import dev.mmatokovic.pantrio.domain.product.OpenFoodFactsPort;
import dev.mmatokovic.pantrio.domain.product.OpenFoodFactsProductResponse;
import dev.mmatokovic.pantrio.domain.product.ProductResponse;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {
    private final OpenFoodFactsPort port;
    private final ProductMapper mapper;

    public ProductServiceImpl(OpenFoodFactsPort port, ProductMapper mapper) {
        this.port = port;
        this.mapper = mapper;
    }

    @Override
    public ProductResponse findProductByBarcode(String barcode) {
        OpenFoodFactsProductResponse response = port.getProductByBarcode(barcode);

        return mapper.toResponse(response);
    }
}
