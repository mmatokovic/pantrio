package dev.mmatokovic.pantrio.item;

import dev.mmatokovic.pantrio.common.exception.NotFoundException;
import dev.mmatokovic.pantrio.item.mapper.ItemMapper;
import dev.mmatokovic.pantrio.item.record.ItemRequest;
import dev.mmatokovic.pantrio.item.record.ItemResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {
    private final ItemRepository repo;
    private final ItemMapper mapper;

    public ItemService(ItemRepository repo, ItemMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public List<ItemResponse> listItems() {
        return mapper.toResponseList(repo.findAll());
    }

    public ItemResponse getItem(Long id) {
        Item item = repo.findById(id)
                .orElseThrow(NotFoundException::new);
        return mapper.toResponse(item);
    }

    public ItemResponse createItem(ItemRequest request) {
        Item item = mapper.toEntity(request);
        Item saved = repo.save(item);
        return mapper.toResponse(saved);
    }

    public ItemResponse updateItem(Long id, ItemRequest request) {
        Item item = repo.findById(id)
            .orElseThrow(NotFoundException::new);
        item.setName(request.name());
        item.setRemarks(request.remarks());
        item.setPictureUrl(request.pictureUrl());
        item.setStatus(request.status());
        item.setDisabled(request.disabled());
        Item updated = repo.save(item);
        return mapper.toResponse(updated);
    }

    public void deleteItem(Long id) {
        Item item = repo.findById(id)
            .orElseThrow(NotFoundException::new);
        repo.delete(item);
    }
}
