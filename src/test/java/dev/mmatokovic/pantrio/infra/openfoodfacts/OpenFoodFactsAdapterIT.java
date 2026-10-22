package dev.mmatokovic.pantrio.infra.openfoodfacts;

import dev.mmatokovic.pantrio.BaseIntegrationTest;
import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpServerErrorException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class OpenFoodFactsAdapterIT extends BaseIntegrationTest {

    @Autowired
    private OpenFoodFactsAdapter openFoodFactsAdapter;

    @Test
    void getProductByBarcode_mapFoundProduct() {
        stubFor(get(urlPathEqualTo("/api/v2/product/737628064502"))
            .willReturn(okJson("""
                {
                    "code": "737628064502",
                    "status": 1,
                    "product": {
                        "code": "737628064502",
                        "product_name": "Pasta",
                        "product_quantity": 500,
                        "product_quantity_unit": "g",
                        "image_url": "https://example.com/pasta.jpg"
                    }
                }
                """)));

        var product = openFoodFactsAdapter.getProductByBarcode("737628064502");

        assertThat(product).isEqualTo(new Product("737628064502", "Pasta", 500L, "g", "https://example.com/pasta.jpg"));
        verify(1, getRequestedFor(urlPathEqualTo("/api/v2/product/737628064502")));
    }

    @Test
    void getProductByBarcode_throwNoSuchElementFound_whenApiReturns404() {
        stubFor(get(urlPathEqualTo("/api/v2/product/000000000000"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"code":"000000000000","status":0,"status_verbose":"product not found"}
                    """)));

        assertThatExceptionOfType(NoSuchElementFoundException.class)
            .isThrownBy(() -> openFoodFactsAdapter.getProductByBarcode("000000000000"))
            .withMessage("Product 000000000000 not found");
    }

    @Test
    void getProductByBarcode_throwNoSuchElementFound_whenStatusIsZero() {
        stubFor(get(urlPathEqualTo("/api/v2/product/000000000000"))
            .willReturn(okJson("""
                {"code":"000000000000","status":0,"status_verbose":"product not found"}
                """)));

        assertThatExceptionOfType(NoSuchElementFoundException.class)
            .isThrownBy(() -> openFoodFactsAdapter.getProductByBarcode("000000000000"))
            .withMessage("Product 000000000000 not found");
    }

    @Test
    void getProductByBarcode_retryServerErrorsUntilSuccess() {
        stubFor(get(urlPathEqualTo("/api/v2/product/737628064502"))
            .inScenario("retry")
            .whenScenarioStateIs(STARTED)
            .willReturn(aResponse().withStatus(500))
            .willSetStateTo("retry-1"));
        stubFor(get(urlPathEqualTo("/api/v2/product/737628064502"))
            .inScenario("retry")
            .whenScenarioStateIs("retry-1")
            .willReturn(aResponse().withStatus(500))
            .willSetStateTo("retry-2"));
        stubFor(get(urlPathEqualTo("/api/v2/product/737628064502"))
            .inScenario("retry")
            .whenScenarioStateIs("retry-2")
            .willReturn(okJson("""
                {"code":"737628064502","status":1,"product":{"code":"737628064502","product_name":"Pasta"}}
                """)));

        var product = openFoodFactsAdapter.getProductByBarcode("737628064502");

        assertThat(product).isEqualTo(new Product("737628064502", "Pasta", null, null, null));
        verify(3, getRequestedFor(urlPathEqualTo("/api/v2/product/737628064502")));
    }

    @Test
    void getProductByBarcode_throwServerError_whenRetriesAreExhausted() {
        stubFor(get(urlPathEqualTo("/api/v2/product/737628064502"))
            .willReturn(aResponse().withStatus(500)));

        assertThatExceptionOfType(HttpServerErrorException.class)
            .isThrownBy(() -> openFoodFactsAdapter.getProductByBarcode("737628064502"));
        verify(3, getRequestedFor(urlPathEqualTo("/api/v2/product/737628064502")));
    }
}
