package com.example.apigateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {
    
    private final RestTemplate restTemplate;
    
    @Value("${services.user-service}")
    private String userServiceUrl;
    
    @Value("${services.product-service}")
    private String productServiceUrl;
    
    @Value("${services.order-service}")
    private String orderServiceUrl;
    
    @Value("${services.inventory-service}")
    private String inventoryServiceUrl;
    
    @Autowired
    public HealthController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("gateway", "UP");
        health.put("services", checkServicesHealth());
        
        return ResponseEntity.ok(health);
    }
    
    private Map<String, String> checkServicesHealth() {
        Map<String, String> servicesHealth = new HashMap<>();
        
        servicesHealth.put("user-service", checkServiceHealth(userServiceUrl + "/api/users/health"));
        servicesHealth.put("product-service", checkServiceHealth(productServiceUrl + "/api/products/health"));
        servicesHealth.put("order-service", checkServiceHealth(orderServiceUrl + "/api/orders/health"));
        servicesHealth.put("inventory-service", checkServiceHealth(inventoryServiceUrl + "/api/inventory/health"));
        
        return servicesHealth;
    }
    
    private String checkServiceHealth(String healthUrl) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            return response.getStatusCode().is2xxSuccessful() ? "UP" : "DOWN";
        } catch (Exception ex) {
            return "DOWN";
        }
    }
}