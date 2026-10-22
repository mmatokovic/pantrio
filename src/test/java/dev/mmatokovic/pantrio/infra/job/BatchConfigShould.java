package dev.mmatokovic.pantrio.infra.job;

import dev.mmatokovic.pantrio.domain.item.Item;
import dev.mmatokovic.pantrio.domain.item.UnitEnum;
import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.repository.JobRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchConfigShould {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ProductRepository productRepository;

    @Test
    void processor_skipItem_whenBarcodeIsMissing() throws Exception {
        var processor = new BatchConfig(jobRepository).processor(productRepository);
        var item = Item.builder().id(1L).name("test").build();

        var result = processor.process(item);

        assertThat(result).isNull();
        verifyNoInteractions(productRepository, jobRepository);
    }

    @Test
    void processor_enrichNamePictureAndUnit() throws Exception {
        var processor = new BatchConfig(jobRepository).processor(productRepository);
        var item = Item.builder().id(1L).barcode("737628064502").build();
        when(productRepository.getProductByBarcode("737628064502"))
            .thenReturn(new Product("737628064502", "Pasta", 500L, "g", "https://example.com/pasta.jpg"));

        var result = processor.process(item);

        assertThat(result)
            .extracting(Item::getName, Item::getPictureUrl, Item::getUnit)
            .containsExactly("Pasta", "https://example.com/pasta.jpg", UnitEnum.G);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(jobRepository);
    }

    @Test
    void processor_keepUnit_whenProductHasNoUnit() throws Exception {
        var processor = new BatchConfig(jobRepository).processor(productRepository);
        var item = Item.builder().id(1L).barcode("737628064502").unit(UnitEnum.PCS).build();
        when(productRepository.getProductByBarcode("737628064502"))
            .thenReturn(new Product("737628064502", "Pasta", null, null, "https://example.com/pasta.jpg"));

        var result = processor.process(item);

        assertThat(result)
            .extracting(Item::getName, Item::getPictureUrl, Item::getUnit)
            .containsExactly("Pasta", "https://example.com/pasta.jpg", UnitEnum.PCS);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(jobRepository);
    }

    @Test
    void processor_keepUnit_whenProductUnitIsUnknown() throws Exception {
        var processor = new BatchConfig(jobRepository).processor(productRepository);
        var item = Item.builder().id(1L).barcode("737628064502").unit(UnitEnum.PCS).build();
        when(productRepository.getProductByBarcode("737628064502"))
            .thenReturn(new Product("737628064502", "Pasta", null, "oz", "https://example.com/pasta.jpg"));

        var result = processor.process(item);

        assertThat(result)
            .extracting(Item::getName, Item::getPictureUrl, Item::getUnit)
            .containsExactly("Pasta", "https://example.com/pasta.jpg", UnitEnum.PCS);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(jobRepository);
    }

    @Test
    void processor_rethrowRepositoryException() {
        var processor = new BatchConfig(jobRepository).processor(productRepository);
        var item = Item.builder().id(1L).barcode("000000000000").build();
        var error = new IllegalStateException("API unavailable");
        when(productRepository.getProductByBarcode("000000000000")).thenThrow(error);

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> processor.process(item))
            .isEqualTo(error);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(jobRepository);
    }
}
