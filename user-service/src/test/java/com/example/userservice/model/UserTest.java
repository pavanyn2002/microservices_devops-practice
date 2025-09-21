package com.example.userservice.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidUser() {
        User user = new User("testuser", "test@example.com", "John", "Doe");
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        assertTrue(violations.isEmpty());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }
    
    @Test
    void testUserWithBlankUsername() {
        User user = new User("", "test@example.com", "John", "Doe");
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Username is required")));
    }
    
    @Test
    void testUserWithInvalidEmail() {
        User user = new User("testuser", "invalid-email", "John", "Doe");
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Email should be valid")));
    }
    
    @Test
    void testUserWithBlankFirstName() {
        User user = new User("testuser", "test@example.com", "", "Doe");
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("First name is required")));
    }
    
    @Test
    void testUserWithBlankLastName() {
        User user = new User("testuser", "test@example.com", "John", "");
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Last name is required")));
    }
}