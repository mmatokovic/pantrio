package dev.mmatokovic.pantrio.infra.job;

import dev.mmatokovic.pantrio.domain.item.Item;
import dev.mmatokovic.pantrio.domain.item.ItemRepository;
import dev.mmatokovic.pantrio.domain.item.UnitEnum;
import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.Order;
import org.springframework.batch.infrastructure.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.util.Locale;
import java.util.Map;

import static dev.mmatokovic.pantrio.infra.job.QuartzConfig.JOB_NAME;

@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class BatchConfig {

    private static final int CHUNK_SIZE = 10;
    private static final int PAGE_SIZE = 100;
    private static final int SKIP_LIMIT = 2;

    private final JobRepository jobRepository;

    @Bean
    ItemReader<Item> reader(DataSource dataSource) throws Exception {
        return new JdbcPagingItemReaderBuilder<Item>()
            .name("itemReader")
            .dataSource(dataSource)
            .selectClause("SELECT *")
            .fromClause("FROM items")
            .whereClause("WHERE barcode IS NOT NULL AND picture_url IS NULL")
            .sortKeys(Map.of("id", Order.ASCENDING))
            .pageSize(PAGE_SIZE)
            .rowMapper(new BeanPropertyRowMapper<>(Item.class))
            .build();
    }

    @Bean
    ItemProcessor<Item, Item> processor(ProductRepository productRepository) {
        return item -> enrichItem(item, productRepository);
    }

    @Bean
    ItemWriter<Item> writer(ItemRepository itemRepository) {
        return chunk -> itemRepository.saveAll(chunk.getItems());
    }

    @Bean
    Step itemEnrichmentStep(ItemReader<Item> reader,
                            ItemProcessor<Item, Item> processor,
                            ItemWriter<Item> writer) {
        return new StepBuilder("itemEnrichmentStep", jobRepository)
            .<Item, Item>chunk(CHUNK_SIZE)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .faultTolerant()
            .skip(Exception.class)
            .skipLimit(SKIP_LIMIT)
            .build();
    }

    @Bean
    Job generateItemEnrichmentJob(Step itemEnrichmentStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(itemEnrichmentStep)
            .build();
    }

    private static @Nullable Item enrichItem(Item item, ProductRepository productRepository) {
        if (item.getBarcode() == null) {
            log.atWarn().addKeyValue("item_id", item.getId()).log("Item has no barcode, skipping enrichment");
            return null;
        }

        var product = productRepository.getProductByBarcode(item.getBarcode());
        item.setName(product.productName());
        item.setPictureUrl(product.imageUrl());
        if (product.unit() != null) {
            try {
                item.setUnit(UnitEnum.valueOf(product.unit().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                log.atWarn()
                    .addKeyValue("unit", product.unit())
                    .addKeyValue("barcode", item.getBarcode())
                    .log("Unknown unit, skipping unit enrichment");
            }
        }
        return item;
    }
}
