package dev.mmatokovic.pantrio.infra;

import dev.mmatokovic.pantrio.BaseIntegrationTest;
import dev.mmatokovic.pantrio.domain.openfoodfacts.OpenFoodFactsProductResponse;
import dev.mmatokovic.pantrio.domain.openfoodfacts.OpenFoodFactsRepo;
import dev.mmatokovic.pantrio.infra.rest.RestClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(RestClientConfig.class)
public class OpenFoodFactsRepoIT extends BaseIntegrationTest {

    @Autowired
    private OpenFoodFactsRepo repo;

    @Test
    void givenValidBarcode_whenIndexProduct_thenReturnProductDetails_realApi() {
        OpenFoodFactsProductResponse response = repo.indexProductByBarcode("8000500179864");

        assertNotNull(response, "Response should not be null");
        assertThat(response.status()).isEqualTo("1");
        assertThat(response.statusVerbose()).isEqualTo("product found");
        assertThat(response.product()).isNotNull();
        assertThat(response.product().productName()).isNotEmpty().isEqualTo("Nutella");
    }

    @Test
    void givenInvalidBarcode_whenIndexProduct_thenThrowException() {
        OpenFoodFactsProductResponse response = repo.indexProductByBarcode("0000000000000");

        assertNotNull(response, "Response should not be null");
        assertThat(response.status()).isEqualTo("0");
        assertThat(response.statusVerbose()).isEqualTo("no code or invalid code");
    }
}
