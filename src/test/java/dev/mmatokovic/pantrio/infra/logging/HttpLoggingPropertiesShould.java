package dev.mmatokovic.pantrio.infra.logging;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HttpLoggingPropertiesShould {

    @Test
    void construct_applyDefaults_whenNothingIsConfigured() {
        var properties = new HttpLoggingProperties(0, null, null);

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
