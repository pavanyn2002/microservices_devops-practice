package com.example.inventoryservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Product ID is required")
    @Column(unique = true)
    private Long productId;
    
    @Min(value = 0, message = "Available stock must be greater than or equal to 0")
    private Integer availableStock;
    
    @Min(value = 0, message = "Reserved stock must be greater than or equal to 0")
    private Integer reservedStock;
    
    private LocalDateTime lastUpdated;
    
    public InventoryItem() {
        this.availableStock = 0;
        this.reservedStock = 0;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public InventoryItem(Long productId, Integer availableStock) {
        this.productId = productId;
        this.availableStock = availableStock;
        this.reservedStock = 0;
        this.lastUpdated = LocalDateTime.now();
    }
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
    
    public Integer getTotalStock() {
        return availableStock + reservedStock;
    }
    
    public boolean canReserve(Integer quantity) {
        return availableStock >= quantity;
    }
    
    public void reserveStock(Integer quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalArgumentException("Insufficient available stock");
        }
        availableStock -= quantity;
        reservedStock += quantity;
    }
    
    public void releaseStock(Integer quantity) {
        if (reservedStock < quantity) {
            throw new IllegalArgumentException("Cannot release more stock than reserved");
        }
        reservedStock -= quantity;
        availableStock += quantity;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Integer getAvailableStock() {
        return availableStock;
    }
    
    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }
    
    public Integer getReservedStock() {
        return reservedStock;
    }
    
    public void setReservedStock(Integer reservedStock) {
        this.reservedStock = reservedStock;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}