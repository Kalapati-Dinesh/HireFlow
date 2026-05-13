package com.hireflow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hireflow.entities.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	User findByEmail(String email);
	User findByResetToken(String resetToken);
}