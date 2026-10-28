package dev.mmatokovic.pantrio.api;

import dev.mmatokovic.pantrio.app.storage.StorageService;
import dev.mmatokovic.pantrio.domain.storage.StorageItemRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageItemResponse;
import dev.mmatokovic.pantrio.domain.storage.StorageItemUpdateRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping(path = "/storages", version = "v1+")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StorageResponse>> index(@ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(storageService.listStorages(pageable));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StorageResponse> create(@Valid @RequestBody StorageRequest request) {
        StorageResponse storage = storageService.createStorage(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(storage.id()).toUri();

        return ResponseEntity.created(location).body(storage);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StorageResponse> show(@PathVariable Long id) {
        return ResponseEntity.ok(storageService.getStorage(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StorageResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody StorageRequest request) {
        return ResponseEntity.ok(storageService.updateStorage(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<@Nullable Void> delete(@PathVariable Long id) {
        storageService.deleteStorage(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{storageId}/items", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StorageItemResponse> addItem(@PathVariable Long storageId,
                                                       @Valid @RequestBody StorageItemRequest request) {
        StorageItemResponse instance = storageService.addInstance(storageId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{itemId}").buildAndExpand(request.itemId()).toUri();
        return ResponseEntity.created(location).body(instance);
    }

    @GetMapping(value = "/{storageId}/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StorageItemResponse>> getStock(@PathVariable Long storageId) {
        return ResponseEntity.ok(storageService.listStock(storageId));
    }

    @GetMapping(value = "/{storageId}/items/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StorageItemResponse> showItem(@PathVariable Long storageId,
                                                        @PathVariable Long itemId) {
        return ResponseEntity.ok(storageService.getItem(storageId, itemId));
    }

    @PutMapping(value = "/{storageId}/items/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StorageItemResponse> restockItem(@PathVariable Long storageId,
                                                           @PathVariable Long itemId,
                                                           @Valid @RequestBody StorageItemUpdateRequest request) {
        return ResponseEntity.ok(storageService.updateInstance(storageId, itemId, request));
    }

    @DeleteMapping("/{storageId}/items/{itemId}")
    public ResponseEntity<@Nullable Void> consumeItem(@PathVariable Long storageId,
                                             @PathVariable Long itemId) {
        storageService.deleteInstance(storageId, itemId);
        return ResponseEntity.noContent().build();
    }
}
