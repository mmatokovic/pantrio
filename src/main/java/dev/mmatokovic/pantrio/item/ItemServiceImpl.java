package dev.mmatokovic.pantrio.item;

import dev.mmatokovic.pantrio.common.exception.NotFoundException;
import dev.mmatokovic.pantrio.item.dto.ItemRequest;
import dev.mmatokovic.pantrio.item.dto.ItemResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repo;
    private final ItemMapper mapper;

    public ItemServiceImpl(ItemRepository repo, ItemMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public List<ItemResponse> listItems(Pageable pageable) {
        return mapper.toResponseList(repo.findAll(pageable).getContent());
    }

    public ItemResponse getItem(Long id) {
        Item item = repo.findById(id)
            .orElseThrow(() -> new NotFoundException("Item " + id + " not found"));
        return mapper.toResponse(item);
    }

    public ItemResponse createItem(ItemRequest request) {
        Item item = mapper.toEntity(request);
        Item saved = repo.save(item);
        return mapper.toResponse(saved);
    }

    public ItemResponse updateItem(Long id, ItemRequest request) {
        Item item = repo.findById(id)
            .orElseThrow(() -> new NotFoundException("Item " + id + " not found"));
        item.setName(request.name());
        item.setRemarks(request.remarks());
        item.setPictureUrl(request.pictureUrl());
        item.setDisabled(request.disabled());
        Item updated = repo.save(item);
        return mapper.toResponse(updated);
    }

    public void deleteItem(Long id) {
        Item item = repo.findById(id)
            .orElseThrow(() -> new NotFoundException("Item " + id + " not found"));
        repo.delete(item);
    }
}
