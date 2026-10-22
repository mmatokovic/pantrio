package dev.mmatokovic.pantrio.app.item;

import dev.mmatokovic.pantrio.domain.item.Item;
import dev.mmatokovic.pantrio.domain.item.ItemRepository;
import dev.mmatokovic.pantrio.domain.item.ItemRequest;
import dev.mmatokovic.pantrio.domain.item.ItemResponse;
import dev.mmatokovic.pantrio.domain.item.UnitEnum;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplShould {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void listItems_returnMappedPage() {
        var item = Item.builder().id(1L).name("Milk").build();
        var itemResponse = new ItemResponse(1L, "Milk", null, null, null, null, false, null, null);
        when(itemRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(item)));
        when(itemMapper.toResponseList(List.of(item))).thenReturn(List.of(itemResponse));

        var response = itemService.listItems(PageRequest.of(0, 10));

        assertThat(response).isEqualTo(List.of(itemResponse));
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }

    @Test
    void getItem_returnMappedItem() {
        var item = Item.builder().id(1L).name("Milk").build();
        var itemResponse = new ItemResponse(1L, "Milk", null, null, null, null, false, null, null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toResponse(item)).thenReturn(itemResponse);

        var response = itemService.getItem(1L);

        assertThat(response).isEqualTo(itemResponse);
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }

    @Test
    void getItem_throwEntityNotFound_whenItemDoesNotExist() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> itemService.getItem(1L))
            .withMessage("Item 1 not found");
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }

    @Test
    void createItem_saveMappedEntity() {
        var request = new ItemRequest("Milk", null, "8000500179864", UnitEnum.PCS, null, false);
        var item = Item.builder().name("Milk").barcode("8000500179864").unit(UnitEnum.PCS).build();
        var itemResponse = new ItemResponse(1L, "Milk", null, "8000500179864", "pcs", null, false, null, null);
        when(itemMapper.toEntity(request)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toResponse(item)).thenReturn(itemResponse);

        var response = itemService.createItem(request);

        assertThat(response).isEqualTo(itemResponse);
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }

    @Test
    void updateItem_applyRequestToExistingItem() {
        var request = new ItemRequest("Oat milk", null, null, null, null, false);
        var item = Item.builder().id(1L).name("Milk").build();
        var itemResponse = new ItemResponse(1L, "Oat milk", null, null, null, null, false, null, null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toResponse(item)).thenReturn(itemResponse);

        var response = itemService.updateItem(1L, request);

        assertThat(response).isEqualTo(itemResponse);
        verify(itemMapper).updateItemFromRequest(request, item);
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }

    @Test
    void updateItem_throwEntityNotFound_whenItemDoesNotExist() {
        var request = new ItemRequest("Oat milk", null, null, null, null, false);
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> itemService.updateItem(1L, request))
            .withMessage("Item 1 not found");
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }

    @Test
    void deleteItem_deleteExistingItem() {
        var item = Item.builder().id(1L).name("Milk").build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.deleteItem(1L);

        verify(itemRepository).delete(item);
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }

    @Test
    void deleteItem_throwEntityNotFound_whenItemDoesNotExist() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> itemService.deleteItem(1L))
            .withMessage("Item 1 not found");
        verifyNoMoreInteractions(itemRepository, itemMapper);
    }
}
