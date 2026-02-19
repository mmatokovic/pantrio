package dev.mmatokovic.pantrio.infra.rest;

import dev.mmatokovic.pantrio.infra.openfoodfacts.OpenFoodFactsProperties;
import dev.mmatokovic.pantrio.infra.logging.RestClientRequestLogger;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.client.RestClient;

@Configuration
@EnableMethodSecurity
public class RestClientConfig {

    private final OpenFoodFactsProperties properties;
    private final String userAgent;

    public RestClientConfig(OpenFoodFactsProperties properties, BuildProperties buildProperties) {
        this.properties = properties;
        this.userAgent = "%s/%s".formatted(
            buildProperties.getName(),
            buildProperties.getVersion()
        );
    }

    @Bean
    RestClient openFoodFactsRestClient() {
        return RestClient.builder()
            .baseUrl(properties.host())
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
            .defaultStatusHandler(new RestClientErrorHandler())
            .requestInterceptor(new RestClientRequestLogger())
            .build();
    }
}
