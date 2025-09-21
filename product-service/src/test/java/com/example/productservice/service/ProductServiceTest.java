package com.example.productservice.service;

import com.example.productservice.exception.ResourceNotFoundException;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductService productService;
    
    private Product testProduct;
    
    @BeforeEach
    void setUp() {
        testProduct = new Product("Test Product", "Test Description", new BigDecimal("29.99"), "Electronics");
        testProduct.setId(1L);
    }
    
    @Test
    void testGetAllProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct, 
            new Product("Product 2", "Description 2", new BigDecimal("39.99"), "Books"));
        when(productRepository.findAll()).thenReturn(products);
        
        // When
        List<Product> result = productService.getAllProducts();
        
        // Then
        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }
    
    @Test
    void testGetProductById() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        
        // When
        Product result = productService.getProductById(1L);
        
        // Then
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testProduct.getPrice(), result.getPrice());
        verify(productRepository).findById(1L);
    }
    
    @Test
    void testGetProductByIdNotFound() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(1L));
        verify(productRepository).findById(1L);
    }
    
    @Test
    void testCreateProduct() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        // When
        Product result = productService.createProduct(testProduct);
        
        // Then
        assertEquals(testProduct.getName(), result.getName());
        verify(productRepository).save(testProduct);
    }
    
    @Test
    void testUpdateProduct() {
        // Given
        Product updatedProduct = new Product("Updated Product", "Updated Description", 
            new BigDecimal("49.99"), "Books");
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        // When
        Product result = productService.updateProduct(1L, updatedProduct);
        
        // Then
        verify(productRepository).findById(1L);
        verify(productRepository).save(testProduct);
    }
    
    @Test
    void testDeleteProduct() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        
        // When
        productService.deleteProduct(1L);
        
        // Then
        verify(productRepository).findById(1L);
        verify(productRepository).delete(testProduct);
    }
    
    @Test
    void testGetProductsByCategory() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByCategoryIgnoreCase("Electronics")).thenReturn(products);
        
        // When
        List<Product> result = productService.getProductsByCategory("Electronics");
        
        // Then
        assertEquals(1, result.size());
        verify(productRepository).findByCategoryIgnoreCase("Electronics");
    }
}