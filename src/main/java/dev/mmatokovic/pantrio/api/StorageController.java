package dev.mmatokovic.pantrio.api;

import dev.mmatokovic.pantrio.app.storage.StorageService;
import dev.mmatokovic.pantrio.domain.storage.StorageItemRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageItemResponse;
import dev.mmatokovic.pantrio.domain.storage.StorageRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/storages")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<StorageResponse>> index(@ParameterObject @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(service.listStorages(pageable));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<StorageResponse> create(@Valid @RequestBody StorageRequest request) {
        StorageResponse storage = service.createStorage(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(storage.id()).toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<StorageResponse> show(@PathVariable Long id) {
        return ResponseEntity.ok(service.getStorage(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<StorageResponse> update(@PathVariable Long id, @Valid @RequestBody StorageRequest request) {
        service.updateStorage(id, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteStorage(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{storageId}/items", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<StorageItemResponse> addItem(@PathVariable Long storageId, @Valid @RequestBody StorageItemRequest request) {
        StorageItemResponse instance = service.addInstance(storageId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(instance.id()).toUri();

        return ResponseEntity.created(location).build();
    }
}
