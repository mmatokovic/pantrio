package dev.mmatokovic.pantrio.infra.rest;

import dev.mmatokovic.pantrio.infra.logging.RestClientRequestLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    private final String userAgent;

    public RestClientConfig(
        @Value("${spring.application.name}") String appName,
        @Value("${info.app.version}") String appVersion
    ) {
        this.userAgent = "%s/%s".formatted(appName, appVersion);
    }

    @Bean
    RestClient openFoodFactsRestClient() {
        return RestClient.builder()
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
            .requestInterceptor(new RestClientRequestLogger())
            .build();
    }
}
