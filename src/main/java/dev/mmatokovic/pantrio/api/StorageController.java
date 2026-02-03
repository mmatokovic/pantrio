package dev.mmatokovic.pantrio.api;

import dev.mmatokovic.pantrio.app.storage.StorageService;
import dev.mmatokovic.pantrio.domain.item.ItemResponse;
import dev.mmatokovic.pantrio.domain.storage.StorageRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Tag(name = "Storage")
@RequestMapping("/storages")
public class StorageController {
    private final StorageService service;

    public StorageController(StorageService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<StorageResponse>> index(
        @ParameterObject @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(service.listStorages(pageable));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<StorageResponse> create(@RequestBody StorageRequest request) {
        StorageResponse storage = service.createStorage(request);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(storage.id())
            .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<StorageResponse> show(@PathVariable Long id) {
        return ResponseEntity.ok(service.getStorage(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<StorageResponse> update(@PathVariable Long id, @RequestBody StorageRequest request) {
        service.updateStorage(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteStorage(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{storageId}/items", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<StorageResponse> addItem(
            @RequestBody StorageRequest request,
            @PathVariable Long storageId
    ) {
        StorageResponse storage = service.addItem(request);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{storageId}/items/{itemId}")
            .buildAndExpand(storage.id())
            .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping(value = "/{storageId}/items/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemResponse>> getItem(
            @PathVariable Long storageId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(service.listItem(storageId, itemId));
    }

    @GetMapping(value = "/{storageId}/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemResponse>> getItems(@PathVariable Long storageId) {
        return ResponseEntity.ok(service.listItems(storageId));
    }

    @PatchMapping(value = "/{storageId}/items/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemResponse> consume(
        @PathVariable Long storageId,
        @PathVariable Long itemId,
        @RequestBody UpdateQuantityRequest request
    ) {
        ItemResponse updated = service.updateQuantity(storageId, itemId, request.quantity());
        return ResponseEntity.ok(updated);
    }
}
