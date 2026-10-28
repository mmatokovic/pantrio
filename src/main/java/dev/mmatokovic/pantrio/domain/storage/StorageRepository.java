package dev.mmatokovic.pantrio.domain.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {

    boolean existsByName(String name);

    Slice<Storage> findAllBy(Pageable pageable);
}
