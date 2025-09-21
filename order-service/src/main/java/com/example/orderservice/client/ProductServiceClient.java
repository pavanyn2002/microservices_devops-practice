package com.example.orderservice.client;

import com.example.orderservice.dto.Product;
import com.example.orderservice.exception.ServiceCommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class ProductServiceClient {
    
    private static final Logger log = LoggerFactory.getLogger(ProductServiceClient.class);
    
    private final RestTemplate restTemplate;
    private final String productServiceUrl;
    
    @Autowired
    public ProductServiceClient(RestTemplate restTemplate, 
                              @Value("${services.product-service}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }
    
    public Optional<Product> getProductById(Long productId) {
        try {
            String url = productServiceUrl + "/api/products/" + productId;
            log.debug("Calling Product Service: {}", url);
            
            ResponseEntity<Product> response = restTemplate.getForEntity(url, Product.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Successfully retrieved product with ID: {}", productId);
                return Optional.of(response.getBody());
            } else {
                log.warn("Product not found with ID: {}", productId);
                return Optional.empty();
            }
        } catch (Exception ex) {
            log.error("Failed to fetch product with ID: {}", productId, ex);
            throw new ServiceCommunicationException("Failed to communicate with Product Service", ex);
        }
    }
    
    public boolean productExists(Long productId) {
        try {
            return getProductById(productId).isPresent();
        } catch (ServiceCommunicationException ex) {
            log.error("Error checking if product exists with ID: {}", productId, ex);
            return false;
        }
    }
}