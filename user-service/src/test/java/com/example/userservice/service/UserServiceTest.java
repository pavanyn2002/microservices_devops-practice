package com.example.userservice.service;

import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.exception.ValidationException;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "John", "Doe");
        testUser.setId(1L);
    }
    
    @Test
    void testGetAllUsers() {
        // Given
        List<User> users = Arrays.asList(testUser, new User("user2", "user2@example.com", "Jane", "Smith"));
        when(userRepository.findAll()).thenReturn(users);
        
        // When
        List<User> result = userService.getAllUsers();
        
        // Then
        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }
    
    @Test
    void testGetUserById() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // When
        User result = userService.getUserById(1L);
        
        // Then
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).findById(1L);
    }
    
    @Test
    void testGetUserByIdNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
        verify(userRepository).findById(1L);
    }
    
    @Test
    void testCreateUser() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        User result = userService.createUser(testUser);
        
        // Then
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(testUser);
    }
    
    @Test
    void testCreateUserWithExistingUsername() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // When & Then
        assertThrows(ValidationException.class, () -> userService.createUser(testUser));
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void testCreateUserWithExistingEmail() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        // When & Then
        assertThrows(ValidationException.class, () -> userService.createUser(testUser));
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void testUpdateUser() {
        // Given
        User updatedUser = new User("updateduser", "updated@example.com", "John", "Updated");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        User result = userService.updateUser(1L, updatedUser);
        
        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }
    
    @Test
    void testDeleteUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // When
        userService.deleteUser(1L);
        
        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }
    
    @Test
    void testFindByUsername() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // When
        Optional<User> result = userService.findByUsername("testuser");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findByUsername("testuser");
    }
    
    @Test
    void testFindByEmail() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        // When
        Optional<User> result = userService.findByEmail("test@example.com");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }
}