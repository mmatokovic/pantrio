# Pantrio

One-stop comestibles management solution.

Pantrio helps you manage pantry inventory with modern practices, using CQRS at the API level to separate commands (actions) from queries (read operations), combined with a responsive UI powered by HTMX.

```java
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
            @RequestBody UpdateQuantityRequest request
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
```

## Author Information
[Mislav Matoković](https://github.com/mmatokovi)
