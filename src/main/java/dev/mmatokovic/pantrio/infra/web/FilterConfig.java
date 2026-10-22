package dev.mmatokovic.pantrio.infra.web;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@Configuration(proxyBeanMethods = false)
class FilterConfig {

    @Bean
    FilterRegistrationBean<ShallowEtagHeaderFilter> etagFilter() {
        var etagFilter = new ShallowEtagHeaderFilter();
        // A weak ETag ("W/...") still lets Tomcat gzip the response
        etagFilter.setWriteWeakETag(true);

        var filter = new FilterRegistrationBean<>(etagFilter);
        filter.addUrlPatterns("/*");
        return filter;
    }
}
