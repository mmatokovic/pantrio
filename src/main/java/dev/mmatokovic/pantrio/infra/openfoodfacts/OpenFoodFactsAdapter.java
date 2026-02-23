package dev.mmatokovic.pantrio.infra.openfoodfacts;

import dev.mmatokovic.pantrio.domain.product.ProductNotFoundException;
import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductCatalogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class OpenFoodFactsAdapter implements ProductCatalogPort {

    private final RestClient client;
    private final OpenFoodFactsMapper mapper;

    private static final String PRODUCT_FIELDS = "product_name,product_quantity,product_quantity_unit,image_url,code";

    @Retryable(
        includes = {HttpServerErrorException.class, ResourceAccessException.class},
        delay = 1500, // 1.5-second delay
        multiplier = 1.5, // double the delay for each retry attempt
        maxDelay = 3500,
        maxRetries = 2 // 1 initial + 2 defined retries
    )
    @ConcurrencyLimit(6)
    @Override
    public Product getProductByBarcode(String barcode) {

        OpenFoodFactsProductResponse response = client.get()
            .uri("/api/v2/product/{barcode}?fields={fields}", barcode, PRODUCT_FIELDS)
            .retrieve()
            .onStatus(
                status -> status == HttpStatus.NOT_FOUND,
                (_, _) -> {
                    throw new ProductNotFoundException("Product " + barcode + " not found");
                })
            .body(OpenFoodFactsProductResponse.class);

        if (response == null || Objects.equals(response.status(), "0")) {
            throw new ProductNotFoundException("Product " + barcode + " not found");
        }

        return mapper.toDomain(response);
    }
}
