package dev.mmatokovic.pantrio.app.product;

import dev.mmatokovic.pantrio.domain.product.OpenFoodFactsProductResponse;
import dev.mmatokovic.pantrio.domain.product.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "name", source = "product.productName")
    @Mapping(target = "barcode", source = "code")
    @Mapping(target = "unit", source = "product.unit")
    @Mapping(target = "pictureUrl", source = "product.imageUrl")
    @Mapping(target = "quantity", source = "product.quantity")
    ProductResponse toResponse(OpenFoodFactsProductResponse openFoodFactsProductResponse);
}
