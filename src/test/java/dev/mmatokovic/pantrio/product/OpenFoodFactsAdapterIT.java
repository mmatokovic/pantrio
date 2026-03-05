package dev.mmatokovic.pantrio.product;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

import dev.mmatokovic.pantrio.BaseIntegrationTest;
import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import dev.mmatokovic.pantrio.infra.openfoodfacts.OpenFoodFactsAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpServerErrorException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@WireMockTest
class OpenFoodFactsAdapterIT extends BaseIntegrationTest {

    @Autowired
    private OpenFoodFactsAdapter adapter;

    @Test
    void should_return_product_when_found() {
        stubFor(get(urlPathEqualTo("/api/v2/product/737628064502"))
            .willReturn(okJson("""
                {
                    "code": "737628064502",
                    "status": 1,
                    "product": {
                        "product_name": "Pasta",
                        "product_quantity": 500,
                        "product_quantity_unit": "g",
                        "image_url": "https://example.com/pasta.jpg"
                    }
                }
            """)));

        Product result = adapter.getProductByBarcode("737628064502");

        assertThat(result)
            .isNotNull()
            .satisfies(p -> {
                assertThat(p.productName()).isEqualTo("Pasta");
                assertThat(p.code()).isEqualTo("737628064502");
                assertThat(p.quantity()).isEqualTo(500);
                assertThat(p.unit()).isEqualTo("g");
            });

        verify(1, getRequestedFor(urlPathEqualTo("/api/v2/product/737628064502")));
    }

    @Test
    void should_return_not_found_when_not_found() {
        stubFor(get(urlPathEqualTo("/api/v2/product/000000000000"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"code":"000000000000","status":0,"status_verbose":"product not found"}
                """)));

        assertThatThrownBy(() -> adapter.getProductByBarcode("000000000000"))
            .isInstanceOf(NoSuchElementFoundException.class)
            .hasMessageContaining("000000000000 not found");
    }

    @Test
    void should_retry_on_server_error_and_succeed() {
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
                {"code":"737628064502","status":1,"product":{"product_name":"Pasta"}}
            """)));

        Product result = adapter.getProductByBarcode("737628064502");

        assertThat(result).isNotNull();
        verify(3, getRequestedFor(urlPathEqualTo("/api/v2/product/737628064502")));
    }

    @Test
    void should_throw_after_retries_exhausted() {
        stubFor(get(urlPathEqualTo("/api/v2/product/737628064502"))
            .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> adapter.getProductByBarcode("737628064502"))
            .isInstanceOf(HttpServerErrorException.class);

        verify(3, getRequestedFor(urlPathEqualTo("/api/v2/product/737628064502")));
    }
}
