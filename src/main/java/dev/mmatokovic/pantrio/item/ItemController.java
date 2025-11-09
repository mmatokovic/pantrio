package dev.mmatokovic.pantrio.item;

import dev.mmatokovic.pantrio.item.record.ItemRequest;
import dev.mmatokovic.pantrio.item.record.ItemResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @GetMapping
    ResponseEntity<List<ItemResponse>> index() {
        return ResponseEntity.ok(service.listItems());
    }

    @PostMapping
    ResponseEntity<ItemResponse> create(@RequestBody ItemRequest request) {
        ItemResponse item = service.createItem(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(item.id())
                .toUri();

        return ResponseEntity.created(location)
                             .body(item);
    }

    @GetMapping("/{id}")
    ResponseEntity<ItemResponse> show(@PathVariable Long id) {
        return ResponseEntity.ok(service.getItem(id));
    }

    @PutMapping("/{id}")
    ResponseEntity<ItemResponse> update(@PathVariable Long id, @RequestBody ItemRequest request) {
        return ResponseEntity.ok(service.updateItem(id, request));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
