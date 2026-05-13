package com.hireflow.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.hireflow.entities.User;
import com.hireflow.repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    UserRepository userRepo;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    public String saveUser(User user) {
        User existingUser = userRepo.findByEmail(user.getEmail());
        if (existingUser != null) return "Email already exists";
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        return "User Registered Successfully";
    }

    public User loginUser(String email) {
        return userRepo.findByEmail(email);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public String generateResetToken(String email) {
        User user = userRepo.findByEmail(email);
        if (user == null) return null;
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepo.save(user);
        return token;
    }

    public User findByResetToken(String token) {
        User user = userRepo.findByResetToken(token);
        if (user == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) return null;
        return user;
    }

    public boolean resetPassword(String token, String newPassword) {
        User user = findByResetToken(token);
        if (user == null) return false;
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepo.save(user);
        return true;
    }
}