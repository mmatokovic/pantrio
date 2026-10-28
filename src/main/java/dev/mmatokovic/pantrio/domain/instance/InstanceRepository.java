package dev.mmatokovic.pantrio.domain.instance;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstanceRepository extends JpaRepository<Instance, Long> {

    @EntityGraph(attributePaths = {"item", "storage"})
    List<Instance> findAllByStorageId(Long storageId);

    @EntityGraph(attributePaths = {"item", "storage"})
    Optional<Instance> findByStorageIdAndItemId(Long storageId, Long itemId);
}
