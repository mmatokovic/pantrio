package dev.mmatokovic.pantrio.storage;

import dev.mmatokovic.pantrio.app.storage.StorageMapper;
import dev.mmatokovic.pantrio.app.storage.StorageServiceImpl;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageServiceImplTest {

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

    private Storage storage;
    private StorageRequest storageRequest;
    private StorageResponse storageResponse;
    private Item item;
    private Instance instance;
    private StorageItemResponse instanceResponse;

    @BeforeEach
    void setUp() {
        storage = Storage.builder().id(1L).name("Fridge").build();
        storageRequest = new StorageRequest("Fridge", null, false);
        storageResponse = new StorageResponse(1L, "Fridge", null, false, null, null);
        item = Item.builder().id(2L).name("Milk").build();
        instance = new Instance();
        instanceResponse = new StorageItemResponse(10L, 3L, null, null, null, null, null);
    }

    @Test
    void should_return_storages_when_listing_with_pagination() {
        Page<Storage> page = new PageImpl<>(List.of(storage));
        when(storageRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(storageMapper.toResponseList(page.getContent())).thenReturn(List.of(storageResponse));

        var result = storageService.listStorages(PageRequest.of(0, 10));

        assertThat(result).containsExactly(storageResponse);
        verify(storageRepository).findAll(ArgumentMatchers.any(PageRequest.class));
        verify(storageMapper).toResponseList(List.of(storage));
    }

    @Test
    void should_return_storage_response_when_found() {
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(storageMapper.toResponse(storage)).thenReturn(storageResponse);

        var result = storageService.getStorage(1L);

        assertThat(result).isEqualTo(storageResponse);
    }

    @Test
    void should_throw_when_storage_not_found() {
        when(storageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.getStorage(1L));
    }

    @Test
    void should_save_and_return_response_when_creating_storage() {
        when(storageMapper.toEntity(storageRequest)).thenReturn(storage);
        when(storageRepository.save(storage)).thenReturn(storage);
        when(storageMapper.toResponse(storage)).thenReturn(storageResponse);

        var result = storageService.createStorage(storageRequest);

        assertThat(result).isEqualTo(storageResponse);
        verify(storageMapper).toEntity(storageRequest);
        verify(storageRepository).save(storage);
        verify(storageMapper).toResponse(storage);
    }

    @Test
    void should_throw_conflict_when_creating_storage_with_duplicate_name() {
        when(storageRepository.existsByName("Fridge")).thenReturn(true);

        assertThatExceptionOfType(ConflictException.class)
            .isThrownBy(() -> storageService.createStorage(storageRequest))
            .withMessageContaining("Fridge");
    }

    @Test
    void should_update_existing_storage_fields_and_return_updated_response() {
        Storage updatedStorage = Storage.builder().id(1L).name("Updated Fridge").build();
        StorageResponse updatedResponse = new StorageResponse(1L, "Updated Fridge", null, false, null, null);

        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(storageRepository.save(any(Storage.class))).thenReturn(updatedStorage);
        when(storageMapper.toResponse(updatedStorage)).thenReturn(updatedResponse);

        var result = storageService.updateStorage(1L, storageRequest);

        assertThat(result).isEqualTo(updatedResponse);
        verify(storageRepository).findById(1L);
        verify(storageRepository).save(any(Storage.class));
        verify(storageMapper).toResponse(updatedStorage);
    }

    @Test
    void should_throw_when_updating_non_existent_storage() {
        when(storageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.updateStorage(99L, storageRequest));
    }

    @Test
    void should_delete_storage_when_exists() {
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));

        storageService.deleteStorage(1L);

        verify(storageRepository).findById(1L);
        verify(storageRepository).delete(storage);
    }

    @Test
    void should_throw_when_deleting_non_existent_storage() {
        when(storageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.deleteStorage(99L));
    }

    @Test
    void should_add_instance_when_storage_and_item_exist() {
        StorageItemRequest request = new StorageItemRequest(2L, 3, null);

        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item));
        when(storageMapper.toInstance(request)).thenReturn(instance);
        when(instanceRepository.save(instance)).thenReturn(instance);
        when(storageMapper.toItemResponse(instance)).thenReturn(instanceResponse);

        var result = storageService.addInstance(1L, request);

        assertThat(result).isEqualTo(instanceResponse);
        assertThat(instance.getStorage()).isEqualTo(storage);
        assertThat(instance.getItem()).isEqualTo(item);
        verify(instanceRepository).save(instance);
    }

    @Test
    void should_throw_when_adding_instance_to_non_existent_storage() {
        StorageItemRequest request = new StorageItemRequest(2L, 3, null);
        when(storageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.addInstance(99L, request))
            .withMessageContaining("Storage 99 not found");
    }

    @Test
    void should_throw_when_adding_non_existent_item_to_storage() {
        StorageItemRequest request = new StorageItemRequest(99L, 3, null);
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.addInstance(1L, request))
            .withMessageContaining("Item 99 not found");
    }

    @Test
    void should_update_instance_quantity_and_expiry() {
        StorageItemUpdateRequest updateRequest = new StorageItemUpdateRequest(5, LocalDate.of(2026, 12, 31));
        StorageItemResponse updatedResponse = new StorageItemResponse(10L, 5L, LocalDate.of(2026, 12, 31), null, null, null, null);

        when(instanceRepository.findByStorageIdAndItemId(1L, 2L)).thenReturn(Optional.of(instance));
        when(instanceRepository.save(instance)).thenReturn(instance);
        when(storageMapper.toItemResponse(instance)).thenReturn(updatedResponse);

        var result = storageService.updateInstance(1L, 2L, updateRequest);

        assertThat(result).isEqualTo(updatedResponse);
        assertThat(instance.getQuantity()).isEqualTo(5);
        assertThat(instance.getExpiryDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        verify(instanceRepository).save(instance);
    }

    @Test
    void should_throw_when_updating_non_existent_instance() {
        StorageItemUpdateRequest updateRequest = new StorageItemUpdateRequest(5, null);
        when(instanceRepository.findByStorageIdAndItemId(1L, 99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.updateInstance(1L, 99L, updateRequest));
    }

    @Test
    void should_return_stock_for_existing_storage() {
        when(storageRepository.existsById(1L)).thenReturn(true);
        when(instanceRepository.findAllByStorageId(1L)).thenReturn(List.of(instance));
        when(storageMapper.toItemResponseList(List.of(instance))).thenReturn(List.of(instanceResponse));

        var result = storageService.listStock(1L);

        assertThat(result).containsExactly(instanceResponse);
    }

    @Test
    void should_throw_when_listing_stock_of_non_existent_storage() {
        when(storageRepository.existsById(99L)).thenReturn(false);

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.listStock(99L));
    }

    @Test
    void should_return_item_from_storage_when_found() {
        when(instanceRepository.findByStorageIdAndItemId(1L, 2L)).thenReturn(Optional.of(instance));
        when(storageMapper.toItemResponse(instance)).thenReturn(instanceResponse);

        var result = storageService.getItem(1L, 2L);

        assertThat(result).isEqualTo(instanceResponse);
    }

    @Test
    void should_throw_when_item_not_in_storage() {
        when(instanceRepository.findByStorageIdAndItemId(1L, 99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.getItem(1L, 99L));
    }

    @Test
    void should_delete_instance_when_found() {
        when(instanceRepository.findByStorageIdAndItemId(1L, 2L)).thenReturn(Optional.of(instance));

        storageService.deleteInstance(1L, 2L);

        verify(instanceRepository).delete(instance);
    }

    @Test
    void should_throw_when_deleting_non_existent_instance() {
        when(instanceRepository.findByStorageIdAndItemId(1L, 99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.deleteInstance(1L, 99L));
    }
}
