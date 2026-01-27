package dev.mmatokovic.pantrio.infra;

import dev.mmatokovic.pantrio.BaseIntegrationTest;
import dev.mmatokovic.pantrio.domain.openfoodfacts.OpenFoodFactsProductResponse;
import dev.mmatokovic.pantrio.domain.openfoodfacts.OpenFoodFactsRepo;
import dev.mmatokovic.pantrio.infra.rest.RestClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@WithMockUser(roles = "USER")
@Import(RestClientConfig.class)
public class OpenFoodFactsRepoIT extends BaseIntegrationTest {

    @Autowired
    private OpenFoodFactsRepo repo;

    @Test
    void givenValidBarcode_whenIndexProduct_thenReturnProductDetails_realApi() {
        OpenFoodFactsProductResponse response = repo.indexProductByBarcode("8000500179864");

        assertNotNull(response);
        System.out.println("Status: " + response.status());
        System.out.println("Verbose: " + response.statusVerbose());
        if (response.product() != null) {
            System.out.println("Product name: " + response.product().productName());
            System.out.println("Brands: " + response.product().brands());
        }
    }
}
