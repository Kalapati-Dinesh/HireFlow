package com.hireflow.services;

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

        if(existingUser != null) {
            return "Email already exists";
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());

        user.setPassword(encodedPassword);

        userRepo.save(user);

        return "User Registered Successfully";
    }
    
    public User loginUser(String email) {
        return userRepo.findByEmail(email);
    }

    public boolean checkPassword(String rawPassword,
                                 String encodedPassword) {

        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}