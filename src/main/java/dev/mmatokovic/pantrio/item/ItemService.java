package dev.mmatokovic.pantrio.item;

import dev.mmatokovic.pantrio.item.dto.ItemRequest;
import dev.mmatokovic.pantrio.item.dto.ItemResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemService {

    List<ItemResponse> listItems(Pageable pageable);

    ItemResponse getItem(Long id);

    ItemResponse createItem(ItemRequest request);

    ItemResponse updateItem(Long id, ItemRequest request);

    void deleteItem(Long id);
}
