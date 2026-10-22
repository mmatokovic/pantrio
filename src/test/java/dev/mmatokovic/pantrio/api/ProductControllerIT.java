package dev.mmatokovic.pantrio.api;

import dev.mmatokovic.pantrio.BaseIntegrationTest;
import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import dev.mmatokovic.pantrio.domain.product.ProductResponse;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProductControllerIT extends BaseIntegrationTest {

    @MockitoBean
    private ProductRepository productRepository;

    @Test
    void findProduct_returnsProduct() {
        when(productRepository.getProductByBarcode("737628064502"))
            .thenReturn(new Product("737628064502", "Pasta", 500L, "g", "https://example.com/pasta.jpg"));

        ProductResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/products/737628064502")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ProductResponse.class);

        var expectedResponse = new ProductResponse("Pasta", "737628064502", 500L, "g", "https://example.com/pasta.jpg");
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void findProduct_returns404_whenProductDoesNotExist() {
        when(productRepository.getProductByBarcode("737628064502"))
            .thenThrow(new NoSuchElementFoundException("Product 737628064502 not found"));

        RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/products/737628064502")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void findProduct_returns422_whenBarcodeIsInvalid() {
        RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/products/737628064503")
            .then()
            .statusCode(HttpStatus.UNPROCESSABLE_CONTENT.value());

        verifyNoInteractions(productRepository);
    }
}
