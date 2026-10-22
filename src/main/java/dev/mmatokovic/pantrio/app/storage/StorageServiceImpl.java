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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final StorageRepository storageRepository;
    private final StorageMapper storageMapper;
    private final InstanceRepository instanceRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StorageResponse> listStorages(Pageable pageable) {
        return storageMapper.toResponseList(storageRepository.findAll(pageable).getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public StorageResponse getStorage(Long id) {
        return storageMapper.toResponse(findStorage(id));
    }

    @Override
    @Transactional
    public StorageResponse createStorage(StorageRequest request) {
        if (storageRepository.existsByName(request.name())) {
            throw new ConflictException("Storage with name " + request.name() + " already exists");
        }
        return storageMapper.toResponse(storageRepository.save(Storage.create(request.name(), request.description(), request.disabled())));
    }

    @Override
    @Transactional
    public StorageResponse updateStorage(Long id, StorageRequest request) {
        var storage = findStorage(id);
        storage.update(request.name(), request.description(), request.disabled());
        return storageMapper.toResponse(storageRepository.save(storage));
    }

    @Override
    @Transactional
    public void deleteStorage(Long id) {
        storageRepository.delete(findStorage(id));
    }

    @Override
    @Transactional
    public StorageItemResponse addInstance(Long storageId, StorageItemRequest request) {
        var storage = findStorage(storageId);
        var item = itemRepository.findById(request.itemId())
            .orElseThrow(() -> new EntityNotFoundException("Item " + request.itemId() + " not found"));
        var instance = Instance.create(storage, item, request.quantity(), request.expiryDate());
        return storageMapper.toItemResponse(instanceRepository.save(instance));
    }

    @Override
    @Transactional
    public StorageItemResponse updateInstance(Long storageId, Long itemId, StorageItemUpdateRequest request) {
        var instance = findInstance(storageId, itemId);
        instance.restock(request.quantity(), request.expiryDate());
        return storageMapper.toItemResponse(instanceRepository.save(instance));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StorageItemResponse> listStock(Long storageId) {
        if (!storageRepository.existsById(storageId)) {
            throw new EntityNotFoundException("Storage " + storageId + " not found");
        }
        return storageMapper.toItemResponseList(instanceRepository.findAllByStorageId(storageId));
    }

    @Override
    @Transactional(readOnly = true)
    public StorageItemResponse getItem(Long storageId, Long itemId) {
        return storageMapper.toItemResponse(findInstance(storageId, itemId));
    }

    @Override
    @Transactional
    public void deleteInstance(Long storageId, Long itemId) {
        instanceRepository.delete(findInstance(storageId, itemId));
    }

    private Storage findStorage(Long id) {
        return storageRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Storage " + id + " not found"));
    }

    private Instance findInstance(Long storageId, Long itemId) {
        return instanceRepository.findByStorageIdAndItemId(storageId, itemId)
            .orElseThrow(() -> new EntityNotFoundException("Item " + itemId + " not found in storage " + storageId));
    }
}
