package dev.mmatokovic.pantrio.infra.openfoodfacts;

import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import dev.mmatokovic.pantrio.infra.error.NoSuchElementFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class OpenFoodFactsAdapter implements ProductRepository {

    private static final String PRODUCT_FIELDS = "product_name,product_quantity,product_quantity_unit,image_url,code";

    private final RestClient client;
    private final OpenFoodFactsMapper mapper;

    @Retryable(
        includes = {HttpServerErrorException.class, ResourceAccessException.class},
        delay = 1500, // 1.5-second delay
        multiplier = 1.5, // double the delay for each retry attempt
        maxDelay = 3500,
        maxRetries = 2 // 1 initial + 2 defined retries
    )
    @ConcurrencyLimit(5)
    @Override
    public Product getProductByBarcode(String barcode) {

        OpenFoodFactsProductResponse response = client.get()
            .uri("/api/v2/product/{barcode}?fields={fields}", barcode, PRODUCT_FIELDS)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                status -> status == HttpStatus.NOT_FOUND,
                (_, _) -> {
                    throw new NoSuchElementFoundException("Product " + barcode + " not found");
                })
            .body(OpenFoodFactsProductResponse.class);

        if (response == null || response.status() == 0) {
            throw new IllegalStateException("no code or invalid code");
        }

        return mapper.toDomain(response);
    }
}
