package dev.mmatokovic.pantrio.infra.job;

import dev.mmatokovic.pantrio.domain.item.Item;
import dev.mmatokovic.pantrio.domain.item.ItemRepository;
import dev.mmatokovic.pantrio.domain.item.UnitEnum;
import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.database.Order;
import org.springframework.batch.infrastructure.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.batch.autoconfigure.BatchTaskExecutor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestClientException;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static dev.mmatokovic.pantrio.infra.job.QuartzConfig.JOB_NAME;

@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class BatchConfig {

    private static final int CHUNK_SIZE = 10;
    private static final int PAGE_SIZE = 100;
    private static final int SKIP_LIMIT = 20;

    private final JobRepository jobRepository;

    @Bean
    @StepScope
    JdbcPagingItemReader<Long> reader(DataSource dataSource) throws Exception {
        return new JdbcPagingItemReaderBuilder<Long>()
            .name("itemReader")
            .dataSource(dataSource)
            .selectClause("SELECT id")
            .fromClause("FROM items")
            .whereClause("WHERE barcode IS NOT NULL AND enriched_at IS NULL")
            .sortKeys(Map.of("id", Order.ASCENDING))
            .pageSize(PAGE_SIZE)
            .rowMapper((resultSet, rowNumber) -> resultSet.getLong("id"))
            .build();
    }

    @Bean
    ItemProcessor<Long, Item> processor(ItemRepository itemRepository, ProductRepository productRepository, Clock clock) {
        return itemId -> itemRepository.findById(itemId)
            .map(item -> enrichItem(item, productRepository, LocalDateTime.now(clock)))
            .orElse(null);
    }

    @Bean
    ItemWriter<Item> writer(ItemRepository itemRepository) {
        return chunk -> itemRepository.saveAll(chunk.getItems());
    }

    @Bean
    @BatchTaskExecutor
    static TaskExecutor batchTaskExecutor() {
        var taskExecutor = new SimpleAsyncTaskExecutor("pantrio-batch-");
        taskExecutor.setConcurrencyLimit(1);
        return taskExecutor;
    }

    @Bean
    Step itemEnrichmentStep(JdbcPagingItemReader<Long> reader,
                            ItemProcessor<Long, Item> processor,
                            ItemWriter<Item> writer,
                            PlatformTransactionManager transactionManager) {
        return new StepBuilder("itemEnrichmentStep", jobRepository)
            .<Long, Item>chunk(CHUNK_SIZE)
            .transactionManager(transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .faultTolerant()
            .skip(RestClientException.class)
            .skipLimit(SKIP_LIMIT)
            .build();
    }

    @Bean
    Job generateItemEnrichmentJob(Step itemEnrichmentStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(itemEnrichmentStep)
            .build();
    }

    private static @Nullable Item enrichItem(Item item, ProductRepository productRepository, LocalDateTime now) {
        if (item.getBarcode() == null) {
            log.atWarn().addKeyValue("item_id", item.getId()).log("Item has no barcode, skipping enrichment");
            return null;
        }

        try {
            var product = productRepository.getProductByBarcode(item.getBarcode());
            item.enrich(product.productName(), product.imageUrl(), toUnit(product.unit(), item.getBarcode()), now);
        } catch (NoSuchElementFoundException e) {
            log.atInfo().addKeyValue("item_id", item.getId()).addKeyValue("barcode", item.getBarcode()).log("Product not found, item will not be enriched");
            item.markEnrichmentAttempted(now);
        }
        return item;
    }

    private static @Nullable UnitEnum toUnit(@Nullable String productUnit, String barcode) {
        if (productUnit == null) {
            return null;
        }
        return Arrays.stream(UnitEnum.values())
            .filter(unit -> unit.getValue().equalsIgnoreCase(productUnit))
            .findFirst()
            .orElseGet(() -> {
                log.atWarn().addKeyValue("unit", productUnit).addKeyValue("barcode", barcode).log("Unknown unit, skipping unit enrichment");
                return null;
            });
    }
}
