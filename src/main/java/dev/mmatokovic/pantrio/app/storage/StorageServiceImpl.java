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
import org.springframework.dao.DataIntegrityViolationException;
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
    @Transactional(readOnly = true)
    public List<StorageResponse> listStorages(Pageable pageable) {
        return mapper.toResponseList(repo.findAll(pageable).getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public StorageResponse getStorage(Long id) {
        return repo.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new EntityNotFoundException("Storage " + id + " not found"));
    }

    @Override
    @Transactional
    public StorageResponse createStorage(@NonNull StorageRequest request) {
        return mapper.toResponse(repo.save(mapper.toEntity(request)));
    }

    @Override
    @Transactional
    public StorageResponse updateStorage(Long id, @NonNull StorageRequest request) {
        Storage storage = repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Storage " + id + " not found"));
        storage.setName(request.name());
        storage.setDescription(request.description());
        storage.setDisabled(request.disabled());
        return mapper.toResponse(repo.save(storage));
    }

    @Override
    @Transactional
    public void deleteStorage(Long id) {
        repo.delete(repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Storage " + id + " not found")));
    }

    @Override
    @Transactional
    public StorageItemResponse addInstance(Long storageId, StorageItemRequest request) {
        try {
            Instance instance = mapper.toInstance(request);
            instance.setStorage(repo.getReferenceById(storageId));
            instance.setItem(itemRepository.getReferenceById(request.itemId()));
            return mapper.toItemResponse(instanceRepository.save(instance));
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Storage " + storageId + " or Item " + request.itemId() + " not found");
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Item " + request.itemId() + " already exists in storage " + storageId);
        }
    }

    @Override
    @Transactional
    public StorageItemResponse updateInstance(Long storageId, Long itemId, @NonNull StorageItemUpdateRequest request) {
        Instance instance = instanceRepository.findByStorageIdAndItemId(storageId, itemId)
            .orElseThrow(() -> new EntityNotFoundException("Item " + itemId + " not found in storage " + storageId));
        instance.setQuantity(request.quantity());
        instance.setExpiryDate(request.expiryDate());
        return mapper.toItemResponse(instanceRepository.save(instance));
    }
}
