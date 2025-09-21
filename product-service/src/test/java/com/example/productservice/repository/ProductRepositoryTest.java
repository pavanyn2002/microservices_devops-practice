package com.example.productservice.repository;

import com.example.productservice.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Test
    void testFindByCategory() {
        // Given
        Product product1 = new Product("Laptop", "Gaming laptop", new BigDecimal("999.99"), "Electronics");
        Product product2 = new Product("Phone", "Smartphone", new BigDecimal("599.99"), "Electronics");
        Product product3 = new Product("Book", "Programming book", new BigDecimal("29.99"), "Books");
        
        entityManager.persistAndFlush(product1);
        entityManager.persistAndFlush(product2);
        entityManager.persistAndFlush(product3);
        
        // When
        List<Product> electronicsProducts = productRepository.findByCategory("Electronics");
        
        // Then
        assertEquals(2, electronicsProducts.size());
        assertTrue(electronicsProducts.stream().allMatch(p -> "Electronics".equals(p.getCategory())));
    }
    
    @Test
    void testFindByCategoryIgnoreCase() {
        // Given
        Product product = new Product("Laptop", "Gaming laptop", new BigDecimal("999.99"), "Electronics");
        entityManager.persistAndFlush(product);
        
        // When
        List<Product> products = productRepository.findByCategoryIgnoreCase("electronics");
        
        // Then
        assertEquals(1, products.size());
        assertEquals("Electronics", products.get(0).getCategory());
    }
    
    @Test
    void testFindByNameContainingIgnoreCase() {
        // Given
        Product product1 = new Product("Gaming Laptop", "High-end gaming laptop", new BigDecimal("1299.99"), "Electronics");
        Product product2 = new Product("Business Laptop", "Professional laptop", new BigDecimal("899.99"), "Electronics");
        Product product3 = new Product("Smartphone", "Latest smartphone", new BigDecimal("699.99"), "Electronics");
        
        entityManager.persistAndFlush(product1);
        entityManager.persistAndFlush(product2);
        entityManager.persistAndFlush(product3);
        
        // When
        List<Product> laptopProducts = productRepository.findByNameContainingIgnoreCase("laptop");
        
        // Then
        assertEquals(2, laptopProducts.size());
        assertTrue(laptopProducts.stream().allMatch(p -> p.getName().toLowerCase().contains("laptop")));
    }
    
    @Test
    void testFindByCategoryNotFound() {
        // When
        List<Product> products = productRepository.findByCategory("NonExistentCategory");
        
        // Then
        assertTrue(products.isEmpty());
    }
}