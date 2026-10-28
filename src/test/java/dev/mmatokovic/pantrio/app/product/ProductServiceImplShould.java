package dev.mmatokovic.pantrio.app.product;

import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import dev.mmatokovic.pantrio.domain.product.ProductResponse;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplShould {

    @Mock
    private ProductRepository productRepository;

    @Test
    void findProductByBarcode_returnMappedProduct() {
        var productService = new ProductServiceImpl(productRepository, new ProductMapperImpl());
        when(productRepository.getProductByBarcode("737628064502"))
            .thenReturn(new Product("737628064502", "Pasta", 500L, "g", "https://example.com/pasta.jpg"));

        var response = productService.findProductByBarcode("737628064502");

        assertThat(response).isEqualTo(new ProductResponse("Pasta", "737628064502", 500L, "g", "https://example.com/pasta.jpg"));
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void findProductByBarcode_rethrowRepositoryException() {
        var productService = new ProductServiceImpl(productRepository, new ProductMapperImpl());
        var error = new NoSuchElementFoundException("Product 737628064502 not found");
        when(productRepository.getProductByBarcode("737628064502")).thenThrow(error);

        assertThatExceptionOfType(NoSuchElementFoundException.class)
            .isThrownBy(() -> productService.findProductByBarcode("737628064502"))
            .isEqualTo(error);
        verifyNoMoreInteractions(productRepository);
    }
}
