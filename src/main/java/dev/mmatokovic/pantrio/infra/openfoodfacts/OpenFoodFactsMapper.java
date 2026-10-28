package dev.mmatokovic.pantrio.infra.openfoodfacts;

import dev.mmatokovic.pantrio.domain.product.Product;
import org.jspecify.annotations.Nullable;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OpenFoodFactsMapper {

    @Mapping(target = "productName", source = "product.productName")
    @Mapping(target = "quantity", source = "product.quantity")
    @Mapping(target = "unit", source = "product.unit")
    @Mapping(target = "imageUrl", source = "product.imageUrl")
    Product toDomain(OpenFoodFactsProductResponse response);

    default @Nullable Long toQuantity(@Nullable String quantity) {
        if (quantity == null || quantity.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(quantity.trim()).longValueExact();
        } catch (NumberFormatException | ArithmeticException e) {
            return null;
        }
    }
}
