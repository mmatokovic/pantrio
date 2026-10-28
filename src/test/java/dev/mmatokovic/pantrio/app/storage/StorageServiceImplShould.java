package dev.mmatokovic.pantrio.app.storage;

import dev.mmatokovic.pantrio.app.item.ItemMapperImpl;
import dev.mmatokovic.pantrio.domain.instance.Instance;
import dev.mmatokovic.pantrio.domain.instance.InstanceRepository;
import dev.mmatokovic.pantrio.domain.item.Item;
import dev.mmatokovic.pantrio.domain.item.ItemRepository;
import dev.mmatokovic.pantrio.domain.item.ItemResponse;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageServiceImplShould {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 28, 12, 0);
    private static final Clock CLOCK = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private InstanceRepository instanceRepository;

    @Mock
    private ItemRepository itemRepository;

    @Test
    void listStorages_returnMappedPage() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        when(storageRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(Storage.create("Fridge", null, false, NOW))));

        var response = storageService.listStorages(PageRequest.of(0, 10));

        assertThat(response).isEqualTo(List.of(new StorageResponse(null, "Fridge", null, false, NOW, NOW)));
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(instanceRepository, itemRepository);
    }

    @Test
    void getStorage_returnMappedStorage() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        when(storageRepository.findById(1L)).thenReturn(Optional.of(Storage.create("Fridge", "Kitchen", true, NOW)));

        var response = storageService.getStorage(1L);

        assertThat(response).isEqualTo(new StorageResponse(null, "Fridge", "Kitchen", true, NOW, NOW));
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(instanceRepository, itemRepository);
    }

    @Test
    void getStorage_throwEntityNotFound_whenStorageDoesNotExist() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        when(storageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.getStorage(1L))
            .withMessage("Storage 1 not found");
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(instanceRepository, itemRepository);
    }

    @Test
    void createStorage_saveNewStorageFromRequest() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        when(storageRepository.existsByName("Fridge")).thenReturn(false);
        // A new entity has no identity to stub with, so the repository echoes what it is given
        when(storageRepository.save(any(Storage.class))).then(returnsFirstArg());

        var response = storageService.createStorage(new StorageRequest("Fridge", "Kitchen fridge", null));

        assertThat(response).isEqualTo(new StorageResponse(null, "Fridge", "Kitchen fridge", false, NOW, NOW));
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(instanceRepository, itemRepository);
    }

    @Test
    void createStorage_throwConflict_whenNameIsTaken() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        var request = new StorageRequest("Fridge", null, false);
        when(storageRepository.existsByName("Fridge")).thenReturn(true);

        assertThatExceptionOfType(ConflictException.class)
            .isThrownBy(() -> storageService.createStorage(request))
            .withMessage("Storage with name Fridge already exists");
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(instanceRepository, itemRepository);
    }

    @Test
    void updateStorage_applyRequestToExistingStorage() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        var storage = Storage.create("Fridge", null, false, NOW);
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(storageRepository.save(storage)).thenReturn(storage);

        var response = storageService.updateStorage(1L, new StorageRequest("Pantry", "Dry goods", true));

        assertThat(response).isEqualTo(new StorageResponse(null, "Pantry", "Dry goods", true, NOW, NOW));
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(instanceRepository, itemRepository);
    }

    @Test
    void updateStorage_enableStorage_whenDisabledIsMissing() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        var storage = Storage.create("Fridge", null, true, NOW);
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(storageRepository.save(storage)).thenReturn(storage);

        var response = storageService.updateStorage(1L, new StorageRequest("Pantry", null, null));

        assertThat(response).isEqualTo(new StorageResponse(null, "Pantry", null, false, NOW, NOW));
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(instanceRepository, itemRepository);
    }

    @Test
    void updateStorage_throwEntityNotFound_whenStorageDoesNotExist() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        var request = new StorageRequest("Pantry", null, false);
        when(storageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.updateStorage(99L, request))
            .withMessage("Storage 99 not found");
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(instanceRepository, itemRepository);
    }

    @Test
    void deleteStorage_deleteExistingStorage() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        var storage = Storage.create("Fridge", null, false, NOW);
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));

        storageService.deleteStorage(1L);

        verify(storageRepository).delete(storage);
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(instanceRepository, itemRepository);
    }

    @Test
    void deleteStorage_throwEntityNotFound_whenStorageDoesNotExist() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        when(storageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.deleteStorage(99L))
            .withMessage("Storage 99 not found");
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(instanceRepository, itemRepository);
    }

    @Test
    void addInstance_saveInstanceOfItemInStorage() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        when(storageRepository.findById(1L)).thenReturn(Optional.of(Storage.create("Fridge", null, false, NOW)));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(Item.create("Milk", null, null, null, null, false, NOW)));
        // A new entity has no identity to stub with, so the repository echoes what it is given
        when(instanceRepository.save(any(Instance.class))).then(returnsFirstArg());

        var response = storageService.addInstance(1L, new StorageItemRequest(2L, 3, LocalDate.of(2026, 11, 1)));

        var expectedResponse = new StorageItemResponse(
            null,
            3L,
            LocalDate.of(2026, 11, 1),
            new ItemResponse(null, "Milk", null, null, null, null, false, NOW, NOW),
            new StorageResponse(null, "Fridge", null, false, NOW, NOW),
            NOW,
            NOW);
        assertThat(response).isEqualTo(expectedResponse);
        verifyNoMoreInteractions(storageRepository, itemRepository, instanceRepository);
    }

    @Test
    void addInstance_throwEntityNotFound_whenStorageDoesNotExist() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        var request = new StorageItemRequest(2L, 3, null);
        when(storageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.addInstance(99L, request))
            .withMessage("Storage 99 not found");
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(itemRepository, instanceRepository);
    }

    @Test
    void addInstance_throwEntityNotFound_whenItemDoesNotExist() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        var request = new StorageItemRequest(99L, 3, null);
        when(storageRepository.findById(1L)).thenReturn(Optional.of(Storage.create("Fridge", null, false, NOW)));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.addInstance(1L, request))
            .withMessage("Item 99 not found");
        verifyNoMoreInteractions(storageRepository, itemRepository);
        verifyNoInteractions(instanceRepository);
    }

    @Test
    void updateInstance_restockQuantityAndExpiryDate() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        var instance = Instance.create(Storage.create("Fridge", null, false, NOW), Item.create("Milk", null, null, null, null, false, NOW), 3, null, NOW);
        when(instanceRepository.findByStorageIdAndItemId(1L, 2L)).thenReturn(Optional.of(instance));
        when(instanceRepository.save(instance)).thenReturn(instance);

        var response = storageService.updateInstance(1L, 2L, new StorageItemUpdateRequest(5, LocalDate.of(2026, 12, 31)));

        var expectedResponse = new StorageItemResponse(
            null,
            5L,
            LocalDate.of(2026, 12, 31),
            new ItemResponse(null, "Milk", null, null, null, null, false, NOW, NOW),
            new StorageResponse(null, "Fridge", null, false, NOW, NOW),
            NOW,
            NOW);
        assertThat(response).isEqualTo(expectedResponse);
        verifyNoMoreInteractions(instanceRepository);
        verifyNoInteractions(storageRepository, itemRepository);
    }

    @Test
    void updateInstance_throwEntityNotFound_whenInstanceDoesNotExist() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        var request = new StorageItemUpdateRequest(5, null);
        when(instanceRepository.findByStorageIdAndItemId(1L, 99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.updateInstance(1L, 99L, request))
            .withMessage("Item 99 not found in storage 1");
        verifyNoMoreInteractions(instanceRepository);
        verifyNoInteractions(storageRepository, itemRepository);
    }

    @Test
    void listStock_returnInstancesOfStorage() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        var instance = Instance.create(Storage.create("Fridge", null, false, NOW), Item.create("Milk", null, null, null, null, false, NOW), 3, null, NOW);
        when(storageRepository.existsById(1L)).thenReturn(true);
        when(instanceRepository.findAllByStorageId(1L)).thenReturn(List.of(instance));

        var response = storageService.listStock(1L);

        var expectedResponse = List.of(new StorageItemResponse(
            null,
            3L,
            null,
            new ItemResponse(null, "Milk", null, null, null, null, false, NOW, NOW),
            new StorageResponse(null, "Fridge", null, false, NOW, NOW),
            NOW,
            NOW));
        assertThat(response).isEqualTo(expectedResponse);
        verifyNoMoreInteractions(storageRepository, instanceRepository);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void listStock_throwEntityNotFound_whenStorageDoesNotExist() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        when(storageRepository.existsById(99L)).thenReturn(false);

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.listStock(99L))
            .withMessage("Storage 99 not found");
        verifyNoMoreInteractions(storageRepository);
        verifyNoInteractions(instanceRepository, itemRepository);
    }

    @Test
    void getItem_returnMappedInstance() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        var instance = Instance.create(Storage.create("Fridge", null, false, NOW), Item.create("Milk", null, null, null, null, false, NOW), 3, null, NOW);
        when(instanceRepository.findByStorageIdAndItemId(1L, 2L)).thenReturn(Optional.of(instance));

        var response = storageService.getItem(1L, 2L);

        var expectedResponse = new StorageItemResponse(
            null,
            3L,
            null,
            new ItemResponse(null, "Milk", null, null, null, null, false, NOW, NOW),
            new StorageResponse(null, "Fridge", null, false, NOW, NOW),
            NOW,
            NOW);
        assertThat(response).isEqualTo(expectedResponse);
        verifyNoMoreInteractions(instanceRepository);
        verifyNoInteractions(storageRepository, itemRepository);
    }

    @Test
    void getItem_throwEntityNotFound_whenItemIsNotInStorage() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        when(instanceRepository.findByStorageIdAndItemId(1L, 99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.getItem(1L, 99L))
            .withMessage("Item 99 not found in storage 1");
        verifyNoMoreInteractions(instanceRepository);
        verifyNoInteractions(storageRepository, itemRepository);
    }

    @Test
    void deleteInstance_deleteExistingInstance() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        var instance = Instance.create(Storage.create("Fridge", null, false, NOW), Item.create("Milk", null, null, null, null, false, NOW), 3, null, NOW);
        when(instanceRepository.findByStorageIdAndItemId(1L, 2L)).thenReturn(Optional.of(instance));

        storageService.deleteInstance(1L, 2L);

        verify(instanceRepository).delete(instance);
        verifyNoMoreInteractions(instanceRepository);
        verifyNoInteractions(storageRepository, itemRepository);
    }

    @Test
    void deleteInstance_throwEntityNotFound_whenInstanceDoesNotExist() {
        var storageService = new StorageServiceImpl(storageRepository, new StorageMapperImpl(new ItemMapperImpl()), instanceRepository, itemRepository, CLOCK);
        when(instanceRepository.findByStorageIdAndItemId(1L, 99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.deleteInstance(1L, 99L))
            .withMessage("Item 99 not found in storage 1");
        verifyNoMoreInteractions(instanceRepository);
        verifyNoInteractions(storageRepository, itemRepository);
    }
}
