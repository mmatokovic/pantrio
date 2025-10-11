package dev.mmatokovic.pantrio.item;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        ItemResponse created = service.createItem(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location)
                             .body(created);
    }

    @GetMapping("/{id}")
    ResponseEntity<ItemResponse> show(@PathVariable Long id) {
        return ResponseEntity.ok(service.getItem(id));
    }
}
