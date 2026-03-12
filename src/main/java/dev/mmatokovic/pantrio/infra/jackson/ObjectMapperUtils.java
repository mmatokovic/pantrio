package dev.mmatokovic.pantrio.infra.jackson;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Used as central point for configuring and accessing a shared, read-only {@link ObjectMapper}.
 */
public final class ObjectMapperUtils {

    private static final ObjectMapper MAPPER = JacksonConfig.buildMapper();

    private ObjectMapperUtils() {
        throw new AssertionError("Utility class!");
    }

    @SuppressWarnings("unused")
    public static <T> T readValue(byte[] value, Class<T> valueType) {
        return MAPPER.readValue(value, valueType);
    }

    @SuppressWarnings("unused")
    public static <T> T readValue(String value, Class<T> valueType) {
        return MAPPER.readValue(value, valueType);
    }

    public static String writeJson(Object value) {
        return MAPPER.writeValueAsString(value);
    }

    public static JsonNode readTree(final byte[] object) {
        return MAPPER.readTree(object);
    }
}
