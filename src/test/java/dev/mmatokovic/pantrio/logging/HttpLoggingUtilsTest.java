package dev.mmatokovic.pantrio.logging;

import dev.mmatokovic.pantrio.infra.logging.HttpLoggingUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class HttpLoggingUtilsTest {

    @ParameterizedTest
    @CsvSource({"POST,application/json", "PUT,application/json", "PATCH,application/json", "POST,text/plain", "PUT,text/plain", "PATCH,text/plain"})
    void shouldReturnTrue_whenLogRequestBody_withValidHttpMethod_andValidContentType(String httpMethod, String contentType) {
        assertThat(HttpLoggingUtils.shouldLogRequestBody(httpMethod, contentType)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "DELETE"})
    void shouldReturnFalse_whenLogRequestBody_withInvalidHttpMethod_andValidContentType(String httpMethod) {
        assertThat(HttpLoggingUtils.shouldLogRequestBody(httpMethod, MediaType.APPLICATION_JSON)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenLogRequestBody_withValidHttpMethod_andInvalidContentType() {
        assertThat(HttpLoggingUtils.shouldLogRequestBody(HttpMethod.POST.name(), MediaType.MULTIPART_FORM_DATA)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenLogRequestBody_forUnparsableMediaType() {
        assertThat(HttpLoggingUtils.shouldLogRequestBody(HttpMethod.POST.name(), "application/unparsable")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/json", "text/plain"})
    void shouldReturnTrue_whenLogResponseBody_withValidContentType(String contentType) {
        assertThat(HttpLoggingUtils.shouldLogResponseBody(contentType)).isTrue();
    }

    @Test
    void shouldReturnFalse_whenLogResponseBody_withInvalidContentType() {
        assertThat(HttpLoggingUtils.shouldLogResponseBody(MediaType.APPLICATION_OCTET_STREAM)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenLogResponseBody_forUnparsableMediaType() {
        assertThat(HttpLoggingUtils.shouldLogResponseBody("application/unparsable")).isFalse();
    }

    @Test
    void shouldReturnNormalizedString() {
        assertThat(HttpLoggingUtils.normalizeBody("  test   test2 \n \r   test3    ".getBytes())).isEqualTo("test test2 test3");
    }
}
