package dev.mmatokovic.pantrio.domain.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ITEMS")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String remarks;
    private String unit;
    private String barcode;
    private String pictureUrl;
    @Column(nullable = false)
    private Boolean disabled = false;
    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    public Item() {

    }

    public Item(Long id, String name, String remarks, String barcode, String unit, String pictureUrl, Boolean disabled) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.remarks = remarks;
        this.unit = unit;
        this.pictureUrl = pictureUrl;
        this.disabled = disabled;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getRemarks() {
        return this.remarks;
    }

    public String getUnit() {
        return this.unit;
    }

    public String getBarcode() {
        return this.barcode;
    }

    public String getPictureUrl() {
        return this.pictureUrl;
    }

    public Boolean getDisabled() {
        return this.disabled;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
