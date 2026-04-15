package dev.mmatokovic.pantrio.infra.rest;

import dev.mmatokovic.pantrio.infra.openfoodfacts.OpenFoodFactsProperties;
import dev.mmatokovic.pantrio.infra.logging.RestClientRequestLogger;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
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
    RestClient openFoodFactsClient() {
        return RestClient.builder()
            .baseUrl(properties.host())
            .requestFactory(customFactory())
            .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
            .defaultStatusHandler(new RestClientErrorHandler())
            .requestInterceptor(new RestClientRequestLogger())
            .build();
    }

    @Bean
    RestClient openFoodFactsImageClient() {
        return RestClient.builder()
            .baseUrl(properties.imageHost())
            .requestFactory(customFactory())
            .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
            .defaultStatusHandler(new RestClientErrorHandler())
            .requestInterceptor(new RestClientRequestLogger())
            .build();
    }

    private ClientHttpRequestFactory customFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(5));
        factory.setConnectTimeout(Duration.ofSeconds(10));
        return factory;
    }
}
