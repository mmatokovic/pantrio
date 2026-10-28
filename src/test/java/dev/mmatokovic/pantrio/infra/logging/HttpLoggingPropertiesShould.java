package dev.mmatokovic.pantrio.infra.logging;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HttpLoggingPropertiesShould {

    @Test
    void bind_applyDefaults_whenNothingIsConfigured() {
        var properties = new Binder(new MapConfigurationPropertySource(Map.of()))
            .bindOrCreate("pantrio.logging.http", HttpLoggingProperties.class);

        assertThat(properties).isEqualTo(new HttpLoggingProperties(
            8192,
            List.of("actuator", "swagger-ui", "v3/api-docs"),
            Set.of("authorization", "proxy-authorization", "cookie", "set-cookie")));
    }

    @Test
    void construct_keepConfiguredValuesAndLowercaseHeaders() {
        var properties = new HttpLoggingProperties(100, List.of("health"), Set.of("X-Api-Key"));

        assertThat(properties).isEqualTo(new HttpLoggingProperties(100, List.of("health"), Set.of("x-api-key")));
    }
}
