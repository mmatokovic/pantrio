package dev.mmatokovic.pantrio.infra.job;

import dev.mmatokovic.pantrio.domain.item.Item;
import dev.mmatokovic.pantrio.domain.item.ItemRepository;
import dev.mmatokovic.pantrio.domain.item.UnitEnum;
import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.repository.JobRepository;

import java.util.Optional;

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
    private ItemRepository itemRepository;

    @Mock
    private ProductRepository productRepository;

    @Test
    void processor_skipItem_whenItemNoLongerExists() throws Exception {
        var processor = new BatchConfig(jobRepository).processor(itemRepository, productRepository);
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        var result = processor.process(1L);

        assertThat(result).isNull();
        verifyNoMoreInteractions(itemRepository);
        verifyNoInteractions(productRepository, jobRepository);
    }

    @Test
    void processor_skipItem_whenBarcodeIsMissing() throws Exception {
        var processor = new BatchConfig(jobRepository).processor(itemRepository, productRepository);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(Item.create("test", null, null, null, null, false)));

        var result = processor.process(1L);

        assertThat(result).isNull();
        verifyNoMoreInteractions(itemRepository);
        verifyNoInteractions(productRepository, jobRepository);
    }

    @Test
    void processor_enrichNamePictureAndUnit() throws Exception {
        var processor = new BatchConfig(jobRepository).processor(itemRepository, productRepository);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(Item.create("unknown", null, "737628064502", null, null, false)));
        when(productRepository.getProductByBarcode("737628064502"))
            .thenReturn(new Product("737628064502", "Pasta", 500L, "g", "https://example.com/pasta.jpg"));

        var result = processor.process(1L);

        assertThat(result)
            .extracting(Item::getName, Item::getPictureUrl, Item::getUnit)
            .containsExactly("Pasta", "https://example.com/pasta.jpg", UnitEnum.G);
        verifyNoMoreInteractions(itemRepository, productRepository);
        verifyNoInteractions(jobRepository);
    }

    @Test
    void processor_keepUnit_whenProductHasNoUnit() throws Exception {
        var processor = new BatchConfig(jobRepository).processor(itemRepository, productRepository);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(Item.create("unknown", null, "737628064502", UnitEnum.PCS, null, false)));
        when(productRepository.getProductByBarcode("737628064502"))
            .thenReturn(new Product("737628064502", "Pasta", null, null, "https://example.com/pasta.jpg"));

        var result = processor.process(1L);

        assertThat(result)
            .extracting(Item::getName, Item::getPictureUrl, Item::getUnit)
            .containsExactly("Pasta", "https://example.com/pasta.jpg", UnitEnum.PCS);
        verifyNoMoreInteractions(itemRepository, productRepository);
        verifyNoInteractions(jobRepository);
    }

    @Test
    void processor_keepUnit_whenProductUnitIsUnknown() throws Exception {
        var processor = new BatchConfig(jobRepository).processor(itemRepository, productRepository);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(Item.create("unknown", null, "737628064502", UnitEnum.PCS, null, false)));
        when(productRepository.getProductByBarcode("737628064502"))
            .thenReturn(new Product("737628064502", "Pasta", null, "oz", "https://example.com/pasta.jpg"));

        var result = processor.process(1L);

        assertThat(result)
            .extracting(Item::getName, Item::getPictureUrl, Item::getUnit)
            .containsExactly("Pasta", "https://example.com/pasta.jpg", UnitEnum.PCS);
        verifyNoMoreInteractions(itemRepository, productRepository);
        verifyNoInteractions(jobRepository);
    }

    @Test
    void processor_rethrowNotFound_soTheStepCanSkipTheItem() {
        var processor = new BatchConfig(jobRepository).processor(itemRepository, productRepository);
        var error = new NoSuchElementFoundException("Product 000000000000 not found");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(Item.create("unknown", null, "000000000000", null, null, false)));
        when(productRepository.getProductByBarcode("000000000000")).thenThrow(error);

        assertThatExceptionOfType(NoSuchElementFoundException.class)
            .isThrownBy(() -> processor.process(1L))
            .isEqualTo(error);
        verifyNoMoreInteractions(itemRepository, productRepository);
        verifyNoInteractions(jobRepository);
    }
}
