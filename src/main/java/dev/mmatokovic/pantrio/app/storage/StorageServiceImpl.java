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
import dev.mmatokovic.pantrio.infra.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final StorageRepository repo;
    private final StorageMapper mapper;
    private final InstanceRepository instanceRepository;
    private final ItemRepository itemRepository;

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
    @Transactional
    public StorageResponse createStorage(StorageRequest request) {
        if (repo.existsByName(request.name())) {
            throw new ConflictException("Storage with name '" + request.name() + "' already exists");
        }
        return mapper.toResponse(repo.save(mapper.toEntity(request)));
    }

    @Override
    public void updateStorage(Long id, StorageRequest request) {
        Storage storage = repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Storage " + id + " not found"));
        storage.setName(request.name());
        storage.setDescription(request.description());
        storage.setDisabled(request.disabled());
        mapper.toResponse(repo.save(storage));
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

        if (instanceRepository.existsByStorageAndItem(storage, item)) {
            throw new ConflictException("Item " + request.itemId() + " already exists in storage " + storageId);
        }

        Instance instance = mapper.toInstance(request);
        instance.setStorage(storage);
        instance.setItem(item);

        return mapper.toItemResponse(instanceRepository.save(instance));
    }
}
