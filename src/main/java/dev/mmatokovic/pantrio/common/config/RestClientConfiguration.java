package dev.mmatokovic.pantrio.common.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    private final String appName;

    public RestClientConfiguration(ApplicationContext applicationContext) {
        this.appName = applicationContext.getApplicationName();
    }

    @Bean
    RestClient.Builder openFoodFactsRestClient() {
        return RestClient.builder()
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, appName );
    }
}
