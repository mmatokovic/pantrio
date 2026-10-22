package dev.mmatokovic.pantrio.app.product;

import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductResponse;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mapping;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ProductMapper {

    @Mapping(target = "name", source = "productName")
    @Mapping(target = "barcode", source = "code")
    @Mapping(target = "pictureUrl", source = "imageUrl")
    ProductResponse toResponse(Product product);
}
