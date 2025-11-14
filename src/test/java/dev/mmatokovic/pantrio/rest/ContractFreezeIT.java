package dev.mmatokovic.pantrio.rest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvc;
import dev.mmatokovic.pantrio.IntegrationTest;

import java.io.OutputStreamWriter;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WithMockUser(roles = "ADMIN")
public class ContractFreezeIT extends IntegrationTest {
    @Autowired
    MockMvc mockMvc;
    ObjectMapper jackson = new ObjectMapper();

    @Value("classpath:/openapi.json") // extracted from deploy
    Resource expectedOpenAPI;

    @Test
    void my_contract_did_not_change() throws Exception {
        String actualOpenAPIJson = prettifyJsonString(
            mockMvc.perform(get("/v3/api-docs"))
                .andReturn().getResponse().getContentAsString());

        String expectedOpenAPIJson = prettifyJsonString(
            IOUtils.toString(expectedOpenAPI.getInputStream(), UTF_8)
                .replace(":8080", "")); // hack the extracted port

        ChangedOpenApi diff = OpenApiCompare.fromContents(expectedOpenAPIJson, actualOpenAPIJson);

        if (!diff.isCompatible()) {
            MarkdownRender markdownRender = new MarkdownRender();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(System.err);
            markdownRender.render(diff, outputStreamWriter);

            assertThat(actualOpenAPIJson)
                .describedAs("Exposed OpenAPI should not have changed")
                .isEqualTo(expectedOpenAPIJson);
        }
    }

    private String prettifyJsonString(String rawJson) throws JsonProcessingException {
        return StringUtils.isBlank(rawJson) ? rawJson :
            jackson.writerWithDefaultPrettyPrinter().writeValueAsString(
                jackson.readValue(rawJson, Map.class));
    }
}
