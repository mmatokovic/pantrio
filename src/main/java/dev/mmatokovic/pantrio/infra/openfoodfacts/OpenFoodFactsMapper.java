package dev.mmatokovic.pantrio.infra.openfoodfacts;

import dev.mmatokovic.pantrio.domain.product.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OpenFoodFactsMapper {

    @Mapping(target = "productName", source = "product.productName")
    @Mapping(target = "quantity", source = "product.quantity")
    @Mapping(target = "unit", source = "product.unit")
    @Mapping(target = "imageUrl", source = "product.imageUrl")
    Product toDomain(OpenFoodFactsProductResponse response);
}
