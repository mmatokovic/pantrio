package dev.mmatokovic.pantrio.app.product;

import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import dev.mmatokovic.pantrio.domain.product.ProductResponse;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplShould {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void findProductByBarcode_returnMappedProduct() {
        var product = new Product("737628064502", "Pasta", 500L, "g", "https://example.com/pasta.jpg");
        var productResponse = new ProductResponse("Pasta", "737628064502", 500L, "g", "https://example.com/pasta.jpg");
        when(productRepository.getProductByBarcode("737628064502")).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        var response = productService.findProductByBarcode("737628064502");

        assertThat(response).isEqualTo(productResponse);
        verifyNoMoreInteractions(productRepository, productMapper);
    }

    @Test
    void findProductByBarcode_throwNoSuchElementFound_whenRepositoryReturnsNull() {
        when(productRepository.getProductByBarcode("737628064502")).thenReturn(null);

        assertThatExceptionOfType(NoSuchElementFoundException.class)
            .isThrownBy(() -> productService.findProductByBarcode("737628064502"))
            .withMessage("Product not found for barcode: 737628064502");
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

    @Test
    void findProductByBarcode_rethrowRepositoryException() {
        var error = new NoSuchElementFoundException("Product 737628064502 not found");
        when(productRepository.getProductByBarcode("737628064502")).thenThrow(error);

        assertThatExceptionOfType(NoSuchElementFoundException.class)
            .isThrownBy(() -> productService.findProductByBarcode("737628064502"))
            .isEqualTo(error);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }
}
