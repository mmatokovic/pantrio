package dev.mmatokovic.pantrio.app.storage;

import dev.mmatokovic.pantrio.domain.storage.StorageItemRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageItemResponse;
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
    void updateStorage(Long id, StorageRequest request);

    /**
     * Deletes a storage identified by its unique identifier.
     *
     * @param id the unique identifier of the storage to delete
     */
    void deleteStorage(Long id);

    //TODO: write docs for this method
    StorageItemResponse addInstance(Long storageId, StorageItemRequest request);

    //StorageItemResponse getInstance(Long storageId, Long instanceId, Long quantity);

    //StorageItemResponse deleteInstance(Long storageId, Long instanceId, Long quantity);
}
