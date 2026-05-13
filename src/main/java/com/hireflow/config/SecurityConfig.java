package com.hireflow.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hireflow.jwt.JwtFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    JwtFilter jwtFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // CSRF disabled: stateless JWT-based API; CSRF attacks do not apply.
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // Public routes
                .requestMatchers(
                    "/", "/login", "/signup", "/register",
                    "/verify-recruiter-code", "/logout",
                    "/forgot-password", "/reset-password",
                    "/css/**", "/js/**", "/fonts/**", "/favicon.ico",
                    "/swagger-ui/**", "/v3/api-docs/**",
                    "/api/login", "/api/refresh"
                ).permitAll()
                // MVC/Thymeleaf routes — auth handled inside controllers via HttpSession
                .requestMatchers(
                    "/recruiter/**", "/jobs/**", "/my-applications",
                    "/viewCandidates", "/addCandidate", "/resumes/**"
                ).permitAll()
                // REST API routes — secured via JWT filter
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
