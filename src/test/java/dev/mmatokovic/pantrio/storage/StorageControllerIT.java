package dev.mmatokovic.pantrio.storage;

import dev.mmatokovic.pantrio.BaseIntegrationTest;
import dev.mmatokovic.pantrio.domain.storage.StorageItemResponse;
import dev.mmatokovic.pantrio.domain.storage.StorageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WithMockUser(roles = "USER")
class StorageControllerIT extends BaseIntegrationTest {

    @Autowired
    RestTestClient rest;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM ITEM_INSTANCE");
        jdbcTemplate.execute("DELETE FROM STORAGE");
        jdbcTemplate.execute("DELETE FROM ITEMS");
    }

    // --- Storage CRUD ---

    @Test
    void should_create_storage_and_return_201_with_body() {
        String body = """
            {"name":"Fridge","description":"Kitchen fridge","disabled":false}
            """;

        StorageResponse created = rest.post()
            .uri("/storages")
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .contentType(APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(StorageResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(created)
            .isNotNull()
            .satisfies(r -> {
                assertThat(r.id()).isNotNull();
                assertThat(r.name()).isEqualTo("Fridge");
                assertThat(r.description()).isEqualTo("Kitchen fridge");
            });
    }

    @Test
    void should_return_409_when_creating_storage_with_duplicate_name() {
        jdbcTemplate.update("INSERT INTO storage (id, name) VALUES (1, 'Fridge')");

        String body = """
            {"name":"Fridge","disabled":false}
            """;

        rest.post()
            .uri("/storages")
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .contentType(APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isEqualTo(409);
    }

    @Test
    void should_return_400_when_creating_storage_with_blank_name() {
        String body = """
            {"name":"","disabled":false}
            """;

        rest.post()
            .uri("/storages")
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .contentType(APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void should_return_storage_when_found() {
        jdbcTemplate.update("INSERT INTO storage (id, name) VALUES (1, 'Pantry')");

        StorageResponse response = rest.get()
            .uri("/storages/{id}", 1)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(StorageResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(response)
            .isNotNull()
            .extracting(StorageResponse::name)
            .isEqualTo("Pantry");
    }

    @Test
    void should_return_404_when_storage_not_found() {
        rest.get()
            .uri("/storages/{id}", 999)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void should_return_list_of_storages() {
        jdbcTemplate.update("INSERT INTO storage (id, name) VALUES (1, 'Fridge')");
        jdbcTemplate.update("INSERT INTO storage (id, name) VALUES (2, 'Pantry')");

        StorageResponse[] storages = rest.get()
            .uri("/storages")
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(StorageResponse[].class)
            .returnResult()
            .getResponseBody();

        assertThat(storages).hasSize(2);
    }

    @Test
    void should_update_storage_and_return_200_with_body() {
        jdbcTemplate.update("INSERT INTO storage (id, name) VALUES (1, 'OldName')");

        String body = """
            {"name":"NewName","description":"Updated desc","disabled":false}
            """;

        StorageResponse updated = rest.put()
            .uri("/storages/{id}", 1)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .contentType(APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isOk()
            .expectBody(StorageResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(updated)
            .isNotNull()
            .satisfies(r -> {
                assertThat(r.name()).isEqualTo("NewName");
                assertThat(r.description()).isEqualTo("Updated desc");
            });
    }

    @Test
    void should_delete_storage_and_return_204() {
        jdbcTemplate.update("INSERT INTO storage (id, name) VALUES (1, 'ToDelete')");

        rest.delete()
            .uri("/storages/{id}", 1)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .exchange()
            .expectStatus().isNoContent();

        rest.get()
            .uri("/storages/{id}", 1)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }

    // --- Stock (Item Instances) ---

    @Test
    void should_add_item_to_storage_and_return_201_with_body() {
        jdbcTemplate.update("INSERT INTO storage (id, name) VALUES (1, 'Fridge')");
        jdbcTemplate.update("INSERT INTO items (id, name) VALUES (10, 'Milk')");

        String body = """
            {"itemId":10,"quantity":2}
            """;

        StorageItemResponse created = rest.post()
            .uri("/storages/{storageId}/items", 1)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .contentType(APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(StorageItemResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(created)
            .isNotNull()
            .satisfies(r -> {
                assertThat(r.id()).isNotNull();
                assertThat(r.quantity()).isEqualTo(2L);
            });
    }

    @Test
    void should_return_404_when_adding_item_to_non_existent_storage() {
        jdbcTemplate.update("INSERT INTO items (id, name) VALUES (10, 'Milk')");

        String body = """
            {"itemId":10,"quantity":1}
            """;

        rest.post()
            .uri("/storages/{storageId}/items", 999)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .contentType(APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void should_return_404_when_adding_non_existent_item_to_storage() {
        jdbcTemplate.update("INSERT INTO storage (id, name) VALUES (1, 'Fridge')");

        String body = """
            {"itemId":999,"quantity":1}
            """;

        rest.post()
            .uri("/storages/{storageId}/items", 1)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .contentType(APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void should_return_stock_for_storage() {
        jdbcTemplate.update("INSERT INTO storage (id, name) VALUES (1, 'Fridge')");
        jdbcTemplate.update("INSERT INTO items (id, name) VALUES (10, 'Milk')");
        jdbcTemplate.update("INSERT INTO item_instance (id, storage_id, item_id, quantity) VALUES (100, 1, 10, 3)");

        StorageItemResponse[] stock = rest.get()
            .uri("/storages/{storageId}/items", 1)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(StorageItemResponse[].class)
            .returnResult()
            .getResponseBody();

        assertThat(stock).hasSize(1);
    }

    @Test
    void should_return_single_stock_item() {
        jdbcTemplate.update("INSERT INTO storage (id, name) VALUES (1, 'Fridge')");
        jdbcTemplate.update("INSERT INTO items (id, name) VALUES (10, 'Milk')");
        jdbcTemplate.update("INSERT INTO item_instance (id, storage_id, item_id, quantity) VALUES (100, 1, 10, 3)");

        StorageItemResponse response = rest.get()
            .uri("/storages/{storageId}/items/{itemId}", 1, 10)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(StorageItemResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(response)
            .isNotNull()
            .extracting(StorageItemResponse::quantity)
            .isEqualTo(3L);
    }

    @Test
    void should_update_stock_item_quantity_and_return_200() {
        jdbcTemplate.update("INSERT INTO storage (id, name) VALUES (1, 'Fridge')");
        jdbcTemplate.update("INSERT INTO items (id, name) VALUES (10, 'Milk')");
        jdbcTemplate.update("INSERT INTO item_instance (id, storage_id, item_id, quantity) VALUES (100, 1, 10, 3)");

        String body = """
            {"quantity":10}
            """;

        StorageItemResponse updated = rest.put()
            .uri("/storages/{storageId}/items/{itemId}", 1, 10)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .contentType(APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isOk()
            .expectBody(StorageItemResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(updated)
            .isNotNull()
            .extracting(StorageItemResponse::quantity)
            .isEqualTo(10L);
    }

    @Test
    void should_delete_stock_item_and_return_204() {
        jdbcTemplate.update("INSERT INTO storage (id, name) VALUES (1, 'Fridge')");
        jdbcTemplate.update("INSERT INTO items (id, name) VALUES (10, 'Milk')");
        jdbcTemplate.update("INSERT INTO item_instance (id, storage_id, item_id, quantity) VALUES (100, 1, 10, 3)");

        rest.delete()
            .uri("/storages/{storageId}/items/{itemId}", 1, 10)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .exchange()
            .expectStatus().isNoContent();

        rest.get()
            .uri("/storages/{storageId}/items/{itemId}", 1, 10)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }
}
