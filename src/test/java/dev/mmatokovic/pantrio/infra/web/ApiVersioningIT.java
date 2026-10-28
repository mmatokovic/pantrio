package dev.mmatokovic.pantrio.infra.web;

import dev.mmatokovic.pantrio.BaseIntegrationTest;
import dev.mmatokovic.pantrio.infra.error.ErrorResponse;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ApiVersioningIT extends BaseIntegrationTest {

    @Test
    void listItems_returns400_whenVersionIsNotSupported() {
        ErrorResponse response = RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/api/v2/items")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract().as(ErrorResponse.class);

        assertThat(response).isEqualTo(new ErrorResponse(400, "Invalid API version: '2.0.0'.", null));
    }

    @Test
    void listItems_returns404_whenPathHasNoVersion() {
        RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/items")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void apiDocs_staysUnversioned() {
        RestAssured.given()
            .when()
            .get("/v3/api-docs")
            .then()
            .statusCode(HttpStatus.OK.value());
    }
}
