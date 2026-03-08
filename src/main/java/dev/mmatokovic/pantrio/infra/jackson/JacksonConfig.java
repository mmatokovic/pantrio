package dev.mmatokovic.pantrio.infra.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration(proxyBeanMethods = false)
public class JacksonConfig {

    public static JsonMapper buildMapper() {
        return JsonMapper.builder()
            // Excludes null fields from JSON output — keeps responses clean
            .changeDefaultPropertyInclusion(include -> include.withValueInclusion(JsonInclude.Include.NON_NULL))
            // Don't fail on empty beans
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            // Or ignore unknown properties (lenient mode)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                     DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
            // Accept case-insensitive enums
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            // Allows a single value where an array is expected "tag" and ["tag"] are both accepted for List<String>
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            // Accept empty strings as null for objects
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            // Maps camelCase Java fields to snake_case JSON
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .addModule(new SimpleModule()
                .addDeserializer(LocalDate.class, new TimestampDateDeserializer())
                .addDeserializer(LocalDateTime.class, new TimestampDateTimeDeserializer()))
            .findAndAddModules()
            .build();
    }

    @Bean
    JsonMapper jacksonJsonMapper() {
        return buildMapper();
    }
}
