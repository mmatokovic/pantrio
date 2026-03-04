package dev.mmatokovic.pantrio.domain.instance;

import dev.mmatokovic.pantrio.domain.item.Item;
import dev.mmatokovic.pantrio.domain.storage.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstanceRepository extends JpaRepository<Instance, Long> {

    boolean existsByStorageAndItem(Storage storage, Item item);
}
