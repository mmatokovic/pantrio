package dev.mmatokovic.pantrio.infra.openfoodfacts;

import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Repository
@RequiredArgsConstructor
public class OpenFoodFactsAdapter implements ProductRepository {

    private static final String PRODUCT_FIELDS = "product_name,product_quantity,product_quantity_unit,image_url,code";

    private final RestClient openFoodFactsClient;
    private final OpenFoodFactsMapper openFoodFactsMapper;

    @Retryable(
        includes = {HttpServerErrorException.class, ResourceAccessException.class},
        delay = 1500,
        multiplier = 1.5,
        maxDelay = 3500,
        jitter = 500,
        maxRetries = 2
    )
    @ConcurrencyLimit(limit = 5, policy = ConcurrencyLimit.ThrottlePolicy.REJECT)
    @Override
    public Product getProductByBarcode(String barcode) {
        var response = openFoodFactsClient.get()
            .uri("/product/{barcode}?fields={fields}", barcode, PRODUCT_FIELDS)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                status -> status == HttpStatus.NOT_FOUND,
                (clientRequest, clientResponse) -> {
                    throw new NoSuchElementFoundException("Product " + barcode + " not found");
                })
            .onStatus(
                status -> status == HttpStatus.TOO_MANY_REQUESTS,
                (clientRequest, clientResponse) -> {
                    throw new HttpServerErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limited by OpenFoodFacts");
                })
            .body(OpenFoodFactsProductResponse.class);

        if (response == null || response.status() == null || response.status() == 0) {
            throw new NoSuchElementFoundException("Product " + barcode + " not found");
        }
        return openFoodFactsMapper.toDomain(response);
    }
}
