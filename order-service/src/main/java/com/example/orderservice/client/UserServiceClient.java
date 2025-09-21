package com.example.orderservice.client;

import com.example.orderservice.dto.User;
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
public class UserServiceClient {
    
    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);
    
    private final RestTemplate restTemplate;
    private final String userServiceUrl;
    
    @Autowired
    public UserServiceClient(RestTemplate restTemplate, 
                           @Value("${services.user-service}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }
    
    public Optional<User> getUserById(Long userId) {
        try {
            String url = userServiceUrl + "/api/users/" + userId;
            log.debug("Calling User Service: {}", url);
            
            ResponseEntity<User> response = restTemplate.getForEntity(url, User.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Successfully retrieved user with ID: {}", userId);
                return Optional.of(response.getBody());
            } else {
                log.warn("User not found with ID: {}", userId);
                return Optional.empty();
            }
        } catch (Exception ex) {
            log.error("Failed to fetch user with ID: {}", userId, ex);
            throw new ServiceCommunicationException("Failed to communicate with User Service", ex);
        }
    }
    
    public boolean userExists(Long userId) {
        try {
            return getUserById(userId).isPresent();
        } catch (ServiceCommunicationException ex) {
            log.error("Error checking if user exists with ID: {}", userId, ex);
            return false;
        }
    }
}