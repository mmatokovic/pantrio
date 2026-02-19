package dev.mmatokovic.pantrio.infra.openfoodfacts;

import dev.mmatokovic.pantrio.app.product.ProductException;
import dev.mmatokovic.pantrio.domain.product.OpenFoodFactsProductResponse;
import dev.mmatokovic.pantrio.domain.product.OpenFoodFactsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Repository
@RequiredArgsConstructor
public class OpenFoodFactsAdapter implements OpenFoodFactsPort {

    private final RestClient openFoodFactsRestClient;

    @Retryable(
        includes = {HttpServerErrorException.class, ResourceAccessException.class},
        delay = 1500, // 1.5-second delay
        multiplier = 1.5, // double the delay for each retry attempt
        maxDelay = 3500,
        maxRetries = 2 // 1 initial + 2 defined retries
    )
    @ConcurrencyLimit(5)
    @Override
    public OpenFoodFactsProductResponse getProductByBarcode(String barcode) {
        try {
            String fields = "product_name,product_quantity,product_quantity_unit,image_url";

            return openFoodFactsRestClient.get()
                .uri("/api/v2/product/{barcode}?fields={fields}", barcode, fields)
                .exchange((request, response) -> switch (response.getStatusCode().value()) {
                    case 200 -> response.bodyTo(OpenFoodFactsProductResponse.class);
                    case 404 -> throw new ProductException("Product not found for barcode: " + barcode);
                    case 429 -> throw new ProductException("Rate limit exceeded");
                    default -> throw new ProductException("Unexpected status: " + response.getStatusCode());
                });

        } catch (RestClientException e) {
            throw new ProductException("Unexpected error calling OpenFoodFacts API", e);
        }
    }
}
