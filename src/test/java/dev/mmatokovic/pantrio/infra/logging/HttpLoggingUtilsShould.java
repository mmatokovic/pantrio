package dev.mmatokovic.pantrio.infra.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class HttpLoggingUtilsShould {

    @ParameterizedTest
    @ValueSource(strings = {"application/json", "text/plain"})
    void isLoggableContent_returnTrueForLoggableContentTypes(String contentType) {
        assertThat(HttpLoggingUtils.isLoggableContent(contentType)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"multipart/form-data", "application/octet-stream", "image/png"})
    void isLoggableContent_returnFalseForNonLoggableContentTypes(String contentType) {
        assertThat(HttpLoggingUtils.isLoggableContent(contentType)).isFalse();
    }

    @Test
    void isLoggableContent_returnFalseForNull() {
        assertThat(HttpLoggingUtils.isLoggableContent((String) null)).isFalse();
    }

    @Test
    void isLoggableContent_returnFalseForUnparsableMediaType() {
        assertThat(HttpLoggingUtils.isLoggableContent("not/a/valid/type")).isFalse();
    }

    @Test
    void isLoggableContent_returnTrueForApplicationJson() {
        assertThat(HttpLoggingUtils.isLoggableContent(MediaType.APPLICATION_JSON)).isTrue();
    }

    @Test
    void isLoggableContent_returnTrueForTextPlain() {
        assertThat(HttpLoggingUtils.isLoggableContent(MediaType.TEXT_PLAIN)).isTrue();
    }

    @Test
    void isLoggableContent_returnFalseForNullMediaType() {
        assertThat(HttpLoggingUtils.isLoggableContent((MediaType) null)).isFalse();
    }

    @Test
    void isLoggableContent_returnFalseForOctetStream() {
        assertThat(HttpLoggingUtils.isLoggableContent(MediaType.APPLICATION_OCTET_STREAM)).isFalse();
    }

    @Test
    void normalizeBody_compactJson() {
        var body = "{  \"key\"  :  \"value\" }".getBytes(UTF_8);

        var normalized = HttpLoggingUtils.normalizeBody(body, "application/json", 100);

        assertThat(normalized).isEqualTo("{\"key\":\"value\"}");
    }

    @Test
    void normalizeBody_returnRawText_whenJsonIsMalformed() {
        var body = " {\"key\": ".getBytes(UTF_8);

        var normalized = HttpLoggingUtils.normalizeBody(body, "application/json", 100);

        assertThat(normalized).isEqualTo("{\"key\":");
    }

    @Test
    void normalizeBody_truncate_whenBodyIsLongerThanMax() {
        var body = "hello world".getBytes(UTF_8);

        var normalized = HttpLoggingUtils.normalizeBody(body, "text/plain", 5);

        assertThat(normalized).isEqualTo("hello...[truncated]");
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/octet-stream", "application/pdf", "image/png", "video/mp4", "audio/mpeg"})
    void isBinaryContent_returnTrueForBinaryMediaTypes(String contentType) {
        assertThat(HttpLoggingUtils.isBinaryContent(MediaType.parseMediaType(contentType))).isTrue();
    }

    @Test
    void isBinaryContent_returnFalseForApplicationJson() {
        assertThat(HttpLoggingUtils.isBinaryContent(MediaType.APPLICATION_JSON)).isFalse();
    }

    @Test
    void isBinaryContent_returnFalseForNull() {
        assertThat(HttpLoggingUtils.isBinaryContent(null)).isFalse();
    }
}
