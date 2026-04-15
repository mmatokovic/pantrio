package dev.mmatokovic.pantrio.app.storage;

import dev.mmatokovic.pantrio.domain.instance.Instance;
import dev.mmatokovic.pantrio.domain.instance.InstanceRepository;
import dev.mmatokovic.pantrio.domain.item.ItemRepository;
import dev.mmatokovic.pantrio.domain.storage.Storage;
import dev.mmatokovic.pantrio.domain.storage.StorageItemRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageItemResponse;
import dev.mmatokovic.pantrio.domain.storage.StorageItemUpdateRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageRepository;
import dev.mmatokovic.pantrio.domain.storage.StorageRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageResponse;
import dev.mmatokovic.pantrio.infra.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final StorageRepository storageRepo;
    private final StorageMapper mapper;
    private final InstanceRepository instanceRepo;
    private final ItemRepository itemRepo;

    @Override
    @Transactional(readOnly = true)
    public List<StorageResponse> listStorages(Pageable pageable) {
        return mapper.toResponseList(storageRepo.findAll(pageable).getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public StorageResponse getStorage(Long id) {
        return storageRepo.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new EntityNotFoundException("Storage " + id + " not found"));
    }

    @Override
    public StorageResponse createStorage(@NonNull StorageRequest request) {
        if (storageRepo.existsByName(request.name())) {
            throw new ConflictException("Storage with name " + request.name() + " already exists");
        }
        return mapper.toResponse(storageRepo.save(mapper.toEntity(request)));
    }

    @Override
    @Transactional
    public StorageResponse updateStorage(Long id, @NonNull StorageRequest request) {
        Storage storage = storageRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Storage " + id + " not found"));
        storage.setName(request.name());
        storage.setDescription(request.description());
        storage.setDisabled(request.disabled());
        return mapper.toResponse(storageRepo.save(storage));
    }

    @Override
    @Transactional
    public void deleteStorage(Long id) {
        storageRepo.delete(storageRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Storage " + id + " not found")));
    }

    @Override
    @Transactional
    public StorageItemResponse addInstance(Long storageId, StorageItemRequest request) {
        Instance instance = mapper.toInstance(request);
        instance.setStorage(storageRepo.getReferenceById(storageId));
        instance.setItem(itemRepo.getReferenceById(request.itemId()));
        return mapper.toItemResponse(instanceRepo.save(instance));
    }

    @Override
    @Transactional
    public StorageItemResponse updateInstance(Long storageId, Long itemId, @NonNull StorageItemUpdateRequest request) {
        Instance instance = instanceRepo.findByStorageIdAndItemId(storageId, itemId)
            .orElseThrow(() -> new EntityNotFoundException("Item " + itemId + " not found in storage " + storageId));
        instance.setQuantity(request.quantity());
        instance.setExpiryDate(request.expiryDate());
        return mapper.toItemResponse(instanceRepo.save(instance));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StorageItemResponse> listStock(Long storageId) {
        if (!storageRepo.existsById(storageId)) {
            throw new EntityNotFoundException("Storage " + storageId + " not found");
        }
        return mapper.toItemResponseList(instanceRepo.findAllByStorageId(storageId));

    }

    @Override
    @Transactional(readOnly = true)
    public StorageItemResponse getItem(Long storageId, Long itemId) {
        return instanceRepo.findByStorageIdAndItemId(storageId, itemId)
            .map(mapper::toItemResponse)
            .orElseThrow(() -> new EntityNotFoundException("Item " + itemId + " not found in storage " + storageId));
    }

    @Override
    @Transactional
    public void deleteInstance(Long storageId, Long itemId) {
        instanceRepo.delete(instanceRepo.findByStorageIdAndItemId(storageId, itemId)
            .orElseThrow(() -> new EntityNotFoundException("Item " + itemId + " not found in storage " + storageId)));
    }
}
