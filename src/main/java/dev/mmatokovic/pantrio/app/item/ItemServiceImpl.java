package dev.mmatokovic.pantrio.app.item;

import dev.mmatokovic.pantrio.domain.item.Item;
import dev.mmatokovic.pantrio.domain.item.ItemRepository;
import dev.mmatokovic.pantrio.domain.item.ItemRequest;
import dev.mmatokovic.pantrio.domain.item.ItemResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> listItems(Pageable pageable) {
        return itemMapper.toResponseList(itemRepository.findAll(pageable).getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItem(Long id) {
        return itemMapper.toResponse(findItem(id));
    }

    @Override
    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        var item = Item.create(request.name(), request.remarks(), request.barcode(), request.unit(), request.pictureUrl(), request.disabled());
        return itemMapper.toResponse(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request) {
        var item = findItem(id);
        item.update(request.name(), request.remarks(), request.barcode(), request.unit(), request.pictureUrl(), request.disabled());
        return itemMapper.toResponse(itemRepository.save(item));
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        itemRepository.delete(findItem(id));
    }

    private Item findItem(Long id) {
        return itemRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Item " + id + " not found"));
    }
}
