package dev.mmatokovic.pantrio.app.item;

import dev.mmatokovic.pantrio.domain.item.ItemRequest;
import dev.mmatokovic.pantrio.domain.item.ItemResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemService {

    List<ItemResponse> listItems(Pageable pageable);

    ItemResponse getItem(Long id);

    ItemResponse createItem(ItemRequest request);

    ItemResponse updateItem(Long id, ItemRequest request);

    ItemResponse replaceItem(Long id, ItemRequest request);

    void deleteItem(Long id);
}
