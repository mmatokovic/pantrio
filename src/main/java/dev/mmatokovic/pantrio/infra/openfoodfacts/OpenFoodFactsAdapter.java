package dev.mmatokovic.pantrio.infra.openfoodfacts;

import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class OpenFoodFactsAdapter implements ProductRepository {

    private static final String PRODUCT_FIELDS = "product_name,product_quantity,product_quantity_unit,image_url,code";

    private final RestClient openFoodFactsClient;
    private final RestClient openFoodFactsImageClient;
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

        OpenFoodFactsProductResponse response = openFoodFactsClient.get()
            .uri("/product/{barcode}?fields={fields}", barcode, PRODUCT_FIELDS)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                status -> status == HttpStatus.NOT_FOUND,
                (req, res) -> {
                    throw new NoSuchElementFoundException("Product " + barcode + " not found");
                })
            .onStatus(
                status -> status == HttpStatus.TOO_MANY_REQUESTS,
                (req, res) -> {
                    throw new HttpServerErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limited by OpenFoodFacts");
                })
            .body(OpenFoodFactsProductResponse.class);

        if (response == null || response.status() == 0) {
            throw new NoSuchElementFoundException("no code or invalid code");
        }

        return mapper.toDomain(response);
    }

    @Retryable(
        includes = {HttpServerErrorException.class, ResourceAccessException.class},
        delay = 1500,
        multiplier = 1.5,
        maxDelay = 3500,
        maxRetries = 2
    )
    @ConcurrencyLimit(5)
    @Override
    public InputStream fetchImage(String imageUrl) {
        return openFoodFactsImageClient.get()
            .uri(imageUrl)
            .accept(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG)
            .retrieve()
            .onStatus(status -> status == HttpStatus.NOT_FOUND,
                (req, res) -> {
                    throw new NoSuchElementFoundException("File not found: " + imageUrl);
            })
            .onStatus(HttpStatusCode::isError,
                (req, res) -> {
                    throw new HttpServerErrorException(res.getStatusCode());
            })
            .body(InputStream.class);
    }
}
