package dev.mmatokovic.pantrio.app.item;

import dev.mmatokovic.pantrio.domain.item.Item;
import dev.mmatokovic.pantrio.domain.item.ItemRepository;
import dev.mmatokovic.pantrio.domain.item.ItemRequest;
import dev.mmatokovic.pantrio.domain.item.ItemResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repo;
    private final ItemMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> listItems(Pageable pageable) {
        return mapper.toResponseList(repo.findAll(pageable).getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItem(Long id) {
        Item item = repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Item " + id + " not found"));
        return mapper.toResponse(item);
    }

    @Override
    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        return mapper.toResponse(repo.save(mapper.toEntity(request)));
    }

    @Override
    @Transactional
    public ItemResponse updateItem(Long id, @NonNull ItemRequest request) {
        Item item = repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Item " + id + " not found"));
        mapper.updateItemFromRequest(request, item);
        return mapper.toResponse(repo.save(item));
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        repo.delete(repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Item " + id + " not found")));
    }
}
