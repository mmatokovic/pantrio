package dev.mmatokovic.pantrio.infra.jackson;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration(proxyBeanMethods = false)
public class JacksonConfig {

    public static JsonMapper buildMapper() {
        return JsonMapper.builder()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                     DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .findAndAddModules()
            .build();
    }

    @Bean
    JsonMapper jacksonJsonMapper() {
        return buildMapper();
    }
}
