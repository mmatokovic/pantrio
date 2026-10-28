package dev.mmatokovic.pantrio.infra.rest;

import dev.mmatokovic.pantrio.infra.logging.RestClientRequestLogger;
import dev.mmatokovic.pantrio.infra.openfoodfacts.OpenFoodFactsProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
class RestClientConfig {

    @Bean
    RestClient openFoodFactsClient(RestClient.Builder restClientBuilder,
                                   OpenFoodFactsProperties openFoodFactsProperties,
                                   RestClientRequestLogger restClientRequestLogger,
                                   @Value("${spring.application.name}") String appName,
                                   @Value("${info.app.version}") String appVersion) {
        var requestFactory = ClientHttpRequestFactoryBuilder.detect()
            .build(HttpClientSettings.defaults().withTimeouts(openFoodFactsProperties.connectTimeout(), openFoodFactsProperties.readTimeout()));
        return restClientBuilder
            .baseUrl(openFoodFactsProperties.host())
            .requestFactory(requestFactory)
            .defaultHeader(HttpHeaders.USER_AGENT, appName + "/" + appVersion)
            .requestInterceptor(restClientRequestLogger)
            .build();
    }
}
