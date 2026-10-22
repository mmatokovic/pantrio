package dev.mmatokovic.pantrio.domain.instance;

import dev.mmatokovic.pantrio.domain.item.Item;
import dev.mmatokovic.pantrio.domain.storage.Storage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "ITEM_INSTANCE",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_instance_storage_item",
            columnNames = {"STORAGE_ID", "ITEM_ID"}
        )
    }
)
public class Instance {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_instance_seq")
    @SequenceGenerator(name = "item_instance_seq", sequenceName = "ITEM_INSTANCE_SEQ", allocationSize = 50)
    @Column(name = "ID", updatable = false, nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "ITEM_ID", nullable = false)
    private Item item;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "STORAGE_ID", nullable = false)
    private Storage storage;

    @Column(name = "QUANTITY")
    private Integer quantity;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    public static Instance create(Storage storage, Item item, Integer quantity, @Nullable LocalDate expiryDate) {
        var instance = new Instance();
        instance.storage = storage;
        instance.item = item;
        instance.quantity = quantity;
        instance.expiryDate = expiryDate;
        return instance;
    }

    public void restock(Integer quantity, @Nullable LocalDate expiryDate) {
        this.quantity = quantity;
        this.expiryDate = expiryDate;
    }
}
