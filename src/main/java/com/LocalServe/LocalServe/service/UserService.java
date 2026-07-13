package com.LocalServe.LocalServe.service;

import com.LocalServe.LocalServe.entity.User;
import com.LocalServe.LocalServe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Register a new user
    public User registerUser(User user) {
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }
        return userRepository.save(user);
    }

    // Login user
    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public long getAllUsersCount() {
        return userRepository.count();
    }

    public User updateUserProfile(Long id, String fullName, String phoneNumber, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        
        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(password);
        }
        
        return userRepository.save(user);
    }
}