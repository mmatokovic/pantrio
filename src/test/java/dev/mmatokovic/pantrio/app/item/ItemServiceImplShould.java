package dev.mmatokovic.pantrio.app.item;

import dev.mmatokovic.pantrio.domain.item.Item;
import dev.mmatokovic.pantrio.domain.item.ItemRepository;
import dev.mmatokovic.pantrio.domain.item.ItemRequest;
import dev.mmatokovic.pantrio.domain.item.ItemResponse;
import dev.mmatokovic.pantrio.domain.item.UnitEnum;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplShould {

    @Mock
    private ItemRepository itemRepository;

    @Test
    void listItems_returnMappedPage() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl());
        var item = Item.create("Milk", null, "8000500179864", UnitEnum.L, null, false);
        when(itemRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(item)));

        var response = itemService.listItems(PageRequest.of(0, 10));

        assertThat(response).isEqualTo(List.of(new ItemResponse(null, "Milk", null, "8000500179864", UnitEnum.L, null, false, null, null)));
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void getItem_returnMappedItem() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl());
        when(itemRepository.findById(1L)).thenReturn(Optional.of(Item.create("Milk", "Fresh", null, UnitEnum.L, null, true)));

        var response = itemService.getItem(1L);

        assertThat(response).isEqualTo(new ItemResponse(null, "Milk", "Fresh", null, UnitEnum.L, null, true, null, null));
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void getItem_throwEntityNotFound_whenItemDoesNotExist() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl());
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> itemService.getItem(1L))
            .withMessage("Item 1 not found");
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void createItem_saveNewItemFromRequest() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl());
        // A new entity has no identity to stub with, so the repository echoes what it is given
        when(itemRepository.save(any(Item.class))).then(returnsFirstArg());

        var response = itemService.createItem(new ItemRequest("Milk", null, "8000500179864", UnitEnum.PCS, null, null));

        assertThat(response).isEqualTo(new ItemResponse(null, "Milk", null, "8000500179864", UnitEnum.PCS, null, false, null, null));
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void updateItem_applyRequestToExistingItem() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl());
        var item = Item.create("Milk", "Fresh", "8000500179864", UnitEnum.L, null, false);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        var response = itemService.updateItem(1L, new ItemRequest("Oat milk", null, null, null, null, true));

        assertThat(response).isEqualTo(new ItemResponse(null, "Oat milk", "Fresh", "8000500179864", UnitEnum.L, null, true, null, null));
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void updateItem_throwEntityNotFound_whenItemDoesNotExist() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl());
        var request = new ItemRequest("Oat milk", null, null, null, null, false);
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> itemService.updateItem(1L, request))
            .withMessage("Item 1 not found");
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void deleteItem_deleteExistingItem() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl());
        var item = Item.create("Milk", null, null, null, null, false);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.deleteItem(1L);

        verify(itemRepository).delete(item);
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void deleteItem_throwEntityNotFound_whenItemDoesNotExist() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl());
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> itemService.deleteItem(1L))
            .withMessage("Item 1 not found");
        verifyNoMoreInteractions(itemRepository);
    }
}
