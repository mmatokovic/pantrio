package dev.mmatokovic.pantrio.app.storage;

import dev.mmatokovic.pantrio.domain.instance.Instance;
import dev.mmatokovic.pantrio.domain.instance.InstanceRepository;
import dev.mmatokovic.pantrio.domain.item.Item;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageServiceImplShould {

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private StorageMapper storageMapper;

    @Mock
    private InstanceRepository instanceRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private StorageServiceImpl storageService;

    @Test
    void listStorages_returnMappedPage() {
        var storage = Storage.builder().id(1L).name("Fridge").build();
        var storageResponse = new StorageResponse(1L, "Fridge", null, false, null, null);
        when(storageRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(storage)));
        when(storageMapper.toResponseList(List.of(storage))).thenReturn(List.of(storageResponse));

        var response = storageService.listStorages(PageRequest.of(0, 10));

        assertThat(response).isEqualTo(List.of(storageResponse));
        verifyNoMoreInteractions(storageRepository, storageMapper);
        verifyNoInteractions(instanceRepository, itemRepository);
    }

    @Test
    void getStorage_returnMappedStorage() {
        var storage = Storage.builder().id(1L).name("Fridge").build();
        var storageResponse = new StorageResponse(1L, "Fridge", null, false, null, null);
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(storageMapper.toResponse(storage)).thenReturn(storageResponse);

        var response = storageService.getStorage(1L);

        assertThat(response).isEqualTo(storageResponse);
        verifyNoMoreInteractions(storageRepository, storageMapper);
        verifyNoInteractions(instanceRepository, itemRepository);
    }

    @Test
    void getStorage_throwEntityNotFound_whenStorageDoesNotExist() {
        when(storageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.getStorage(1L))
            .withMessage("Storage 1 not found");
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(storageMapper, instanceRepository, itemRepository);
    }

    @Test
    void createStorage_saveMappedEntity() {
        var request = new StorageRequest("Fridge", "Kitchen fridge", false);
        var storage = Storage.builder().name("Fridge").description("Kitchen fridge").build();
        var storageResponse = new StorageResponse(1L, "Fridge", "Kitchen fridge", false, null, null);
        when(storageRepository.existsByName("Fridge")).thenReturn(false);
        when(storageMapper.toEntity(request)).thenReturn(storage);
        when(storageRepository.save(storage)).thenReturn(storage);
        when(storageMapper.toResponse(storage)).thenReturn(storageResponse);

        var response = storageService.createStorage(request);

        assertThat(response).isEqualTo(storageResponse);
        verifyNoMoreInteractions(storageRepository, storageMapper);
        verifyNoInteractions(instanceRepository, itemRepository);
    }

    @Test
    void createStorage_throwConflict_whenNameIsTaken() {
        var request = new StorageRequest("Fridge", null, false);
        when(storageRepository.existsByName("Fridge")).thenReturn(true);

        assertThatExceptionOfType(ConflictException.class)
            .isThrownBy(() -> storageService.createStorage(request))
            .withMessage("Storage with name Fridge already exists");
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(storageMapper, instanceRepository, itemRepository);
    }

    @Test
    void updateStorage_applyRequestToExistingStorage() {
        var request = new StorageRequest("Pantry", "Dry goods", true);
        var storage = Storage.builder().id(1L).name("Fridge").build();
        var storageResponse = new StorageResponse(1L, "Pantry", "Dry goods", true, null, null);
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(storageRepository.save(storage)).thenReturn(storage);
        when(storageMapper.toResponse(storage)).thenReturn(storageResponse);

        var response = storageService.updateStorage(1L, request);

        assertThat(response).isEqualTo(storageResponse);
        assertThat(storage)
            .extracting(Storage::getName, Storage::getDescription, Storage::isDisabled)
            .containsExactly("Pantry", "Dry goods", true);
        verifyNoMoreInteractions(storageRepository, storageMapper);
        verifyNoInteractions(instanceRepository, itemRepository);
    }

    @Test
    void updateStorage_enableStorage_whenDisabledIsMissing() {
        var request = new StorageRequest("Pantry", null, null);
        var storage = Storage.builder().id(1L).name("Fridge").disabled(true).build();
        var storageResponse = new StorageResponse(1L, "Pantry", null, false, null, null);
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(storageRepository.save(storage)).thenReturn(storage);
        when(storageMapper.toResponse(storage)).thenReturn(storageResponse);

        var response = storageService.updateStorage(1L, request);

        assertThat(response).isEqualTo(storageResponse);
        assertThat(storage.isDisabled()).isFalse();
        verifyNoMoreInteractions(storageRepository, storageMapper);
        verifyNoInteractions(instanceRepository, itemRepository);
    }

    @Test
    void updateStorage_throwEntityNotFound_whenStorageDoesNotExist() {
        var request = new StorageRequest("Pantry", null, false);
        when(storageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.updateStorage(99L, request))
            .withMessage("Storage 99 not found");
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(storageMapper, instanceRepository, itemRepository);
    }

    @Test
    void deleteStorage_deleteExistingStorage() {
        var storage = Storage.builder().id(1L).name("Fridge").build();
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));

        storageService.deleteStorage(1L);

        verify(storageRepository).delete(storage);
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(storageMapper, instanceRepository, itemRepository);
    }

    @Test
    void deleteStorage_throwEntityNotFound_whenStorageDoesNotExist() {
        when(storageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.deleteStorage(99L))
            .withMessage("Storage 99 not found");
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(storageMapper, instanceRepository, itemRepository);
    }

    @Test
    void addInstance_saveInstanceLinkedToStorageAndItem() {
        var request = new StorageItemRequest(2L, 3, LocalDate.of(2026, 11, 1));
        var storage = Storage.builder().id(1L).name("Fridge").build();
        var item = Item.builder().id(2L).name("Milk").build();
        var instance = new Instance();
        var storageItemResponse = new StorageItemResponse(10L, 3L, LocalDate.of(2026, 11, 1), null, null, null, null);
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item));
        when(storageMapper.toInstance(request)).thenReturn(instance);
        when(instanceRepository.save(instance)).thenReturn(instance);
        when(storageMapper.toItemResponse(instance)).thenReturn(storageItemResponse);

        var response = storageService.addInstance(1L, request);

        assertThat(response).isEqualTo(storageItemResponse);
        assertThat(instance)
            .extracting(Instance::getStorage, Instance::getItem)
            .containsExactly(storage, item);
        verifyNoMoreInteractions(storageRepository, itemRepository, storageMapper, instanceRepository);
    }

    @Test
    void addInstance_throwEntityNotFound_whenStorageDoesNotExist() {
        var request = new StorageItemRequest(2L, 3, null);
        when(storageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.addInstance(99L, request))
            .withMessage("Storage 99 not found");
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(itemRepository, storageMapper, instanceRepository);
    }

    @Test
    void addInstance_throwEntityNotFound_whenItemDoesNotExist() {
        var request = new StorageItemRequest(99L, 3, null);
        var storage = Storage.builder().id(1L).name("Fridge").build();
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.addInstance(1L, request))
            .withMessage("Item 99 not found");
        verifyNoMoreInteractions(storageRepository, itemRepository);
        verifyNoInteractions(storageMapper, instanceRepository);
    }

    @Test
    void updateInstance_applyQuantityAndExpiryDate() {
        var request = new StorageItemUpdateRequest(5, LocalDate.of(2026, 12, 31));
        var instance = new Instance();
        var storageItemResponse = new StorageItemResponse(10L, 5L, LocalDate.of(2026, 12, 31), null, null, null, null);
        when(instanceRepository.findByStorageIdAndItemId(1L, 2L)).thenReturn(Optional.of(instance));
        when(instanceRepository.save(instance)).thenReturn(instance);
        when(storageMapper.toItemResponse(instance)).thenReturn(storageItemResponse);

        var response = storageService.updateInstance(1L, 2L, request);

        assertThat(response).isEqualTo(storageItemResponse);
        assertThat(instance)
            .extracting(Instance::getQuantity, Instance::getExpiryDate)
            .containsExactly(5, LocalDate.of(2026, 12, 31));
        verifyNoMoreInteractions(instanceRepository, storageMapper);
        verifyNoInteractions(storageRepository, itemRepository);
    }

    @Test
    void updateInstance_throwEntityNotFound_whenInstanceDoesNotExist() {
        var request = new StorageItemUpdateRequest(5, null);
        when(instanceRepository.findByStorageIdAndItemId(1L, 99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.updateInstance(1L, 99L, request))
            .withMessage("Item 99 not found in storage 1");
        verifyNoMoreInteractions(instanceRepository);
        verifyNoInteractions(storageRepository, itemRepository, storageMapper);
    }

    @Test
    void listStock_returnInstancesOfStorage() {
        var instance = new Instance();
        var storageItemResponse = new StorageItemResponse(10L, 3L, null, null, null, null, null);
        when(storageRepository.existsById(1L)).thenReturn(true);
        when(instanceRepository.findAllByStorageId(1L)).thenReturn(List.of(instance));
        when(storageMapper.toItemResponseList(List.of(instance))).thenReturn(List.of(storageItemResponse));

        var response = storageService.listStock(1L);

        assertThat(response).isEqualTo(List.of(storageItemResponse));
        verifyNoMoreInteractions(storageRepository, instanceRepository, storageMapper);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void listStock_throwEntityNotFound_whenStorageDoesNotExist() {
        when(storageRepository.existsById(99L)).thenReturn(false);

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.listStock(99L))
            .withMessage("Storage 99 not found");
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(instanceRepository, storageMapper, itemRepository);
    }

    @Test
    void getItem_returnMappedInstance() {
        var instance = new Instance();
        var storageItemResponse = new StorageItemResponse(10L, 3L, null, null, null, null, null);
        when(instanceRepository.findByStorageIdAndItemId(1L, 2L)).thenReturn(Optional.of(instance));
        when(storageMapper.toItemResponse(instance)).thenReturn(storageItemResponse);

        var response = storageService.getItem(1L, 2L);

        assertThat(response).isEqualTo(storageItemResponse);
        verifyNoMoreInteractions(instanceRepository, storageMapper);
        verifyNoInteractions(storageRepository, itemRepository);
    }

    @Test
    void getItem_throwEntityNotFound_whenItemIsNotInStorage() {
        when(instanceRepository.findByStorageIdAndItemId(1L, 99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.getItem(1L, 99L))
            .withMessage("Item 99 not found in storage 1");
        verifyNoMoreInteractions(instanceRepository);
        verifyNoInteractions(storageRepository, itemRepository, storageMapper);
    }

    @Test
    void deleteInstance_deleteExistingInstance() {
        var instance = new Instance();
        when(instanceRepository.findByStorageIdAndItemId(1L, 2L)).thenReturn(Optional.of(instance));

        storageService.deleteInstance(1L, 2L);

        verify(instanceRepository).delete(instance);
        verifyNoMoreInteractions(instanceRepository);
        verifyNoInteractions(storageRepository, itemRepository, storageMapper);
    }

    @Test
    void deleteInstance_throwEntityNotFound_whenInstanceDoesNotExist() {
        when(instanceRepository.findByStorageIdAndItemId(1L, 99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.deleteInstance(1L, 99L))
            .withMessage("Item 99 not found in storage 1");
        verifyNoMoreInteractions(instanceRepository);
        verifyNoInteractions(storageRepository, itemRepository, storageMapper);
    }
}
