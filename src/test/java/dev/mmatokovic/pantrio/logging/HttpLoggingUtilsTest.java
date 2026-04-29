package dev.mmatokovic.pantrio.logging;

import dev.mmatokovic.pantrio.infra.logging.HttpLoggingUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class HttpLoggingUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {"application/json", "text/plain"})
    void isLoggableContent_returnsTrueForLoggableContentTypes(String contentType) {
        assertThat(HttpLoggingUtils.isLoggableContent(contentType)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"multipart/form-data", "application/octet-stream", "image/png"})
    void isLoggableContent_returnsFalseForNonLoggableContentTypes(String contentType) {
        assertThat(HttpLoggingUtils.isLoggableContent(contentType)).isFalse();
    }

    @Test
    void isLoggableContent_returnsFalseForNull() {
        assertThat(HttpLoggingUtils.isLoggableContent((String) null)).isFalse();
    }

    @Test
    void isLoggableContent_returnsFalseForUnparsableMediaType() {
        assertThat(HttpLoggingUtils.isLoggableContent("not/a/valid/type")).isFalse();
    }

    @Test
    void isLoggableContent_returnsTrueForApplicationJson() {
        assertThat(HttpLoggingUtils.isLoggableContent(MediaType.APPLICATION_JSON)).isTrue();
    }

    @Test
    void isLoggableContent_returnsTrueForTextPlain() {
        assertThat(HttpLoggingUtils.isLoggableContent(MediaType.TEXT_PLAIN)).isTrue();
    }

    @Test
    void isLoggableContent_returnsFalseForNullMediaType() {
        assertThat(HttpLoggingUtils.isLoggableContent((MediaType) null)).isFalse();
    }

    @Test
    void isLoggableContent_returnsFalseForOctetStream() {
        assertThat(HttpLoggingUtils.isLoggableContent(MediaType.APPLICATION_OCTET_STREAM)).isFalse();
    }

    @Test
    void isBinaryContent_returnsTrueForOctetStream() {
        assertThat(HttpLoggingUtils.isBinaryContent(MediaType.APPLICATION_OCTET_STREAM)).isTrue();
    }

    @Test
    void isBinaryContent_returnsTrueForPdf() {
        assertThat(HttpLoggingUtils.isBinaryContent(MediaType.APPLICATION_PDF)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/png", "image/jpeg", "video/mp4", "audio/mpeg"})
    void isBinaryContent_returnsTrueForMediaTypes(String contentType) {
        assertThat(HttpLoggingUtils.isBinaryContent(MediaType.parseMediaType(contentType))).isTrue();
    }

    @Test
    void isBinaryContent_returnsFalseForNull() {
        assertThat(HttpLoggingUtils.isBinaryContent(null)).isFalse();
    }

    @Test
    void isBinaryContent_returnsFalseForApplicationJson() {
        assertThat(HttpLoggingUtils.isBinaryContent(MediaType.APPLICATION_JSON)).isFalse();
    }

    @Test
    void extractLoggableBody_returnsNullForNonLoggableContentType() {
        assertThat(HttpLoggingUtils.extractLoggableBody("image/png", "{}".getBytes())).isNull();
    }

    @Test
    void extractLoggableBody_returnsNullForNullContentType() {
        assertThat(HttpLoggingUtils.extractLoggableBody((String) null, "{}".getBytes())).isNull();
    }

    @Test
    void extractLoggableBody_returnsEmptyMarkerForEmptyContent() {
        assertThat(HttpLoggingUtils.extractLoggableBody("application/json", new byte[0])).isEqualTo("[empty]");
    }

    @Test
    void extractLoggableBody_normalizesValidJson() {
        assertThat(HttpLoggingUtils.extractLoggableBody("application/json", "{  \"key\"  :  \"value\"  }".getBytes()))
            .isEqualTo("{\"key\":\"value\"}");
    }

    @Test
    void extractLoggableBody_returnsRawTextForNonJsonBody() {
        assertThat(HttpLoggingUtils.extractLoggableBody("text/plain", "hello world".getBytes()))
            .isEqualTo("hello world");
    }

    @Test
    void extractLoggableBody_returnsNullForNonLoggableMediaType() {
        assertThat(HttpLoggingUtils.extractLoggableBody(MediaType.APPLICATION_OCTET_STREAM, "{}".getBytes())).isNull();
    }

    @Test
    void extractLoggableBody_returnsNullForNullMediaType() {
        assertThat(HttpLoggingUtils.extractLoggableBody((MediaType) null, "{}".getBytes())).isNull();
    }

    @Test
    void extractLoggableBody_normalizesJsonForMediaTypeOverload() {
        assertThat(HttpLoggingUtils.extractLoggableBody(MediaType.APPLICATION_JSON, "{  \"key\"  :  \"value\"  }".getBytes()))
            .isEqualTo("{\"key\":\"value\"}");
    }
}
