package dev.mmatokovic.pantrio.item;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {
    private final ItemRepository repo;

    public ItemService(ItemRepository repo) {
        this.repo = repo;
    }

    public List<ItemResponse> listItems() {
        return repo.findAll()
                .stream()
                .map(ItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ItemResponse getItem(Long id) {
        Item item = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));
        return ItemMapper.toResponse(item);
    }

    public ItemResponse createItem(ItemRequest request) {
        Item item = ItemMapper.toEntity(request);
        Item saved = repo.save(item);
        return ItemMapper.toResponse(saved);
    }
}
