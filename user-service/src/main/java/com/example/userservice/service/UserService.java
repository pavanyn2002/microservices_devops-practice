package com.example.userservice.service;

import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.exception.ValidationException;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
    
    public User createUser(User user) {
        validateUserForCreation(user);
        return userRepository.save(user);
    }
    
    public User updateUser(Long id, User userDetails) {
        User existingUser = getUserById(id);
        
        validateUserForUpdate(userDetails, existingUser);
        
        existingUser.setUsername(userDetails.getUsername());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        
        return userRepository.save(existingUser);
    }
    
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    private void validateUserForCreation(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ValidationException("Username already exists: " + user.getUsername());
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ValidationException("Email already exists: " + user.getEmail());
        }
    }
    
    private void validateUserForUpdate(User userDetails, User existingUser) {
        // Check if username is being changed and if it already exists
        if (!existingUser.getUsername().equals(userDetails.getUsername()) &&
            userRepository.existsByUsername(userDetails.getUsername())) {
            throw new ValidationException("Username already exists: " + userDetails.getUsername());
        }
        
        // Check if email is being changed and if it already exists
        if (!existingUser.getEmail().equals(userDetails.getEmail()) &&
            userRepository.existsByEmail(userDetails.getEmail())) {
            throw new ValidationException("Email already exists: " + userDetails.getEmail());
        }
    }
}