package dev.mmatokovic.pantrio.infra.logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class HttpLoggingConfig {

    @Bean
    HttpLoggingFilter httpLoggingFilter(HttpLoggingProperties httpLoggingProperties) {
        return new HttpLoggingFilter(httpLoggingProperties);
    }

    @Bean
    RestClientRequestLogger restClientRequestLogger(HttpLoggingProperties httpLoggingProperties) {
        return new RestClientRequestLogger(httpLoggingProperties);
    }
}
