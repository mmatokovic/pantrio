package dev.mmatokovic.pantrio.contract;

import dev.mmatokovic.pantrio.BaseIntegrationTest;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.openapitools.openapidiff.core.OpenApiCompare;
import org.openapitools.openapidiff.core.output.MarkdownRender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ContractFreezeIT extends BaseIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Value("classpath:/openapi.json")
    private Resource expectedOpenApi;

    @Test
    void apiDocs_staysCompatibleWithFrozenContract() throws IOException {
        var actualOpenApiJson = prettify(RestAssured.given()
            .auth().basic(USER_NAME, USER_PWD)
            .when()
            .get("/v3/api-docs")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().asString());
        var expectedOpenApiJson = prettify(IOUtils.toString(expectedOpenApi.getInputStream(), UTF_8).replace(":8080", ""));

        var diff = OpenApiCompare.fromContents(expectedOpenApiJson, actualOpenApiJson);

        if (!diff.isCompatible()) {
            var markdown = new ByteArrayOutputStream();
            new MarkdownRender().render(diff, new OutputStreamWriter(markdown, UTF_8));
            log.error("OpenAPI contract diff:\n{}", markdown.toString(UTF_8));
        }
        assertThat(diff.isCompatible())
            .describedAs("Exposed OpenAPI must stay compatible with src/test/resources/openapi.json")
            .isTrue();
    }

    private String prettify(String json) {
        return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMapper.readValue(json, Map.class));
    }
}
