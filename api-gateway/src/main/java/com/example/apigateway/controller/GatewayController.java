package com.example.apigateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@RestController
@RequestMapping("/api")
public class GatewayController {
    
    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);
    
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
    public GatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @RequestMapping(value = "/users/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<Object> routeToUserService(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return forwardRequest(request, body, userServiceUrl, "User Service");
    }
    
    @RequestMapping(value = "/products/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<Object> routeToProductService(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return forwardRequest(request, body, productServiceUrl, "Product Service");
    }
    
    @RequestMapping(value = "/orders/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<Object> routeToOrderService(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return forwardRequest(request, body, orderServiceUrl, "Order Service");
    }
    
    @RequestMapping(value = "/inventory/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<Object> routeToInventoryService(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return forwardRequest(request, body, inventoryServiceUrl, "Inventory Service");
    }
    
    private ResponseEntity<Object> forwardRequest(HttpServletRequest request, Object body, String serviceUrl, String serviceName) {
        try {
            String path = request.getRequestURI();
            String queryString = request.getQueryString();
            String fullPath = path + (queryString != null ? "?" + queryString : "");
            
            String targetUrl = serviceUrl + fullPath;
            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            
            log.debug("Forwarding {} request to {}: {}", method, serviceName, targetUrl);
            
            // Copy headers from original request
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headers.add(headerName, headerValue);
            }
            
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<Object> response = restTemplate.exchange(targetUrl, method, entity, Object.class);
            
            log.debug("Successfully forwarded request to {}, status: {}", serviceName, response.getStatusCode());
            return response;
            
        } catch (Exception ex) {
            log.error("Error forwarding request to {}: {}", serviceName, ex.getMessage());
            return ResponseEntity.status(503).body("Service temporarily unavailable: " + serviceName);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("API Gateway is healthy");
    }
    
    @GetMapping("/")
    public ResponseEntity<String> root() {
        return ResponseEntity.ok("Spring Boot Microservices API Gateway - Available endpoints: /api/users, /api/products, /api/orders, /api/inventory");
    }
}