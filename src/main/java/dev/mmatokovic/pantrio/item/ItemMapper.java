package dev.mmatokovic.pantrio.item;

import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {

    public static ItemResponse toResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getRemarks(),
                item.getPictureUrl(),
                item.getStatus(),
                item.getDisabled()
        );
    }

    public static List<ItemResponse> toResponseList(List<Item> items) {
        return items.stream()
                .map(ItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static Item toEntity(ItemRequest request) {
        Item item = new Item();
        item.setName(request.name());
        item.setRemarks(request.remarks());
        item.setPictureUrl(request.pictureUrl());
        item.setStatus(request.status());
        item.setDisabled(request.disabled());
        return item;
    }
}
