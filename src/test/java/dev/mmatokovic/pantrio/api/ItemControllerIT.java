package dev.mmatokovic.pantrio.api;


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
        jdbcTemplate.execute("TRUNCATE TABLE PANTRIO_TEST.ITEMS CASCADE");
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
}

