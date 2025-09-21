package com.example.orderservice.service;

import com.example.orderservice.client.ProductServiceClient;
import com.example.orderservice.client.UserServiceClient;
import com.example.orderservice.dto.Product;
import com.example.orderservice.exception.ResourceNotFoundException;
import com.example.orderservice.exception.ValidationException;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    
    @Autowired
    public OrderService(OrderRepository orderRepository,
                       UserServiceClient userServiceClient,
                       ProductServiceClient productServiceClient) {
        this.orderRepository = orderRepository;
        this.userServiceClient = userServiceClient;
        this.productServiceClient = productServiceClient;
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }
    
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    public Order createOrder(Order order) {
        log.info("Creating order for user ID: {}", order.getUserId());
        
        // Validate user exists
        validateUser(order.getUserId());
        
        // Validate and enrich order items
        validateAndEnrichOrderItems(order);
        
        // Calculate total amount
        order.calculateTotalAmount();
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());
        
        return savedOrder;
    }
    
    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }
    
    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        orderRepository.delete(order);
    }
    
    private void validateUser(Long userId) {
        log.debug("Validating user with ID: {}", userId);
        
        if (!userServiceClient.userExists(userId)) {
            throw new ValidationException("User not found with id: " + userId);
        }
        
        log.debug("User validation successful for ID: {}", userId);
    }
    
    private void validateAndEnrichOrderItems(Order order) {
        log.debug("Validating and enriching {} order items", order.getOrderItems().size());
        
        for (OrderItem item : order.getOrderItems()) {
            Optional<Product> productOpt = productServiceClient.getProductById(item.getProductId());
            
            if (productOpt.isEmpty()) {
                throw new ValidationException("Product not found with id: " + item.getProductId());
            }
            
            Product product = productOpt.get();
            
            // Set the current price from the product service
            item.setUnitPrice(product.getPrice());
            
            log.debug("Validated product ID: {} with price: {}", item.getProductId(), item.getUnitPrice());
        }
        
        log.debug("Order items validation and enrichment completed");
    }
}