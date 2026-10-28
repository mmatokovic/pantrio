package dev.mmatokovic.pantrio.infra.rest;

import dev.mmatokovic.pantrio.infra.config.AppInfoProperties;
import dev.mmatokovic.pantrio.infra.logging.RestClientRequestLogger;
import dev.mmatokovic.pantrio.infra.openfoodfacts.OpenFoodFactsProperties;
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
                                   AppInfoProperties appInfoProperties) {
        var requestFactory = ClientHttpRequestFactoryBuilder.detect()
            .build(HttpClientSettings.defaults().withTimeouts(openFoodFactsProperties.connectTimeout(), openFoodFactsProperties.readTimeout()));
        return restClientBuilder
            .baseUrl(openFoodFactsProperties.host())
            .requestFactory(requestFactory)
            .defaultHeader(HttpHeaders.USER_AGENT, appInfoProperties.name() + "/" + appInfoProperties.version())
            .requestInterceptor(restClientRequestLogger)
            .build();
    }
}
