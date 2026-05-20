package dev.mmatokovic.pantrio.item;


import dev.mmatokovic.pantrio.BaseIntegrationTest;
import dev.mmatokovic.pantrio.domain.item.ItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WithMockUser(roles = "USER")
class ItemControllerIT extends BaseIntegrationTest {

    @Autowired
    RestTestClient rest;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM ITEM_INSTANCE");
        jdbcTemplate.execute("DELETE FROM ITEMS");
    }

    @Test
    void should_return_single_item() {
        jdbcTemplate.update(
            "INSERT INTO items (id, name) VALUES (1, 'string')"
        );

        ItemResponse itemResponse =
            rest.get()
                .uri("/items/{id}", 1)
                .headers(headers -> headers.setBasicAuth("user", "userpwd"))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ItemResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(itemResponse)
            .isNotNull()
            .extracting(ItemResponse::name)
            .isEqualTo("string");
    }

    @Test
    void should_return_404_when_item_not_found() {
        rest.get()
            .uri("/items/{id}", 999)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void should_return_list_of_items() {
        jdbcTemplate.update("INSERT INTO items (id, name) VALUES (1, 'Apple')");
        jdbcTemplate.update("INSERT INTO items (id, name) VALUES (2, 'Bread')");

        ItemResponse[] items = rest.get()
            .uri("/items")
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(ItemResponse[].class)
            .returnResult()
            .getResponseBody();

        assertThat(items).hasSize(2);
    }

    @Test
    void should_create_item_and_return_201_with_body() {
        String body = """
            {"name":"Milk","barcode":"8000500179864","unit":"pcs","disabled":false}
            """;

        ItemResponse created = rest.post()
            .uri("/items")
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .contentType(APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(ItemResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(created)
            .isNotNull()
            .satisfies(r -> {
                assertThat(r.id()).isNotNull();
                assertThat(r.name()).isEqualTo("Milk");
                assertThat(r.barcode()).isEqualTo("8000500179864");
            });
    }

    @Test
    void should_return_400_when_creating_item_with_blank_name() {
        String body = """
            {"name":"","disabled":false}
            """;

        rest.post()
            .uri("/items")
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .contentType(APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void should_update_item_and_return_200_with_body() {
        jdbcTemplate.update("INSERT INTO items (id, name) VALUES (1, 'OldName')");

        String body = """
            {"name":"NewName","disabled":false}
            """;

        ItemResponse updated = rest.put()
            .uri("/items/{id}", 1)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .contentType(APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isOk()
            .expectBody(ItemResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(updated)
            .isNotNull()
            .extracting(ItemResponse::name)
            .isEqualTo("NewName");
    }

    @Test
    void should_return_400_when_updating_item_with_blank_name() {
        jdbcTemplate.update("INSERT INTO items (id, name) VALUES (1, 'OldName')");

        String body = """
            {"name":"","disabled":false}
            """;

        rest.put()
            .uri("/items/{id}", 1)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .contentType(APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void should_delete_item_and_return_204() {
        jdbcTemplate.update("INSERT INTO items (id, name) VALUES (1, 'ToDelete')");

        rest.delete()
            .uri("/items/{id}", 1)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .exchange()
            .expectStatus().isNoContent();

        rest.get()
            .uri("/items/{id}", 1)
            .headers(h -> h.setBasicAuth("user", "userpwd"))
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void should_return_401_when_not_authenticated() {
        rest.get()
            .uri("/items")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized();
    }
}
