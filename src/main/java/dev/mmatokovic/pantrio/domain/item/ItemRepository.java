package dev.mmatokovic.pantrio.domain.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @SuppressWarnings("unused")
    Page<Item> findByBarcodeIsNotNullAndPictureUrlIsNull(Pageable pageable);
}
