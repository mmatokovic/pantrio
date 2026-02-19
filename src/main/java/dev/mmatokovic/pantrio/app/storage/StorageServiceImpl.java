package dev.mmatokovic.pantrio.app.storage;

import dev.mmatokovic.pantrio.domain.instance.Instance;
import dev.mmatokovic.pantrio.domain.instance.InstanceRepository;
import dev.mmatokovic.pantrio.domain.item.Item;
import dev.mmatokovic.pantrio.domain.item.ItemRepository;
import dev.mmatokovic.pantrio.domain.storage.Storage;
import dev.mmatokovic.pantrio.domain.storage.StorageItemRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageItemResponse;
import dev.mmatokovic.pantrio.domain.storage.StorageRepository;
import dev.mmatokovic.pantrio.domain.storage.StorageRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StorageServiceImpl implements StorageService {
    private final StorageRepository repo;
    private final StorageMapper mapper;
    private final InstanceRepository instanceRepository;
    private final ItemRepository itemRepository;

    public StorageServiceImpl(StorageRepository repo, StorageMapper mapper, InstanceRepository instanceRepository, ItemRepository itemRepository) {
        this.repo = repo;
        this.instanceRepository = instanceRepository;
        this.mapper = mapper;
        this.itemRepository = itemRepository;
    }

    @Override
    public List<StorageResponse> listStorages(Pageable pageable) {
        return mapper.toResponseList(repo.findAll(pageable).getContent());
    }

    @Override
    public StorageResponse getStorage(Long id) {
        Storage storage = repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Storage " + id + " not found"));
        return mapper.toResponse(storage);
    }

    @Override
    public StorageResponse createStorage(StorageRequest request) {
        Storage storage = mapper.toEntity(request);
        Storage saved = repo.save(storage);
        return mapper.toResponse(saved);
    }

    @Override
    public void updateStorage(Long id, StorageRequest request) {
        Storage storage = repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Storage " + id + " not found"));
        storage.setName(request.name());
        storage.setDescription(request.description());
        storage.setDisabled(request.disabled());
        Storage updated = repo.save(storage);
        mapper.toResponse(updated);
    }

    @Override
    public void deleteStorage(Long id) {
        Storage storage = repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Storage " + id + " not found"));
        repo.delete(storage);
    }

    @Override
    public StorageItemResponse addInstance(Long storageId, StorageItemRequest request) {
        Storage storage = repo.findById(storageId)
            .orElseThrow(() -> new EntityNotFoundException("Storage " + storageId + " not found"));

        Item item = itemRepository.findById(request.itemId())
            .orElseThrow(() -> new EntityNotFoundException("Item " + request.itemId() + " not found"));

        Instance instance = instanceRepository.findByStorageAndItem(storage, item)
            .map(existing -> {
                existing.setQuantity(request.quantity());
                existing.setExpiryDate(request.expiryDate());
                return existing;
            })
            .orElseGet(() -> {
                Instance newInstance = mapper.toInstance(request);
                newInstance.setStorage(storage);
                newInstance.setItem(item);
                return newInstance;
            });

        Instance saved = instanceRepository.save(instance);

        return mapper.toItemResponse(saved);
    }
}
