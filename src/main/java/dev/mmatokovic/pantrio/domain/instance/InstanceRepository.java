package dev.mmatokovic.pantrio.domain.instance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstanceRepository extends JpaRepository<Instance, Long> {
    Optional<Instance> findByStorageIdAndItemId(Long storageId, Long itemId);
}
