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

    /**
     * Fetch a product by barcode from OpenFoodFacts.
     *
     * @Retryable configuration:
     * - default: All Exceptions & 3 retries
     * - maxRetries: 4 (1 initial + 3 retries)
     * - includes: Only retry on OpenFoodFactsApiException
     * - backoff: Start with 1 second, multiply by 2 (exponential backoff)
     */
    @Retryable(
        includes = {HttpServerErrorException.class, ResourceAccessException.class},
        delay = 1500, // 1.5-second delay
        multiplier = 1.5, // double the delay for each retry attempt
        maxDelay = 3500
    )
    @ConcurrencyLimit(5)
    @Override
    public OpenFoodFactsProductResponse indexProductByBarcode(String barcode) {
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
