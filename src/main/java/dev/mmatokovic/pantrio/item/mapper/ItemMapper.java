package dev.mmatokovic.pantrio.item.mapper;

import dev.mmatokovic.pantrio.item.Item;
import dev.mmatokovic.pantrio.item.record.ItemRequest;
import dev.mmatokovic.pantrio.item.record.ItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    ItemResponse toResponse(Item item);

    List<ItemResponse> toResponseList(List<Item> items);

    @Mapping(target = "id", ignore = true)
    Item toEntity(ItemRequest request);
}
