package dev.mmatokovic.pantrio.infra.openfoodfacts;

import com.google.common.util.concurrent.RateLimiter;
import dev.mmatokovic.pantrio.domain.openfoodfacts.OpenFoodFactsProductResponse;
import dev.mmatokovic.pantrio.domain.openfoodfacts.OpenFoodFactsProperties;
import dev.mmatokovic.pantrio.domain.openfoodfacts.OpenFoodFactsRepo;
import org.springframework.http.HttpStatusCode;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.concurrent.Semaphore;

@Repository
public class OpenFoodFactsRepoImpl implements OpenFoodFactsRepo {

    private final OpenFoodFactsProperties openFoodFactsProperties;
    private final RestClient openFoodFactsRestClient;
    private final RateLimiter openFoodFactsProductRateLimiter;
    private final Semaphore openFoodFactsSemaphore;

    public OpenFoodFactsRepoImpl(OpenFoodFactsProperties openFoodFactsProperties, RestClient openFoodFactsRestClient, RateLimiter openFoodFactsProductRateLimiter, Semaphore openFoodFactsSemaphore) {
        this.openFoodFactsProperties = openFoodFactsProperties;
        this.openFoodFactsRestClient = openFoodFactsRestClient;
        this.openFoodFactsProductRateLimiter = openFoodFactsProductRateLimiter;
        this.openFoodFactsSemaphore = openFoodFactsSemaphore;
    }

    /**
     * Fetch a product by barcode from OpenFoodFacts.
     *
     * @Retryable configuration:
     * - default: All Exceptions & 3 retries
     * - maxAttempts: 4 (1 initial + 3 retries)
     * - includes: Only retry on RestaurantApiException
     * - backoff: Start with 1 second, multiply by 2 (exponential backoff)
     */
    @Retryable(
        includes = {HttpServerErrorException.class, ResourceAccessException.class},
        delay = 1000, // 1-second delay
        multiplier = 2 // double the delay for each retry attempt
    )
    @ConcurrencyLimit(5)
    @Override
    public OpenFoodFactsProductResponse indexProductByBarcode(String barcode) {
        try {
            openFoodFactsSemaphore.acquire();
            openFoodFactsProductRateLimiter.acquire();

            return openFoodFactsRestClient.get()
                .uri(openFoodFactsProperties.host() + "/api/v2/product/{barcode}", barcode)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new IllegalArgumentException("Product with barcode " + barcode + " not found in OpenFoodFacts");
                })
                .body(OpenFoodFactsProductResponse.class);

        } catch (RestClientException e) {
            throw new RuntimeException("Unexpected error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for OpenFoodFacts semaphore!", e);
        } finally {
            openFoodFactsSemaphore.release();
        }
    }
}
