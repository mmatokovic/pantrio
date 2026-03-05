package dev.mmatokovic.pantrio.app.storage;

import dev.mmatokovic.pantrio.domain.storage.StorageItemRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageItemResponse;
import dev.mmatokovic.pantrio.domain.storage.StorageItemUpdateRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StorageService {

    /**
     * Returns a paginated list of storages.
     *
     * @param pageable pagination and sorting information
     * @return a list of storages for the requested page
     */
    List<StorageResponse> listStorages(Pageable pageable);

    /**
     * Retrieves a single storage by its unique identifier.
     *
     * @param id the unique identifier of the storage
     * @return the requested storage
     */
    StorageResponse getStorage(Long id);

    /**
     * Creates a new storage using the provided request data.
     *
     * @param request data required to create a new storage
     * @return the newly created storage
     */
    StorageResponse createStorage(StorageRequest request);

    /**
     * Updates an existing storage identified by its unique identifier.
     *
     * @param id      the unique identifier of the storage to update
     * @param request updated storage data
     */
    StorageResponse updateStorage(Long id, StorageRequest request);

    /**
     * Deletes a storage identified by its unique identifier.
     *
     * @param id the unique identifier of the storage to delete
     */
    void deleteStorage(Long id);

    /**
     * Adds a new item instance to an existing storage.
     *
     * @param storageId the unique identifier of the storage to which the item will be added
     * @param request   the details of the item instance to add
     * @return the newly added storage item with its assigned identifiers and details
     */
    StorageItemResponse addInstance(Long storageId, StorageItemRequest request);

    /**
     * Updates an existing item within a storage.
     *
     * @param storageId the unique identifier of the storage
     * @param itemId    the unique identifier of the item to update
     * @param request   the updated item details
     * @return the updated storage item {@link StorageItemResponse}
     */
    StorageItemResponse updateInstance(Long storageId, Long itemId, StorageItemUpdateRequest request);
}
