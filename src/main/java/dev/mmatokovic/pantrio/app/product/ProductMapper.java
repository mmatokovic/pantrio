package dev.mmatokovic.pantrio.app.product;

import dev.mmatokovic.pantrio.domain.product.Product;
import dev.mmatokovic.pantrio.domain.product.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "name", source = "productName")
    @Mapping(target = "barcode", source = "code")
    @Mapping(target = "pictureUrl", source = "imageUrl")
    ProductResponse toResponse(Product product);
}
