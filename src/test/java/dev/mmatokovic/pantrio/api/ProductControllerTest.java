package dev.mmatokovic.pantrio.api;

import dev.mmatokovic.pantrio.app.product.ProductService;
import dev.mmatokovic.pantrio.domain.product.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService productService;

    RestTestClient rest;

    @BeforeEach
    void setUp() {
        rest = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    void should_getProductByBarcode_ReturnsProduct() {
        when(productService.findProductByBarcode("737628064502")).thenReturn(
            new ProductResponse("Pasta", "737628064502", 500L, "g", "https://example.com/pasta.jpg")
        );

        ProductResponse product = rest.get()
            .uri("/products/737628064502")
            .exchange()
            .expectStatus().isOk()
            .expectBody(ProductResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(product)
            .isNotNull()
            .extracting(ProductResponse::name)
            .isEqualTo("Pasta");
    }

    @Test
    void should_validate_Input() {
        RestTestClient client = RestTestClient.bindTo(mockMvc).build();

        client.post().uri("/products")
            .body(new ProductResponse(null, null, null, null, null))
            .exchange()
            .expectStatus().isNotFound();
    }
}
