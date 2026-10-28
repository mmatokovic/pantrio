package dev.mmatokovic.pantrio.infra.logging;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class HttpLoggingConfig {

    private static final int BEFORE_SPRING_SECURITY = -101;

    @Bean
    FilterRegistrationBean<HttpLoggingFilter> httpLoggingFilter(HttpLoggingProperties httpLoggingProperties) {
        var filter = new FilterRegistrationBean<>(new HttpLoggingFilter(httpLoggingProperties));
        filter.setOrder(BEFORE_SPRING_SECURITY);
        return filter;
    }

    @Bean
    RestClientRequestLogger restClientRequestLogger(HttpLoggingProperties httpLoggingProperties) {
        return new RestClientRequestLogger(httpLoggingProperties);
    }
}
