package dev.mmatokovic.pantrio.infra.openfoodfacts;

import dev.mmatokovic.pantrio.domain.product.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class OpenFoodFactsMapperShould {

    @Test
    void toDomain_mapProductFields() {
        var response = new OpenFoodFactsProductResponse("737628064502",
            new OpenFoodFactsProductResponse.Product("737628064502", "Pasta", "500", "g", "https://example.com/pasta.jpg"), 1, "product found");

        var product = new OpenFoodFactsMapperImpl().toDomain(response);

        assertThat(product).isEqualTo(new Product("737628064502", "Pasta", 500L, "g", "https://example.com/pasta.jpg"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "1.5", "about 500"})
    void toQuantity_returnNull_whenQuantityIsNotAWholeNumber(String quantity) {
        assertThat(new OpenFoodFactsMapperImpl().toQuantity(quantity)).isNull();
    }

    @Test
    void toQuantity_acceptWholeNumberWrittenAsDecimal() {
        assertThat(new OpenFoodFactsMapperImpl().toQuantity("500.0")).isEqualTo(500L);
    }
}
