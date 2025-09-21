package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.StockReservationRequest;
import com.example.inventoryservice.model.InventoryItem;
import com.example.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    @GetMapping
    public ResponseEntity<List<InventoryItem>> getAllInventoryItems() {
        List<InventoryItem> items = inventoryService.getAllInventoryItems();
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryItem> getInventoryByProductId(@PathVariable Long productId) {
        InventoryItem item = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(item);
    }
    
    @PostMapping
    public ResponseEntity<InventoryItem> createInventoryItem(@Valid @RequestBody InventoryItem inventoryItem) {
        InventoryItem createdItem = inventoryService.createInventoryItem(inventoryItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }
    
    @PutMapping("/{productId}")
    public ResponseEntity<InventoryItem> updateStock(@PathVariable Long productId, @RequestParam Integer stock) {
        InventoryItem updatedItem = inventoryService.updateStock(productId, stock);
        return ResponseEntity.ok(updatedItem);
    }
    
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable Long productId) {
        inventoryService.deleteInventoryItem(productId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{productId}/reserve")
    public ResponseEntity<InventoryItem> reserveStock(@PathVariable Long productId, 
                                                     @Valid @RequestBody StockReservationRequest request) {
        InventoryItem updatedItem = inventoryService.reserveStock(productId, request.getQuantity());
        return ResponseEntity.ok(updatedItem);
    }
    
    @PostMapping("/{productId}/release")
    public ResponseEntity<InventoryItem> releaseStock(@PathVariable Long productId, 
                                                     @Valid @RequestBody StockReservationRequest request) {
        InventoryItem updatedItem = inventoryService.releaseStock(productId, request.getQuantity());
        return ResponseEntity.ok(updatedItem);
    }
    
    @GetMapping("/{productId}/available")
    public ResponseEntity<Boolean> checkStockAvailability(@PathVariable Long productId, @RequestParam Integer quantity) {
        boolean available = inventoryService.isStockAvailable(productId, quantity);
        return ResponseEntity.ok(available);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<InventoryItem>> getItemsWithAvailableStock() {
        List<InventoryItem> items = inventoryService.getItemsWithAvailableStock();
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItem>> getLowStockItems(@RequestParam(defaultValue = "10") Integer threshold) {
        List<InventoryItem> items = inventoryService.getLowStockItems(threshold);
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Inventory Service is healthy");
    }
}