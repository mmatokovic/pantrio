package dev.mmatokovic.pantrio.app.storage;

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
}
