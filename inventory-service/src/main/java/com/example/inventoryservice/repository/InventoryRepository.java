package com.example.inventoryservice.repository;

import com.example.inventoryservice.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    
    Optional<InventoryItem> findByProductId(Long productId);
    
    boolean existsByProductId(Long productId);
    
    @Query("SELECT i FROM InventoryItem i WHERE i.availableStock > 0")
    List<InventoryItem> findItemsWithAvailableStock();
    
    @Query("SELECT i FROM InventoryItem i WHERE i.availableStock < :threshold")
    List<InventoryItem> findLowStockItems(Integer threshold);
}