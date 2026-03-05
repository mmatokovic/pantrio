package dev.mmatokovic.pantrio.storage;

import dev.mmatokovic.pantrio.app.storage.StorageMapper;
import dev.mmatokovic.pantrio.app.storage.StorageServiceImpl;
import dev.mmatokovic.pantrio.domain.storage.Storage;
import dev.mmatokovic.pantrio.domain.storage.StorageRepository;
import dev.mmatokovic.pantrio.domain.storage.StorageRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageResponse;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class StorageServiceImplTest {

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private StorageMapper storageMapper;

    @InjectMocks
    private StorageServiceImpl storageService;

    private Storage storage;
    private StorageRequest storageRequest;
    private StorageResponse storageResponse;

    @BeforeEach
    void setUp() {
        storage = Storage.builder().id(1L).name("Fridge").build();
        storageRequest = new StorageRequest("Fridge", null, false);
        storageResponse = new StorageResponse(1L, "Fridge", null, false);
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
    void should_return_storage_response_when_exists() {
        when(storageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> storageService.getStorage(1L));
    }

    @Test
    void should_save_and_return_response_when_creating_item() {
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
    void should_update_existing_storage_fields_and_return_updated_response() {
        Storage updatedStorage = Storage.builder().id(1L).name("Updated Fridge").build();
        StorageResponse updatedResponse = new StorageResponse(1L, "Updated Fridge", null, false);

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
    void should_delete_storage_when_item_exists() {
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));

        storageService.deleteStorage(1L);

        verify(storageRepository).findById(1L);
        verify(storageRepository).delete(storage);
    }
}
