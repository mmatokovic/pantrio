package dev.mmatokovic.pantrio.domain.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "ITEMS")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "items_seq")
    @SequenceGenerator(name = "items_seq", sequenceName = "ITEMS_SEQ", allocationSize = 50)
    @Column(name = "ID", updatable = false, nullable = false)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "REMARKS")
    private @Nullable String remarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "UNIT")
    private @Nullable UnitEnum unit;

    @Column(name = "BARCODE", unique = true)
    private @Nullable String barcode;

    @Column(name = "PICTURE_URL")
    private @Nullable String pictureUrl;

    @Column(name = "DISABLED", nullable = false)
    private boolean disabled;

    @Column(name = "ENRICHED_AT")
    private @Nullable LocalDateTime enrichedAt;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "CREATED_AT", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    public static Item create(String name,
                              @Nullable String remarks,
                              @Nullable String barcode,
                              @Nullable UnitEnum unit,
                              @Nullable String pictureUrl,
                              @Nullable Boolean disabled,
                              LocalDateTime now) {
        var item = new Item();
        item.name = name;
        item.remarks = remarks;
        item.barcode = barcode;
        item.unit = unit;
        item.pictureUrl = pictureUrl;
        item.disabled = disabled != null && disabled;
        item.createdAt = now;
        item.updatedAt = now;
        return item;
    }

    public void update(@Nullable String name,
                       @Nullable String remarks,
                       @Nullable String barcode,
                       @Nullable UnitEnum unit,
                       @Nullable String pictureUrl,
                       @Nullable Boolean disabled,
                       LocalDateTime now) {
        if (name != null) {
            this.name = name;
        }
        if (remarks != null) {
            this.remarks = remarks;
        }
        if (barcode != null) {
            this.barcode = barcode;
        }
        if (unit != null) {
            this.unit = unit;
        }
        if (pictureUrl != null) {
            this.pictureUrl = pictureUrl;
        }
        if (disabled != null) {
            this.disabled = disabled;
        }
        updatedAt = now;
    }

    public void replace(String name,
                        @Nullable String remarks,
                        @Nullable String barcode,
                        @Nullable UnitEnum unit,
                        @Nullable String pictureUrl,
                        @Nullable Boolean disabled,
                        LocalDateTime now) {
        this.name = name;
        this.remarks = remarks;
        this.barcode = barcode;
        this.unit = unit;
        this.pictureUrl = pictureUrl;
        this.disabled = disabled != null && disabled;
        updatedAt = now;
    }

    public void enrich(@Nullable String productName,
                       @Nullable String productPictureUrl,
                       @Nullable UnitEnum productUnit,
                       LocalDateTime enrichedAt) {
        if (productName != null && !productName.isBlank()) {
            name = productName;
        }
        if (productPictureUrl != null && !productPictureUrl.isBlank()) {
            pictureUrl = productPictureUrl;
        }
        if (productUnit != null) {
            unit = productUnit;
        }
        this.enrichedAt = enrichedAt;
        updatedAt = enrichedAt;
    }

    public void markEnrichmentAttempted(LocalDateTime attemptedAt) {
        enrichedAt = attemptedAt;
        updatedAt = attemptedAt;
    }
}
