package dev.mmatokovic.pantrio.app.item;

import dev.mmatokovic.pantrio.domain.item.Item;
import dev.mmatokovic.pantrio.domain.item.ItemRepository;
import dev.mmatokovic.pantrio.domain.item.ItemRequest;
import dev.mmatokovic.pantrio.domain.item.ItemResponse;
import jakarta.persistence.EntityNotFoundException;
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

    @Override
    public ItemResponse getItem(Long id) {
        Item item = repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Item " + id + " not found"));
        return mapper.toResponse(item);
    }

    @Override
    public ItemResponse createItem(ItemRequest request) {
        Item item = mapper.toEntity(request);
        Item saved = repo.save(item);
        return mapper.toResponse(saved);
    }

    @Override
    public ItemResponse updateItem(Long id, ItemRequest request) {
        Item item = repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Item " + id + " not found"));
        item.setName(request.name());
        item.setRemarks(request.remarks());
        item.setBarcode(request.barcode());
        item.setUnit(request.unit().name().toLowerCase());
        item.setPictureUrl(request.pictureUrl());
        item.setDisabled(request.disabled());
        Item updated = repo.save(item);
        return mapper.toResponse(updated);
    }

    @Override
    public void deleteItem(Long id) {
        Item item = repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Item " + id + " not found"));
        repo.delete(item);
    }
}
