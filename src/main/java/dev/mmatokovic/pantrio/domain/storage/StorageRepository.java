package dev.mmatokovic.pantrio.domain.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {

    boolean existsByName(String name);
}
