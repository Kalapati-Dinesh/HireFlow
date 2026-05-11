package com.hireflow.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hireflow.entities.Candidate;

public interface CandidateRepository
        extends JpaRepository<Candidate, Integer> {

    long countByStatus(String status);

    long count();

    List<Candidate> findByStudentEmailContaining(String email);

    List<Candidate> findByStatus(String status);
    
    List<Candidate> findByStudentEmail(String email);
}