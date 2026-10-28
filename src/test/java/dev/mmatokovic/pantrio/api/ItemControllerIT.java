package dev.mmatokovic.pantrio.api;

import dev.mmatokovic.pantrio.BaseIntegrationTest;
import dev.mmatokovic.pantrio.domain.item.ItemRequest;
import dev.mmatokovic.pantrio.domain.item.ItemResponse;
import dev.mmatokovic.pantrio.domain.item.UnitEnum;
import dev.mmatokovic.pantrio.infra.error.ErrorResponse;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemControllerIT extends BaseIntegrationTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 1, 8, 30);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM ITEM_INSTANCE");
        jdbcTemplate.execute("DELETE FROM ITEMS");
    }

    @Test
    void getItem_returnsItem() {
        jdbcTemplate.update("INSERT INTO items (id, name, created_at, updated_at) VALUES (1, 'Milk', ?, ?)", CREATED_AT, CREATED_AT);

        ItemResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/api/v1/items/1")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ItemResponse.class);

        var expectedResponse = new ItemResponse(1L, "Milk", null, null, null, null, false, CREATED_AT, CREATED_AT);
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void getItem_returns404_whenItemDoesNotExist() {
        ErrorResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/api/v1/items/999")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .extract().as(ErrorResponse.class);

        assertThat(response).isEqualTo(new ErrorResponse(404, "Item 999 not found", null));
    }

    @Test
    void listItems_returnsAllItems() {
        jdbcTemplate.update("INSERT INTO items (id, name, created_at, updated_at) VALUES (1, 'Apple', ?, ?)", CREATED_AT, CREATED_AT);
        jdbcTemplate.update("INSERT INTO items (id, name, created_at, updated_at) VALUES (2, 'Bread', ?, ?)", CREATED_AT, CREATED_AT);

        List<ItemResponse> response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/api/v1/items")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(new TypeRef<>() { });

        var expectedResponse = List.of(
            new ItemResponse(1L, "Apple", null, null, null, null, false, CREATED_AT, CREATED_AT),
            new ItemResponse(2L, "Bread", null, null, null, null, false, CREATED_AT, CREATED_AT));
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void createItem_returns201WithCreatedItem() {
        ItemResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .contentType(ContentType.JSON)
            .body(new ItemRequest("Milk", null, "8000500179864", UnitEnum.PCS, null, false))
            .when()
            .post("/api/v1/items")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract().as(ItemResponse.class);

        var expectedResponse = new ItemResponse(null, "Milk", null, "8000500179864", UnitEnum.PCS, null, false, null, null);
        assertThat(response)
            .usingRecursiveComparison()
            .ignoringFields("id", "createdAt", "updatedAt")
            .isEqualTo(expectedResponse);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createItem_returns400_whenNameIsBlank(String name) {
        ErrorResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .contentType(ContentType.JSON)
            .body(new ItemRequest(name, null, null, null, null, false))
            .when()
            .post("/api/v1/items")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract().as(ErrorResponse.class);

        var expectedResponse = new ErrorResponse(400, "Validation failed", List.of(new ErrorResponse.ErrorInfo("name", "Name must not be blank")));
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void createItem_returns400_whenBarcodeIsInvalid() {
        ErrorResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .contentType(ContentType.JSON)
            .body(new ItemRequest("Milk", null, "8000500179865", null, null, false))
            .when()
            .post("/api/v1/items")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract().as(ErrorResponse.class);

        var expectedResponse = new ErrorResponse(400, "Validation failed", List.of(new ErrorResponse.ErrorInfo("barcode", "Invalid barcode")));
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void createItem_returns400_whenBodyIsMalformed() {
        // Malformed JSON cannot be expressed as an ItemRequest record
        ErrorResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .contentType(ContentType.JSON)
            .body("{\"name\": ")
            .when()
            .post("/api/v1/items")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract().as(ErrorResponse.class);

        assertThat(response).isEqualTo(new ErrorResponse(400, "Failed to read request", null));
    }

    @Test
    void updateItem_returnsUpdatedItem() {
        jdbcTemplate.update("INSERT INTO items (id, name, created_at, updated_at) VALUES (1, 'Milk', ?, ?)", CREATED_AT, CREATED_AT);

        ItemResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .contentType(ContentType.JSON)
            .body(new ItemRequest("Oat milk", "Lactose free", null, null, null, false))
            .when()
            .put("/api/v1/items/1")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ItemResponse.class);

        var expectedResponse = new ItemResponse(1L, "Oat milk", "Lactose free", null, null, null, false, CREATED_AT, null);
        assertThat(response)
            .usingRecursiveComparison()
            .ignoringFields("updatedAt")
            .isEqualTo(expectedResponse);
    }

    @Test
    void updateItem_keepsFieldsMissingFromRequest_inV1() {
        jdbcTemplate.update("INSERT INTO items (id, name, remarks, created_at, updated_at) VALUES (1, 'Milk', 'Fresh', ?, ?)", CREATED_AT, CREATED_AT);

        ItemResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .contentType(ContentType.JSON)
            .body(new ItemRequest("Oat milk", null, null, null, null, null))
            .when()
            .put("/api/v1/items/1")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ItemResponse.class);

        assertThat(response)
            .usingRecursiveComparison()
            .ignoringFields("updatedAt")
            .isEqualTo(new ItemResponse(1L, "Oat milk", "Fresh", null, null, null, false, CREATED_AT, null));
    }

    @Test
    void replaceItem_clearsFieldsMissingFromRequest_inV2() {
        jdbcTemplate.update("INSERT INTO items (id, name, remarks, created_at, updated_at) VALUES (1, 'Milk', 'Fresh', ?, ?)", CREATED_AT, CREATED_AT);

        ItemResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .contentType(ContentType.JSON)
            .body(new ItemRequest("Oat milk", null, null, null, null, null))
            .when()
            .put("/api/v2/items/1")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ItemResponse.class);

        assertThat(response)
            .usingRecursiveComparison()
            .ignoringFields("updatedAt")
            .isEqualTo(new ItemResponse(1L, "Oat milk", null, null, null, null, false, CREATED_AT, null));
    }

    @Test
    void listItems_returns400_whenSortPropertyIsUnknown() {
        ErrorResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .queryParam("sort", "colour")
            .when()
            .get("/api/v1/items")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract().as(ErrorResponse.class);

        assertThat(response).isEqualTo(new ErrorResponse(400, "Unknown sort property: colour", null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void updateItem_returns400_whenNameIsBlank(String name) {
        jdbcTemplate.update("INSERT INTO items (id, name, created_at, updated_at) VALUES (1, 'Milk', ?, ?)", CREATED_AT, CREATED_AT);

        RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .contentType(ContentType.JSON)
            .body(new ItemRequest(name, null, null, null, null, false))
            .when()
            .put("/api/v1/items/1")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void deleteItem_returns204AndRemovesItem() {
        jdbcTemplate.update("INSERT INTO items (id, name, created_at, updated_at) VALUES (1, 'Milk', ?, ?)", CREATED_AT, CREATED_AT);

        RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .delete("/api/v1/items/1")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM items WHERE id = 1", Integer.class)).isZero();
    }

    @Test
    void listItems_returns401_whenNotAuthenticated() {
        ErrorResponse response = RestAssured.given()
            .when()
            .get("/api/v1/items")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"pantrio\"")
            .extract().as(ErrorResponse.class);

        assertThat(response).isEqualTo(new ErrorResponse(401, "Authentication required", null));
    }

    @Test
    void listItems_returns403_whenUserIsTheMetricsUser() {
        ErrorResponse response = RestAssured.given()
            .auth().basic("prometheus", "prometheus")
            .when()
            .get("/api/v1/items")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .extract().as(ErrorResponse.class);

        assertThat(response).isEqualTo(new ErrorResponse(403, "Access denied", null));
    }
}
