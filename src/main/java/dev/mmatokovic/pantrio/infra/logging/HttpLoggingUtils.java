package dev.mmatokovic.pantrio.infra.logging;

import dev.mmatokovic.pantrio.infra.jackson.ObjectMapperUtils;
import jakarta.annotation.Nullable;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

public final class HttpLoggingUtils {

    public static final int MAX_BODY_BYTES = 1024 * 1024; // 1 MB
    private static final int MAX_LOG_BYTES = 8 * 1024; // 8 KB
    private static final Set<String> BINARY_TYPES = Set.of("image", "video", "audio");

    private HttpLoggingUtils() {
        throw new AssertionError("Utility class!");
    }

    public static String extractLoggableBody(@Nullable String contentType, byte[] content) {
        if (!isLoggableContent(contentType)) {
            return null;
        }
        return extractBody(content);
    }

    public static String extractLoggableBody(@Nullable MediaType contentType, byte[] content) {
        if (!isLoggableContent(contentType)) {
            return null;
        }
        return extractBody(content);
    }

    public static boolean isLoggableContent(@Nullable String rawContentType) {
        if (rawContentType == null) {
            return false;
        }
        try {
            return isLoggableContent(MediaType.parseMediaType(rawContentType));
        } catch (InvalidMediaTypeException e) {
            return false;
        }
    }

    public static boolean isLoggableContent(@Nullable MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }
        return MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)
            || MediaType.TEXT_PLAIN.isCompatibleWith(mediaType);
    }

    public static boolean isBinaryContent(@Nullable MediaType contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.equals(MediaType.APPLICATION_OCTET_STREAM)
            || contentType.equals(MediaType.APPLICATION_PDF)
            || BINARY_TYPES.contains(contentType.getType());
    }

    private static String extractBody(byte[] raw) {
        if (raw == null || raw.length == 0) {
            return "[empty]";
        }
        boolean truncated = raw.length > MAX_LOG_BYTES;
        byte[] slice = truncated ? Arrays.copyOf(raw, MAX_LOG_BYTES) : raw;
        String body;
        try {
            JsonNode root = ObjectMapperUtils.readTree(slice);
            body = ObjectMapperUtils.writeJson(root);
        } catch (Exception e) {
            body = new String(slice, StandardCharsets.UTF_8).trim();
        }
        return body;
    }
}
