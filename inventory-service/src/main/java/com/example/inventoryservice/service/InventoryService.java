package com.example.inventoryservice.service;

import com.example.inventoryservice.exception.InsufficientStockException;
import com.example.inventoryservice.exception.ResourceNotFoundException;
import com.example.inventoryservice.model.InventoryItem;
import com.example.inventoryservice.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    
    @Autowired
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }
    
    public List<InventoryItem> getAllInventoryItems() {
        return inventoryRepository.findAll();
    }
    
    public InventoryItem getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + productId));
    }
    
    public InventoryItem createInventoryItem(InventoryItem inventoryItem) {
        if (inventoryRepository.existsByProductId(inventoryItem.getProductId())) {
            throw new IllegalArgumentException("Inventory already exists for product id: " + inventoryItem.getProductId());
        }
        return inventoryRepository.save(inventoryItem);
    }
    
    public InventoryItem updateStock(Long productId, Integer newStock) {
        InventoryItem inventoryItem = getInventoryByProductId(productId);
        inventoryItem.setAvailableStock(newStock);
        return inventoryRepository.save(inventoryItem);
    }
    
    public void deleteInventoryItem(Long productId) {
        InventoryItem inventoryItem = getInventoryByProductId(productId);
        inventoryRepository.delete(inventoryItem);
    }
    
    public InventoryItem reserveStock(Long productId, Integer quantity) {
        InventoryItem inventoryItem = getInventoryByProductId(productId);
        
        if (!inventoryItem.canReserve(quantity)) {
            throw new InsufficientStockException(
                String.format("Insufficient stock for product %d. Available: %d, Requested: %d", 
                    productId, inventoryItem.getAvailableStock(), quantity));
        }
        
        inventoryItem.reserveStock(quantity);
        return inventoryRepository.save(inventoryItem);
    }
    
    public InventoryItem releaseStock(Long productId, Integer quantity) {
        InventoryItem inventoryItem = getInventoryByProductId(productId);
        
        if (inventoryItem.getReservedStock() < quantity) {
            throw new IllegalArgumentException(
                String.format("Cannot release more stock than reserved for product %d. Reserved: %d, Requested: %d", 
                    productId, inventoryItem.getReservedStock(), quantity));
        }
        
        inventoryItem.releaseStock(quantity);
        return inventoryRepository.save(inventoryItem);
    }
    
    public boolean isStockAvailable(Long productId, Integer quantity) {
        try {
            InventoryItem inventoryItem = getInventoryByProductId(productId);
            return inventoryItem.canReserve(quantity);
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
    
    public List<InventoryItem> getItemsWithAvailableStock() {
        return inventoryRepository.findItemsWithAvailableStock();
    }
    
    public List<InventoryItem> getLowStockItems(Integer threshold) {
        return inventoryRepository.findLowStockItems(threshold);
    }
}