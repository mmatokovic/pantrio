package dev.mmatokovic.pantrio.domain.storage;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class StorageShould {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 28, 12, 0);

    @Test
    void create_enableStorage_whenDisabledIsMissing() {
        var storage = Storage.create("Fridge", "Kitchen", null, NOW);

        assertThat(storage)
            .extracting(Storage::getName, Storage::getDescription, Storage::isDisabled)
            .containsExactly("Fridge", "Kitchen", false);
    }

    @Test
    void update_replaceAllFields() {
        var storage = Storage.create("Fridge", "Kitchen", true, NOW);

        storage.update("Pantry", null, null, NOW);

        assertThat(storage)
            .extracting(Storage::getName, Storage::getDescription, Storage::isDisabled)
            .containsExactly("Pantry", null, false);
    }
}
