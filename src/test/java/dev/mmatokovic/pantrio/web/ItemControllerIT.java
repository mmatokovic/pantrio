package dev.mmatokovic.pantrio.web;

import dev.mmatokovic.pantrio.item.ItemController;
import dev.mmatokovic.pantrio.item.record.ItemResponse;
import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import dev.mmatokovic.pantrio.TestcontainersConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestcontainersConfiguration.class, ItemController.class})
class ItemControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE PANTRIO_TEST.ITEMS CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE PANTRIO_TEST.STORAGE CASCADE");

        RestAssured.basePath = "/items";
        RestAssured.port = port;
    }

    @Test
    void should_return_single_item() {
        jdbcTemplate.update(
            "INSERT INTO items (id, name) VALUES (1, 'string')"
        );

        Response response = RestAssured.given()
            .auth().basic("user", "userpwd")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when().get("/1")
            .then().log().ifValidationFails(LogDetail.ALL)
            .extract().response();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        ItemResponse itemResponse = response.getBody().as(ItemResponse.class);
        assertThat(itemResponse).isNotNull();
        assertThat(itemResponse.name()).isEqualTo("string");
    }
}
