package dev.mmatokovic.pantrio.infra.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Semaphore;

@EnableAsync
@Configuration
public class AsyncConfig {

    /**
     * Product read endpoints
     * Limit: 100 req/min
     */
    @Bean
    public RateLimiter openFoodFactsProductRateLimiter() {
        return RateLimiter.create(100d / 60d);
    }

    /**
     * Safety net for concurrent calls
     */
    @Bean
    public Semaphore openFoodFactsSemaphore() {
        return new Semaphore(5, true);
    }
}
