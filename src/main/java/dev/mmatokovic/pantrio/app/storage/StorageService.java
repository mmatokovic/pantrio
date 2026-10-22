package dev.mmatokovic.pantrio.app.storage;

import dev.mmatokovic.pantrio.domain.storage.StorageItemRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageItemResponse;
import dev.mmatokovic.pantrio.domain.storage.StorageItemUpdateRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StorageService {

    List<StorageResponse> listStorages(Pageable pageable);

    StorageResponse getStorage(Long id);

    StorageResponse createStorage(StorageRequest request);

    StorageResponse updateStorage(Long id, StorageRequest request);

    void deleteStorage(Long id);

    StorageItemResponse addInstance(Long storageId, StorageItemRequest request);

    StorageItemResponse updateInstance(Long storageId, Long itemId, StorageItemUpdateRequest request);

    List<StorageItemResponse> listStock(Long storageId);

    StorageItemResponse getItem(Long storageId, Long itemId);

    void deleteInstance(Long storageId, Long itemId);
}
