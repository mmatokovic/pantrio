package dev.mmatokovic.pantrio.api;

import dev.mmatokovic.pantrio.BaseIntegrationTest;
import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import dev.mmatokovic.pantrio.domain.product.ProductResponse;
import dev.mmatokovic.pantrio.infra.error.ErrorResponse;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static dev.mmatokovic.pantrio.infra.job.QuartzConfig.JOB_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

class JobManagementControllerIT extends BaseIntegrationTest {

    @MockitoBean
    private ProductRepository productRepository;


    @Autowired
    private JobRepository jobRepository;

    private static Map<String, Object> mapWithNulls(BigDecimal id, String name) {
        var row = new HashMap<String, Object>();
        row.put("ID", id);
        row.put("NAME", name);
        row.put("PICTURE_URL", null);
        row.put("UNIT", null);
        return row;
    }

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
    void executeJob_enrichesItemsAndMarksUnknownProducts() {
        jdbcTemplate.execute("DELETE FROM ITEM_INSTANCE");
        jdbcTemplate.execute("DELETE FROM ITEMS");
        jdbcTemplate.update("INSERT INTO items (id, name, barcode, created_at) VALUES (1, 'unknown', '737628064502', CURRENT_TIMESTAMP)");
        jdbcTemplate.update("INSERT INTO items (id, name, barcode, created_at) VALUES (2, 'Homemade jam', '000000000000', CURRENT_TIMESTAMP)");
        when(productRepository.getProductByBarcode("737628064502"))
            .thenReturn(new Product("737628064502", "Pasta", 500L, "g", "https://example.com/pasta.jpg"));
        when(productRepository.getProductByBarcode("000000000000"))
            .thenThrow(new NoSuchElementFoundException("Product 000000000000 not found"));

        RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .post("/api/v1/pull/job")
            .then()
            .statusCode(HttpStatus.ACCEPTED.value());

        await().atMost(Duration.ofSeconds(30))
            .until(() -> jobRepository.findRunningJobExecutions(JOB_NAME).isEmpty()
                && jdbcTemplate.queryForObject("SELECT COUNT(*) FROM items WHERE enriched_at IS NULL", Integer.class) == 0);

        var items = jdbcTemplate.queryForList("SELECT id, name, picture_url, unit FROM items ORDER BY id");
        assertThat(items).containsExactly(
            Map.of("ID", BigDecimal.ONE, "NAME", "Pasta", "PICTURE_URL", "https://example.com/pasta.jpg", "UNIT", "G"),
            mapWithNulls(BigDecimal.TWO, "Homemade jam"));
    }

    @Test
    void interruptJob_returns409_whenJobIsNotRunning() {
        ErrorResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .delete("/api/v1/pull/job")
            .then()
            .statusCode(HttpStatus.CONFLICT.value())
            .extract().as(ErrorResponse.class);

        assertThat(response).isEqualTo(new ErrorResponse(409, "Job itemEnrichmentJob is not running", null));
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
