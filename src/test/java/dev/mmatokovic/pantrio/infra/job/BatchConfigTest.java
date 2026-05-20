package dev.mmatokovic.pantrio.infra.job;

import dev.mmatokovic.pantrio.domain.item.Item;
import dev.mmatokovic.pantrio.domain.item.UnitEnum;
import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchConfigTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ProductRepository productRepository;

    private ItemProcessor<Item, Item> processor;

    @BeforeEach
    void setUp() {
        BatchConfig batchConfig = new BatchConfig(jobRepository);
        processor = batchConfig.processor(productRepository);
    }

    @Test
    void should_return_null_when_barcode_is_null() throws Exception {
        Item item = Item.builder().id(1L).name("test").build();

        assertThat(processor.process(item)).isNull();
    }

    @Test
    void should_enrich_item_name_picture_and_unit() throws Exception {
        Item item = Item.builder().id(1L).barcode("737628064502").build();
        Product product = new Product("737628064502", "Pasta", 500L, "G", "https://example.com/pasta.jpg");
        when(productRepository.getProductByBarcode("737628064502")).thenReturn(product);

        Item result = processor.process(item);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Pasta");
        assertThat(result.getPictureUrl()).isEqualTo("https://example.com/pasta.jpg");
        assertThat(result.getUnit()).isEqualTo(UnitEnum.G);
    }

    @Test
    void should_not_set_unit_when_product_unit_is_null() throws Exception {
        Item item = Item.builder().id(1L).barcode("737628064502").unit(UnitEnum.PCS).build();
        Product product = new Product("737628064502", "Pasta", null, null, "https://example.com/pasta.jpg");
        when(productRepository.getProductByBarcode("737628064502")).thenReturn(product);

        Item result = processor.process(item);

        assertThat(result).isNotNull();
        assertThat(result.getUnit()).isEqualTo(UnitEnum.PCS);
    }

    @Test
    void should_leave_unit_unchanged_when_product_unit_is_unknown() {
        Item item = Item.builder().id(1L).barcode("737628064502").unit(UnitEnum.PCS).build();
        Product product = new Product("737628064502", "Pasta", null, "oz", "https://example.com/pasta.jpg");
        when(productRepository.getProductByBarcode("737628064502")).thenReturn(product);

        assertThatNoException().isThrownBy(() -> processor.process(item));
        assertThat(item.getUnit()).isEqualTo(UnitEnum.PCS);
    }

    @Test
    void should_propagate_exception_from_product_repository() {
        Item item = Item.builder().id(1L).barcode("000000000000").build();
        when(productRepository.getProductByBarcode("000000000000"))
            .thenThrow(new IllegalStateException("API unavailable"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> processor.process(item))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("API unavailable");
    }
}
