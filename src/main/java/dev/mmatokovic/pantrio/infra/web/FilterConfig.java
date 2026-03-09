package dev.mmatokovic.pantrio.infra.web;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> etagFilter() {
        ShallowEtagHeaderFilter etagFilter = new ShallowEtagHeaderFilter();
        // ETag that start with "W/..." allows Tomcat gzip compression to work
        etagFilter.setWriteWeakETag(true);

        FilterRegistrationBean<ShallowEtagHeaderFilter> filter = new FilterRegistrationBean<>(etagFilter);
        filter.addUrlPatterns("/*");
        return filter;
    }
}
