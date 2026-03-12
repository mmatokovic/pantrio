package dev.mmatokovic.pantrio.infra.logging;

import dev.mmatokovic.pantrio.infra.jackson.ObjectMapperUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;

public final class HttpLoggingUtils {

    public static final String LOG_ITEM_DELIMITER = ", ";
    public static final int INITIAL_SB_SIZE = 512;

    private HttpLoggingUtils() {
        throw new AssertionError("Utility class!");
    }

    public static boolean shouldLogRequestBody(String httpMethod, String contentType) {
        try {
            return shouldLogRequestBody(httpMethod, MediaType.parseMediaType(contentType));
        } catch (InvalidMediaTypeException e) {
            return false;
        }
    }

    public static boolean shouldLogRequestBody(String httpMethod, MediaType mediaType) {
        final var isValidHttpMethod = !HttpMethod.GET.matches(httpMethod) && !HttpMethod.DELETE.matches(httpMethod);
        final var isValidContentType = isContentTypeForLogging(mediaType);

        return isValidHttpMethod && isValidContentType;
    }

    public static boolean shouldLogResponseBody(String contentType) {
        try {
            return shouldLogResponseBody(MediaType.parseMediaType(contentType));
        } catch (InvalidMediaTypeException e) {
            return false;
        }
    }

    public static boolean shouldLogResponseBody(MediaType mediaType) {
        return isContentTypeForLogging(mediaType);
    }

    private static boolean isContentTypeForLogging(MediaType mediaType) {
        return MediaType.APPLICATION_JSON.isCompatibleWith(mediaType) || MediaType.TEXT_PLAIN.isCompatibleWith(mediaType);
    }

    public static String normalizeBody(byte[] rawBody) {
        try {
            JsonNode root = ObjectMapperUtils.readTree(rawBody);
            return ObjectMapperUtils.writeJson(root);
        } catch (Exception e) {
            return new String(rawBody, StandardCharsets.UTF_8).trim();
        }
    }
}
