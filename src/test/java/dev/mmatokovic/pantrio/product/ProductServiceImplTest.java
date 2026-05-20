package dev.mmatokovic.pantrio.product;

import dev.mmatokovic.pantrio.app.product.ProductMapper;
import dev.mmatokovic.pantrio.app.product.ProductServiceImpl;
import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductRepository;
import dev.mmatokovic.pantrio.domain.product.ProductResponse;
import dev.mmatokovic.pantrio.infra.exception.NoSuchElementFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repo;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductServiceImpl service;

    private static final String BARCODE = "737628064502";

    private Product product;
    private ProductResponse response;

    @BeforeEach
    void setUp() {
        product = new Product(BARCODE, "Pasta", 500L, "g", "https://example.com/pasta.jpg");
        response = new ProductResponse("Pasta", BARCODE, 500L, "g", "https://example.com/pasta.jpg");
    }

    @Test
    void should_return_product_response_when_found() {
        when(repo.getProductByBarcode(BARCODE)).thenReturn(product);
        when(mapper.toResponse(product)).thenReturn(response);

        var result = service.findProductByBarcode(BARCODE);

        assertThat(result).isEqualTo(response);
        verify(repo).getProductByBarcode(BARCODE);
        verify(mapper).toResponse(product);
    }

    @Test
    void should_throw_when_product_is_null() {
        when(repo.getProductByBarcode(BARCODE)).thenReturn(null);

        assertThatExceptionOfType(NoSuchElementFoundException.class)
            .isThrownBy(() -> service.findProductByBarcode(BARCODE))
            .withMessageContaining(BARCODE);
    }

    @Test
    void should_propagate_exception_from_repository() {
        when(repo.getProductByBarcode(BARCODE))
            .thenThrow(new NoSuchElementFoundException("Product " + BARCODE + " not found"));

        assertThatExceptionOfType(NoSuchElementFoundException.class)
            .isThrownBy(() -> service.findProductByBarcode(BARCODE))
            .withMessageContaining(BARCODE);
    }
}
