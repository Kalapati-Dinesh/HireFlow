package com.hireflow.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hireflow.dto.AuthResponse;
import com.hireflow.dto.LoginRequest;
import com.hireflow.dto.RefreshTokenRequest;
import com.hireflow.entities.User;
import com.hireflow.jwt.JwtUtil;
import com.hireflow.services.UserService;

import io.jsonwebtoken.JwtException;

@RestController
public class AuthRestController {

    private static final Logger logger = LoggerFactory.getLogger(AuthRestController.class);

    @Autowired
    UserService userService;

    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User dbUser = userService.loginUser(request.getEmail());

        if (dbUser != null && userService.checkPassword(request.getPassword(), dbUser.getPassword())) {
            String accessToken = jwtUtil.generateToken(dbUser.getEmail(), dbUser.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(dbUser.getEmail());
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
    }

    @PostMapping("/api/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String email = jwtUtil.extractEmail(request.getRefreshToken());
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
            }

            User user = userService.loginUser(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }

            String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole());
            return ResponseEntity.ok(new AuthResponse(newAccessToken, request.getRefreshToken()));

        } catch (JwtException e) {
            logger.warn("Refresh token invalid: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }
    }
}
