package com.example.userservice.repository;

import com.example.userservice.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testFindByUsername() {
        // Given
        User user = new User("testuser", "test@example.com", "John", "Doe");
        entityManager.persistAndFlush(user);
        
        // When
        Optional<User> found = userRepository.findByUsername("testuser");
        
        // Then
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
    }
    
    @Test
    void testFindByUsernameNotFound() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexistent");
        
        // Then
        assertFalse(found.isPresent());
    }
    
    @Test
    void testFindByEmail() {
        // Given
        User user = new User("testuser", "test@example.com", "John", "Doe");
        entityManager.persistAndFlush(user);
        
        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");
        
        // Then
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
    }
    
    @Test
    void testExistsByUsername() {
        // Given
        User user = new User("testuser", "test@example.com", "John", "Doe");
        entityManager.persistAndFlush(user);
        
        // When & Then
        assertTrue(userRepository.existsByUsername("testuser"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }
    
    @Test
    void testExistsByEmail() {
        // Given
        User user = new User("testuser", "test@example.com", "John", "Doe");
        entityManager.persistAndFlush(user);
        
        // When & Then
        assertTrue(userRepository.existsByEmail("test@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }
}