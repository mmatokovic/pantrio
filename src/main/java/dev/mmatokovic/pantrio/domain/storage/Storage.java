package dev.mmatokovic.pantrio.domain.storage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "STORAGE")
public class Storage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "storage_seq")
    @SequenceGenerator(name = "storage_seq", sequenceName = "STORAGE_SEQ", allocationSize = 50)
    @Column(name = "ID", updatable = false, nullable = false)
    private Long id;

    @Column(name = "NAME", unique = true, nullable = false)
    private String name;

    @Column(name = "DESCRIPTION")
    private @Nullable String description;

    @Column(name = "DISABLED", nullable = false)
    private boolean disabled;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "CREATED_AT", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    public static Storage create(String name, @Nullable String description, @Nullable Boolean disabled, LocalDateTime now) {
        var storage = new Storage();
        storage.name = name;
        storage.description = description;
        storage.disabled = disabled != null && disabled;
        storage.createdAt = now;
        storage.updatedAt = now;
        return storage;
    }

    public void update(String name, @Nullable String description, @Nullable Boolean disabled, LocalDateTime now) {
        this.name = name;
        this.description = description;
        this.disabled = disabled != null && disabled;
        updatedAt = now;
    }
}
