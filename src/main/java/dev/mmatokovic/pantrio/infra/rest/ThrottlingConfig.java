package dev.mmatokovic.pantrio.infra.rest;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Semaphore;

@EnableAsync
@Configuration
public class ThrottlingConfig {

    /*
     * Product read endpoints
     * Limit: 100 req/min
     */
    @Bean
    public RateLimiter openFoodFactsProductRateLimiter() {
        return RateLimiter.create(100d / 60d);
    }

    /**
     * Safety net for concurrent calls.
     * Limit: 5 concurrent calls.
     *
     * @deprecated Use Spring Boot 4's declarative concurrency control instead:
     * @ConcurrencyLimit(5) on the method or bean.
     */
    @Bean
    @Deprecated
    public Semaphore openFoodFactsSemaphore() {
        return new Semaphore(5, true);
    }
}
