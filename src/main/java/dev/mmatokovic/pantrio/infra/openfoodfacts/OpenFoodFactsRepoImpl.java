package dev.mmatokovic.pantrio.infra.openfoodfacts;

import com.google.common.util.concurrent.RateLimiter;
import dev.mmatokovic.pantrio.domain.openfoodfacts.OpenFoodFactsProductResponse;
import dev.mmatokovic.pantrio.domain.openfoodfacts.OpenFoodFactsRepo;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Repository
public class OpenFoodFactsRepoImpl implements OpenFoodFactsRepo {

    private final RestClient openFoodFactsRestClient;
    private final RateLimiter openFoodFactsProductRateLimiter;

    public OpenFoodFactsRepoImpl(RestClient openFoodFactsRestClient, RateLimiter openFoodFactsProductRateLimiter) {
        this.openFoodFactsRestClient = openFoodFactsRestClient;
        this.openFoodFactsProductRateLimiter = openFoodFactsProductRateLimiter;
    }

    @Retryable(
        includes = {HttpServerErrorException.class, ResourceAccessException.class},
        delay = 1500, // 1.5-second delay
        multiplier = 1.5, // double the delay for each retry attempt
        maxDelay = 3500,
        maxRetries = 3 // 1 initial + 2 retries
    )
    @ConcurrencyLimit(5)
    @Override
    public OpenFoodFactsProductResponse getProductByBarcode(String barcode) {
        try {
            openFoodFactsProductRateLimiter.acquire();
            String fields = "product_name,brands,quantity,image_url";

            return openFoodFactsRestClient.get()
                .uri("/api/v2/product/{barcode}?fields={fields}", barcode, fields)
                .retrieve()
                .body(OpenFoodFactsProductResponse.class);

        } catch (RestClientException e) {
            throw new RuntimeException("Unexpected error calling OpenFoodFacts API", e);
        }
    }
}
