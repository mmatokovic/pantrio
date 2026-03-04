package dev.mmatokovic.pantrio.infra.logging;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

public final class HttpLoggingUtils {

    public static final String LOG_ITEM_DELIMITER = ", ";
    public static final int INITIAL_SB_SIZE = 512;
    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        final var javaTimeModule = new JavaTimeModule();
        MAPPER.registerModule(javaTimeModule);
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

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
            Object json = MAPPER.readValue(rawBody, Object.class);
            String jsonString = MAPPER.writeValueAsString(json);

            return StringUtils.normalizeSpace(jsonString.replace("\n", "").replace("\r", ""));
        } catch (Exception e) {
            String text = new String(rawBody, StandardCharsets.UTF_8);

            return StringUtils.normalizeSpace(text.replace("\n", "").replace("\r", ""));
        }
    }
}
