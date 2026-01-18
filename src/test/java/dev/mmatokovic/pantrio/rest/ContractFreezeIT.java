package dev.mmatokovic.pantrio.rest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.openapitools.openapidiff.core.OpenApiCompare;
import org.openapitools.openapidiff.core.model.ChangedOpenApi;
import org.openapitools.openapidiff.core.output.MarkdownRender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.test.context.support.WithMockUser;
import dev.mmatokovic.pantrio.BaseIntegrationTest;
import org.springframework.test.web.servlet.client.RestTestClient;
import tools.jackson.databind.ObjectMapper;

import java.io.OutputStreamWriter;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@WithMockUser(roles = "ADMIN")
class ContractFreezeIT extends BaseIntegrationTest {

    @Autowired
    private RestTestClient rest;

    @Autowired
    private ObjectMapper jackson;

    @Value("classpath:/openapi.json") // extracted from deploy
    private Resource expectedOpenAPI;

    @Test
    void my_contract_did_not_change() throws Exception {

        String actualOpenAPIJson = prettifyJsonString(
            rest.get()
                .uri("/v3/api-docs")
                .headers(headers -> headers.setBasicAuth("user", "userpwd"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody()
        );

        String expectedOpenAPIJson = prettifyJsonString(
            IOUtils.toString(expectedOpenAPI.getInputStream(), UTF_8)
                .replace(":8080", "") // hack the extracted port
        );

        ChangedOpenApi diff =
            OpenApiCompare.fromContents(expectedOpenAPIJson, actualOpenAPIJson);

        if (!diff.isCompatible()) {
            MarkdownRender markdownRender = new MarkdownRender();
            markdownRender.render(diff, new OutputStreamWriter(System.err));

            assertThat(actualOpenAPIJson)
                .describedAs("Exposed OpenAPI should not have changed")
                .isEqualTo(expectedOpenAPIJson);
        }
    }

    private String prettifyJsonString(String raw) throws JsonProcessingException {
        if (StringUtils.isBlank(raw)) return raw;
        return jackson.writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                jackson.readValue(raw, Map.class)
            );
    }
}
