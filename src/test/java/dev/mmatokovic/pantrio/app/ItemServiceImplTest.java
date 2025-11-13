package dev.mmatokovic.pantrio.app;

import dev.mmatokovic.pantrio.common.exception.NotFoundException;
import dev.mmatokovic.pantrio.item.Item;
import dev.mmatokovic.pantrio.item.ItemRepository;
import dev.mmatokovic.pantrio.item.ItemServiceImpl;
import dev.mmatokovic.pantrio.item.mapper.ItemMapper;
import dev.mmatokovic.pantrio.item.record.ItemRequest;
import dev.mmatokovic.pantrio.item.record.ItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemServiceImplTest {

    @Mock
    private ItemRepository repo;

    @Mock
    private ItemMapper mapper;

    @InjectMocks
    private ItemServiceImpl service;

    private Item item;
    private ItemRequest request;
    private ItemResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        item = new Item(1L, "item", "remarks", "pic.png", false);
        request = new ItemRequest("item", "remarks", "pic.png", false);
        response = new ItemResponse(1L, "item", "remarks", "pic.png", false);
    }

    @Test
    void should_return_items_when_listing_with_pagination() {
        Page<Item> page = new PageImpl<>(List.of(item));
        when(repo.findAll(any(PageRequest.class))).thenReturn(page);
        when(mapper.toResponseList(anyList())).thenReturn(List.of(response));

        var result = service.listItems(PageRequest.of(0, 10));

        assertThat(result).containsExactly(response);
        verify(repo).findAll(any(PageRequest.class));
        verify(mapper).toResponseList(List.of(item));
    }

    @Test
    void should_return_item_response_when_item_exists() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getItem(1L));
    }

    @Test
    void throw_NotFoundException_when_item_does_not_exist() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getItem(1L));
    }

    @Test
    void should_save_and_return_response_when_creating_item() {
        when(mapper.toEntity(request)).thenReturn(item);
        when(repo.save(item)).thenReturn(item);
        when(mapper.toResponse(item)).thenReturn(response);

        var result = service.createItem(request);

        assertThat(result).isEqualTo(response);
        verify(repo).save(item);
    }

    @Test
    void should_update_existing_item_fields_and_return_updated_response() {
        when(repo.findById(1L)).thenReturn(Optional.of(item));
        when(repo.save(item)).thenReturn(item);
        when(mapper.toResponse(item)).thenReturn(response);

        var result = service.updateItem(1L, request);

        assertThat(result).isEqualTo(response);
        assertThat(item.getName()).isEqualTo("item");
        verify(repo).save(item);
    }

    @Test
    void should_delete_item_when_item_exists() {
        when(repo.findById(1L)).thenReturn(Optional.of(item));

        service.deleteItem(1L);

        verify(repo).delete(item);
    }

    @Test
    void should_throw_NotFoundException_when_deleting_nonexistent_item() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.deleteItem(1L));
    }
}
