package dev.mmatokovic.pantrio.app.item;

import dev.mmatokovic.pantrio.domain.item.ItemRequest;
import dev.mmatokovic.pantrio.domain.item.ItemResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemService {

    /**
     * Returns a paginated list of items.
     *
     * @param pageable pagination and sorting information
     * @return a list of items for the requested page
     */
    List<ItemResponse> listItems(Pageable pageable);

    /**
     * Retrieves a single item by its unique identifier.
     *
     * @param id the unique identifier of the item
     * @return the requested item
     */
    ItemResponse getItem(Long id);

    /**
     * Creates a new item using the provided request data.
     *
     * @param request data required to create a new item
     * @return the newly created item
     */
    ItemResponse createItem(ItemRequest request);

    /**
     * Updates an existing item identified by its unique identifier.
     *
     * @param id the unique identifier of the item to update
     * @param request updated item data
     * @return the updated item
     */
    ItemResponse updateItem(Long id, ItemRequest request);

    /**
     * Deletes an item identified by its unique identifier.
     *
     * @param id the unique identifier of the item to delete
     */
    void deleteItem(Long id);
}
