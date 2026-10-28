package dev.mmatokovic.pantrio.api;

import dev.mmatokovic.pantrio.BaseIntegrationTest;
import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import dev.mmatokovic.pantrio.domain.product.ProductResponse;
import dev.mmatokovic.pantrio.infra.error.ErrorResponse;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class JobManagementControllerIT extends BaseIntegrationTest {

    @MockitoBean
    private ProductRepository productRepository;

    @Test
    void pullProduct_returnsProduct() {
        when(productRepository.getProductByBarcode("737628064502"))
            .thenReturn(new Product("737628064502", "Pasta", 500L, "g", "https://example.com/pasta.jpg"));

        ProductResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/api/v1/pull/737628064502")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ProductResponse.class);

        assertThat(response).isEqualTo(new ProductResponse("Pasta", "737628064502", 500L, "g", "https://example.com/pasta.jpg"));
    }

    @Test
    void changeJobState_pausesJob_whenStateIsLowercase() {
        RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .queryParam("state", "pause")
            .when()
            .put("/api/v1/pull/job/state")
            .then()
            .statusCode(HttpStatus.OK.value());

        Map<String, String> response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/api/v1/pull/job")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(new TypeRef<>() { });

        assertThat(response).isEqualTo(Map.of("state", "PAUSED"));
    }

    @Test
    void changeJobState_resumesJob() {
        RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .queryParam("state", "RESUME")
            .when()
            .put("/api/v1/pull/job/state")
            .then()
            .statusCode(HttpStatus.OK.value());

        Map<String, String> response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/api/v1/pull/job")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(new TypeRef<>() { });

        assertThat(response).isEqualTo(Map.of("state", "NORMAL"));
    }

    @Test
    void changeJobState_returns400_whenStateIsUnknown() {
        ErrorResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .queryParam("state", "STOP")
            .when()
            .put("/api/v1/pull/job/state")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract().as(ErrorResponse.class);

        assertThat(response).isEqualTo(new ErrorResponse(400, "Failed to convert 'state' with value: 'STOP'", null));
    }

    @Test
    void rescheduleJob_returns400_whenCronIsInvalid() {
        ErrorResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .queryParam("cron", "every five minutes")
            .when()
            .put("/api/v1/pull/job")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract().as(ErrorResponse.class);

        assertThat(response).isEqualTo(new ErrorResponse(400, "Invalid cron expression: every five minutes", null));
    }
}
