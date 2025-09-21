package com.example.productservice.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidProduct() {
        Product product = new Product("Test Product", "Test Description", new BigDecimal("29.99"), "Electronics");
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertTrue(violations.isEmpty());
        assertEquals("Test Product", product.getName());
        assertEquals("Test Description", product.getDescription());
        assertEquals(new BigDecimal("29.99"), product.getPrice());
        assertEquals("Electronics", product.getCategory());
        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
    }
    
    @Test
    void testProductWithBlankName() {
        Product product = new Product("", "Test Description", new BigDecimal("29.99"), "Electronics");
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Product name is required")));
    }
    
    @Test
    void testProductWithNullPrice() {
        Product product = new Product("Test Product", "Test Description", null, "Electronics");
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Price is required")));
    }
    
    @Test
    void testProductWithNegativePrice() {
        Product product = new Product("Test Product", "Test Description", new BigDecimal("-10.00"), "Electronics");
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Price must be greater than or equal to 0")));
    }
    
    @Test
    void testProductWithZeroPrice() {
        Product product = new Product("Test Product", "Test Description", BigDecimal.ZERO, "Electronics");
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertTrue(violations.isEmpty());
    }
}