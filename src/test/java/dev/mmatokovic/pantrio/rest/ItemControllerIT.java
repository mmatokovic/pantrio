package dev.mmatokovic.pantrio.rest;

import dev.mmatokovic.pantrio.IntegrationTest;
import dev.mmatokovic.pantrio.item.ItemController;
import dev.mmatokovic.pantrio.item.dto.ItemResponse;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = RANDOM_PORT) // starts a full Tomcat in memory
@Import(ItemController.class)
class ItemControllerIT extends IntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE PANTRIO_TEST.ITEMS CASCADE");

        RestAssured.basePath = "/items";
        RestAssured.port = port;
    }

    @Test
    void should_return_single_item() {
        jdbcTemplate.update(
            "INSERT INTO items (id, name) VALUES (1, 'string')"
        );

        ItemResponse itemResponse = RestAssured.given()
            .auth().basic("user", "userpwd")
            .accept(APPLICATION_JSON_VALUE)
            .when().get("/1")
            .then()
            .statusCode(OK.value())
            .contentType(APPLICATION_JSON_VALUE)
            .extract()
            .as(ItemResponse.class);

        assertThat(itemResponse)
            .isNotNull()
            .extracting(ItemResponse::name).isEqualTo("string");
    }
}
