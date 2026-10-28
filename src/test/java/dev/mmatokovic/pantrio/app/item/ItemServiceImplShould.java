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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 28, 12, 0);
    private static final Clock CLOCK = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    @Mock
    private ItemRepository itemRepository;

    @Test
    void listItems_returnMappedPage() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl(), CLOCK);
        var item = Item.create("Milk", null, "8000500179864", UnitEnum.L, null, false, NOW);
        when(itemRepository.findAllBy(PageRequest.of(0, 10))).thenReturn(new SliceImpl<>(List.of(item)));

        var response = itemService.listItems(PageRequest.of(0, 10));

        assertThat(response).isEqualTo(List.of(new ItemResponse(null, "Milk", null, "8000500179864", UnitEnum.L, null, false, NOW, NOW)));
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void getItem_returnMappedItem() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl(), CLOCK);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(Item.create("Milk", "Fresh", null, UnitEnum.L, null, true, NOW)));

        var response = itemService.getItem(1L);

        assertThat(response).isEqualTo(new ItemResponse(null, "Milk", "Fresh", null, UnitEnum.L, null, true, NOW, NOW));
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void getItem_throwEntityNotFound_whenItemDoesNotExist() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl(), CLOCK);
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> itemService.getItem(1L))
            .withMessage("Item 1 not found");
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void createItem_saveNewItemFromRequest() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl(), CLOCK);
        // A new entity has no identity to stub with, so the repository echoes what it is given
        when(itemRepository.save(any(Item.class))).then(returnsFirstArg());

        var response = itemService.createItem(new ItemRequest("Milk", null, "8000500179864", UnitEnum.PCS, null, null));

        assertThat(response).isEqualTo(new ItemResponse(null, "Milk", null, "8000500179864", UnitEnum.PCS, null, false, NOW, NOW));
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void updateItem_applyRequestToExistingItem() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl(), CLOCK);
        var item = Item.create("Milk", "Fresh", "8000500179864", UnitEnum.L, null, false, NOW);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        var response = itemService.updateItem(1L, new ItemRequest("Oat milk", null, null, null, null, true));

        assertThat(response).isEqualTo(new ItemResponse(null, "Oat milk", "Fresh", "8000500179864", UnitEnum.L, null, true, NOW, NOW));
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void replaceItem_overwriteEveryField() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl(), CLOCK);
        var item = Item.create("Milk", "Fresh", "8000500179864", UnitEnum.L, "https://example.com/milk.jpg", true, NOW);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        var response = itemService.replaceItem(1L, new ItemRequest("Oat milk", null, null, null, null, null));

        assertThat(response).isEqualTo(new ItemResponse(null, "Oat milk", null, null, null, null, false, NOW, NOW));
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void replaceItem_throwEntityNotFound_whenItemDoesNotExist() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl(), CLOCK);
        var request = new ItemRequest("Oat milk", null, null, null, null, null);
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> itemService.replaceItem(1L, request))
            .withMessage("Item 1 not found");
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void updateItem_throwEntityNotFound_whenItemDoesNotExist() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl(), CLOCK);
        var request = new ItemRequest("Oat milk", null, null, null, null, false);
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> itemService.updateItem(1L, request))
            .withMessage("Item 1 not found");
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void deleteItem_deleteExistingItem() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl(), CLOCK);
        var item = Item.create("Milk", null, null, null, null, false, NOW);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.deleteItem(1L);

        verify(itemRepository).delete(item);
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void deleteItem_throwEntityNotFound_whenItemDoesNotExist() {
        var itemService = new ItemServiceImpl(itemRepository, new ItemMapperImpl(), CLOCK);
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> itemService.deleteItem(1L))
            .withMessage("Item 1 not found");
        verifyNoMoreInteractions(itemRepository);
    }
}
