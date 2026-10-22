package dev.mmatokovic.pantrio.domain.storage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StorageShould {

    @Test
    void create_enableStorage_whenDisabledIsMissing() {
        var storage = Storage.create("Fridge", "Kitchen", null);

        assertThat(storage)
            .extracting(Storage::getName, Storage::getDescription, Storage::isDisabled)
            .containsExactly("Fridge", "Kitchen", false);
    }

    @Test
    void update_replaceAllFields() {
        var storage = Storage.create("Fridge", "Kitchen", true);

        storage.update("Pantry", null, null);

        assertThat(storage)
            .extracting(Storage::getName, Storage::getDescription, Storage::isDisabled)
            .containsExactly("Pantry", null, false);
    }
}
