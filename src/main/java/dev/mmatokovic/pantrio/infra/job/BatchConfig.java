package dev.mmatokovic.pantrio.infra.job;

import dev.mmatokovic.pantrio.domain.item.Item;
import dev.mmatokovic.pantrio.domain.item.ItemRepository;
import dev.mmatokovic.pantrio.domain.item.UnitEnum;
import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.Map;

import static dev.mmatokovic.pantrio.infra.job.QuartzConfig.JOB_NAME;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private static final int CHUNK_SIZE = 10;

    private final JobRepository jobRepository;

    @Bean
    public ItemReader<Item> reader(ItemRepository itemRepository) {
        return new RepositoryItemReaderBuilder<Item>()
            .name("itemReader")
            .repository(itemRepository)
            .methodName("findByBarcodeIsNotNullAndPictureUrlIsNull")
            .pageSize(100)
            .sorts(Map.of("id", Sort.Direction.ASC))
            .build();
    }

    @Bean
    public ItemProcessor<Item, Item> processor(ProductRepository productRepository) {
        return item -> enrichItem(item, productRepository);
    }

    @Bean
    public ItemWriter<Item> writer(ItemRepository itemRepository) {
        return chunk -> {
            for (Item item : chunk.getItems()) {
                itemRepository.save(item);
            }
        };
    }

    @Bean
    public Step itemEnrichmentStep(ItemReader<Item> reader,
                                   ItemProcessor<Item, Item> processor,
                                   ItemWriter<Item> writer) {
        return new StepBuilder("itemEnrichmentStep", jobRepository)
            .<Item, Item>chunk(CHUNK_SIZE)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .faultTolerant()
            .skip(Exception.class)
            .skipLimit(2)
            .build();
    }

    @Bean
    public Job generateItemEnrichmentJob(Step itemEnrichmentStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(itemEnrichmentStep)
            .build();
    }

    private Item enrichItem(Item item, ProductRepository productRepository) {
        if (item.getBarcode() == null) {
            return null;
        }

        Product product = productRepository.getProductByBarcode(item.getBarcode());

        item.setName(product.productName());
        item.setPictureUrl(product.imageUrl());
        if (product.unit() != null) {
            item.setUnit(UnitEnum.valueOf(product.unit().toUpperCase()));
        }

        return item;
    }
}
