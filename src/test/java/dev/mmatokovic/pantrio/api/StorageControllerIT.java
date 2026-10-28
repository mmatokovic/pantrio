package dev.mmatokovic.pantrio.api;

import dev.mmatokovic.pantrio.BaseIntegrationTest;
import dev.mmatokovic.pantrio.domain.item.ItemResponse;
import dev.mmatokovic.pantrio.domain.storage.StorageItemRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageItemResponse;
import dev.mmatokovic.pantrio.domain.storage.StorageItemUpdateRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageResponse;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StorageControllerIT extends BaseIntegrationTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 1, 8, 30);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM ITEM_INSTANCE");
        jdbcTemplate.execute("DELETE FROM STORAGE");
        jdbcTemplate.execute("DELETE FROM ITEMS");
    }

    @Test
    void createStorage_returns201WithCreatedStorage() {
        StorageResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .contentType(ContentType.JSON)
            .body(new StorageRequest("Fridge", "Kitchen fridge", false))
            .when()
            .post("/api/v1/storages")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract().as(StorageResponse.class);

        var expectedResponse = new StorageResponse(null, "Fridge", "Kitchen fridge", false, null, null);
        assertThat(response)
            .usingRecursiveComparison()
            .ignoringFields("id", "createdAt", "updatedAt")
            .isEqualTo(expectedResponse);
    }

    @Test
    void createStorage_returns409_whenNameIsTaken() {
        jdbcTemplate.update("INSERT INTO storage (id, name) VALUES (1, 'Fridge')");

        RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .contentType(ContentType.JSON)
            .body(new StorageRequest("Fridge", null, false))
            .when()
            .post("/api/v1/storages")
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createStorage_returns400_whenNameIsBlank(String name) {
        RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .contentType(ContentType.JSON)
            .body(new StorageRequest(name, null, false))
            .when()
            .post("/api/v1/storages")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void getStorage_returnsStorage() {
        jdbcTemplate.update("INSERT INTO storage (id, name, created_at, updated_at) VALUES (1, 'Pantry', ?, ?)", CREATED_AT, CREATED_AT);

        StorageResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/api/v1/storages/1")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(StorageResponse.class);

        var expectedResponse = new StorageResponse(1L, "Pantry", null, false, CREATED_AT, CREATED_AT);
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void getStorage_returns404_whenStorageDoesNotExist() {
        RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/api/v1/storages/999")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void listStorages_returnsAllStorages() {
        jdbcTemplate.update("INSERT INTO storage (id, name, created_at, updated_at) VALUES (1, 'Fridge', ?, ?)", CREATED_AT, CREATED_AT);
        jdbcTemplate.update("INSERT INTO storage (id, name, created_at, updated_at) VALUES (2, 'Pantry', ?, ?)", CREATED_AT, CREATED_AT);

        List<StorageResponse> response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/api/v1/storages")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(new TypeRef<>() { });

        var expectedResponse = List.of(
            new StorageResponse(1L, "Fridge", null, false, CREATED_AT, CREATED_AT),
            new StorageResponse(2L, "Pantry", null, false, CREATED_AT, CREATED_AT));
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void updateStorage_returnsUpdatedStorage() {
        jdbcTemplate.update("INSERT INTO storage (id, name, created_at, updated_at) VALUES (1, 'Fridge', ?, ?)", CREATED_AT, CREATED_AT);

        StorageResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .contentType(ContentType.JSON)
            .body(new StorageRequest("Pantry", "Dry goods", false))
            .when()
            .put("/api/v1/storages/1")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(StorageResponse.class);

        var expectedResponse = new StorageResponse(1L, "Pantry", "Dry goods", false, CREATED_AT, null);
        assertThat(response)
            .usingRecursiveComparison()
            .ignoringFields("updatedAt")
            .isEqualTo(expectedResponse);
    }

    @Test
    void deleteStorage_returns204AndRemovesStorage() {
        jdbcTemplate.update("INSERT INTO storage (id, name) VALUES (1, 'Fridge')");

        RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .delete("/api/v1/storages/1")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM storage WHERE id = 1", Integer.class)).isZero();
    }

    @Test
    void addItem_returns201WithStockedItem() {
        jdbcTemplate.update("INSERT INTO storage (id, name, created_at, updated_at) VALUES (1, 'Fridge', ?, ?)", CREATED_AT, CREATED_AT);
        jdbcTemplate.update("INSERT INTO items (id, name, created_at, updated_at) VALUES (10, 'Milk', ?, ?)", CREATED_AT, CREATED_AT);

        StorageItemResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .contentType(ContentType.JSON)
            .body(new StorageItemRequest(10L, 2, LocalDate.of(2026, 11, 1)))
            .when()
            .post("/api/v1/storages/1/items")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract().as(StorageItemResponse.class);

        var expectedResponse = new StorageItemResponse(
            null,
            2L,
            LocalDate.of(2026, 11, 1),
            new ItemResponse(10L, "Milk", null, null, null, null, false, CREATED_AT, CREATED_AT),
            new StorageResponse(1L, "Fridge", null, false, CREATED_AT, CREATED_AT),
            null,
            null);
        assertThat(response)
            .usingRecursiveComparison()
            .ignoringFields("id", "createdAt", "updatedAt")
            .isEqualTo(expectedResponse);
    }

    @Test
    void addItem_returns404_whenStorageDoesNotExist() {
        jdbcTemplate.update("INSERT INTO items (id, name) VALUES (10, 'Milk')");

        RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .contentType(ContentType.JSON)
            .body(new StorageItemRequest(10L, 1, null))
            .when()
            .post("/api/v1/storages/999/items")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void addItem_returns404_whenItemDoesNotExist() {
        jdbcTemplate.update("INSERT INTO storage (id, name) VALUES (1, 'Fridge')");

        RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .contentType(ContentType.JSON)
            .body(new StorageItemRequest(999L, 1, null))
            .when()
            .post("/api/v1/storages/1/items")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void getStock_returnsItemsOfStorage() {
        jdbcTemplate.update("INSERT INTO storage (id, name, created_at, updated_at) VALUES (1, 'Fridge', ?, ?)", CREATED_AT, CREATED_AT);
        jdbcTemplate.update("INSERT INTO items (id, name, created_at, updated_at) VALUES (10, 'Milk', ?, ?)", CREATED_AT, CREATED_AT);
        jdbcTemplate.update(
            "INSERT INTO item_instance (id, storage_id, item_id, quantity, created_at, updated_at) VALUES (100, 1, 10, 3, ?, ?)",
            CREATED_AT, CREATED_AT);

        List<StorageItemResponse> response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/api/v1/storages/1/items")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(new TypeRef<>() { });

        var expectedResponse = List.of(new StorageItemResponse(
            100L,
            3L,
            null,
            new ItemResponse(10L, "Milk", null, null, null, null, false, CREATED_AT, CREATED_AT),
            new StorageResponse(1L, "Fridge", null, false, CREATED_AT, CREATED_AT),
            CREATED_AT,
            CREATED_AT));
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void showItem_returnsStockedItem() {
        jdbcTemplate.update("INSERT INTO storage (id, name, created_at, updated_at) VALUES (1, 'Fridge', ?, ?)", CREATED_AT, CREATED_AT);
        jdbcTemplate.update("INSERT INTO items (id, name, created_at, updated_at) VALUES (10, 'Milk', ?, ?)", CREATED_AT, CREATED_AT);
        jdbcTemplate.update(
            "INSERT INTO item_instance (id, storage_id, item_id, quantity, created_at, updated_at) VALUES (100, 1, 10, 3, ?, ?)",
            CREATED_AT, CREATED_AT);

        StorageItemResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/api/v1/storages/1/items/10")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(StorageItemResponse.class);

        var expectedResponse = new StorageItemResponse(
            100L,
            3L,
            null,
            new ItemResponse(10L, "Milk", null, null, null, null, false, CREATED_AT, CREATED_AT),
            new StorageResponse(1L, "Fridge", null, false, CREATED_AT, CREATED_AT),
            CREATED_AT,
            CREATED_AT);
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void restockItem_returnsUpdatedQuantity() {
        jdbcTemplate.update("INSERT INTO storage (id, name, created_at, updated_at) VALUES (1, 'Fridge', ?, ?)", CREATED_AT, CREATED_AT);
        jdbcTemplate.update("INSERT INTO items (id, name, created_at, updated_at) VALUES (10, 'Milk', ?, ?)", CREATED_AT, CREATED_AT);
        jdbcTemplate.update(
            "INSERT INTO item_instance (id, storage_id, item_id, quantity, created_at, updated_at) VALUES (100, 1, 10, 3, ?, ?)",
            CREATED_AT, CREATED_AT);

        StorageItemResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .contentType(ContentType.JSON)
            .body(new StorageItemUpdateRequest(10, LocalDate.of(2026, 12, 31)))
            .when()
            .put("/api/v1/storages/1/items/10")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().as(StorageItemResponse.class);

        var expectedResponse = new StorageItemResponse(
            100L,
            10L,
            LocalDate.of(2026, 12, 31),
            new ItemResponse(10L, "Milk", null, null, null, null, false, CREATED_AT, CREATED_AT),
            new StorageResponse(1L, "Fridge", null, false, CREATED_AT, CREATED_AT),
            CREATED_AT,
            null);
        assertThat(response)
            .usingRecursiveComparison()
            .ignoringFields("updatedAt")
            .isEqualTo(expectedResponse);
    }

    @Test
    void consumeItem_returns204AndRemovesStock() {
        jdbcTemplate.update("INSERT INTO storage (id, name, created_at, updated_at) VALUES (1, 'Fridge', ?, ?)", CREATED_AT, CREATED_AT);
        jdbcTemplate.update("INSERT INTO items (id, name, created_at, updated_at) VALUES (10, 'Milk', ?, ?)", CREATED_AT, CREATED_AT);
        jdbcTemplate.update(
            "INSERT INTO item_instance (id, storage_id, item_id, quantity, created_at, updated_at) VALUES (100, 1, 10, 3, ?, ?)",
            CREATED_AT, CREATED_AT);

        RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .delete("/api/v1/storages/1/items/10")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM item_instance WHERE id = 100", Integer.class)).isZero();
    }
}
