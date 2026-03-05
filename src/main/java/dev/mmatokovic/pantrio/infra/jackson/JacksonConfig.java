package dev.mmatokovic.pantrio.infra.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
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
            .changeDefaultPropertyInclusion(include -> include.withValueInclusion(JsonInclude.Include.NON_NULL))
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
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
