package dev.mmatokovic.pantrio.infra.rest;

import dev.mmatokovic.pantrio.infra.logging.RestClientRequestLogger;
import dev.mmatokovic.pantrio.infra.openfoodfacts.OpenFoodFactsProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
class RestClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    @Bean
    RestClient openFoodFactsClient(RestClient.Builder restClientBuilder,
                                   OpenFoodFactsProperties openFoodFactsProperties,
                                   RestClientRequestLogger restClientRequestLogger,
                                   @Value("${spring.application.name}") String appName,
                                   @Value("${info.app.version}") String appVersion) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        return restClientBuilder
            .baseUrl(openFoodFactsProperties.host())
            .requestFactory(requestFactory)
            .defaultHeader(HttpHeaders.USER_AGENT, appName + "/" + appVersion)
            .requestInterceptor(restClientRequestLogger)
            .build();
    }
}
