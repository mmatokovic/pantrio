package dev.mmatokovic.pantrio.infra.logging;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;

public final class HttpLoggingUtils {

    // Only compacts raw JSON (tree in, tree out), which no setting of JacksonConfig affects
    private static final JsonMapper MAPPER = JsonMapper.shared();
    private static final Set<String> BINARY_TYPES = Set.of("image", "video", "audio");

    private HttpLoggingUtils() {
        throw new AssertionError("Utility class!");
    }

    public static boolean isLoggableContent(@Nullable String contentType) {
        return isLoggableContent(parseMediaType(contentType));
    }

    public static boolean isLoggableContent(@Nullable MediaType mediaType) {
        return isJson(mediaType) || MediaType.TEXT_PLAIN.isCompatibleWith(mediaType);
    }

    public static boolean isBinaryContent(@Nullable MediaType mediaType) {
        return mediaType != null
            && (mediaType.equals(MediaType.APPLICATION_OCTET_STREAM)
                || mediaType.equals(MediaType.APPLICATION_PDF)
                || BINARY_TYPES.contains(mediaType.getType()));
    }

    static boolean shouldLogRequestBody(String httpMethod, @Nullable String contentType) {
        return !HttpMethod.GET.matches(httpMethod)
            && !HttpMethod.DELETE.matches(httpMethod)
            && isLoggableContent(contentType);
    }

    static boolean shouldLogResponseBody(@Nullable String contentType) {
        return isLoggableContent(contentType);
    }

    static String normalizeBody(byte[] rawBody, @Nullable String contentType, int maxBodyBytes) {
        var mediaType = parseMediaType(contentType);
        if (isJson(mediaType)) {
            return normalizeJson(rawBody, maxBodyBytes);
        }
        var text = new String(rawBody, StandardCharsets.UTF_8).trim();
        return text.length() > maxBodyBytes
            ? text.substring(0, maxBodyBytes) + "...[truncated]"
            : text;
    }

    private static String normalizeJson(byte[] rawBody, int maxLength) {
        try {
            var json = MAPPER.readTree(rawBody).toString();
            return json.length() > maxLength ? json.substring(0, maxLength) + "...[truncated]" : json;
        } catch (RuntimeException e) {
            var raw = new String(rawBody, StandardCharsets.UTF_8).trim();
            return raw.length() > maxLength ? raw.substring(0, maxLength) + "...[truncated]" : raw;
        }
    }

    private static boolean isJson(@Nullable MediaType mediaType) {
        return mediaType != null && (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)
            || Objects.equals(mediaType.getSubtypeSuffix(), "json"));
    }

    private static @Nullable MediaType parseMediaType(@Nullable String contentType) {
        if (contentType == null) {
            return null;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
