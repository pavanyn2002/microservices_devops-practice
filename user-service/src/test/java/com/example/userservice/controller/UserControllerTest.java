package com.example.userservice.controller;

import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.exception.ValidationException;
import com.example.userservice.model.User;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "John", "Doe");
        testUser.setId(1L);
    }
    
    @Test
    void testGetAllUsers() throws Exception {
        // Given
        List<User> users = Arrays.asList(testUser, new User("user2", "user2@example.com", "Jane", "Smith"));
        when(userService.getAllUsers()).thenReturn(users);
        
        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));
        
        verify(userService).getAllUsers();
    }
    
    @Test
    void testGetUserById() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(testUser);
        
        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
        
        verify(userService).getUserById(1L);
    }
    
    @Test
    void testGetUserByIdNotFound() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenThrow(new ResourceNotFoundException("User not found with id: 1"));
        
        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found with id: 1"));
        
        verify(userService).getUserById(1L);
    }
    
    @Test
    void testCreateUser() throws Exception {
        // Given
        when(userService.createUser(any(User.class))).thenReturn(testUser);
        
        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
        
        verify(userService).createUser(any(User.class));
    }
    
    @Test
    void testCreateUserWithValidationError() throws Exception {
        // Given
        User invalidUser = new User("", "invalid-email", "", "");
        
        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testCreateUserWithDuplicateUsername() throws Exception {
        // Given
        when(userService.createUser(any(User.class)))
                .thenThrow(new ValidationException("Username already exists: testuser"));
        
        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Username already exists: testuser"));
        
        verify(userService).createUser(any(User.class));
    }
    
    @Test
    void testUpdateUser() throws Exception {
        // Given
        User updatedUser = new User("updateduser", "updated@example.com", "John", "Updated");
        updatedUser.setId(1L);
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updatedUser);
        
        // When & Then
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
        
        verify(userService).updateUser(eq(1L), any(User.class));
    }
    
    @Test
    void testDeleteUser() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);
        
        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
        
        verify(userService).deleteUser(1L);
    }
    
    @Test
    void testHealthCheck() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("User Service is healthy"));
    }
}