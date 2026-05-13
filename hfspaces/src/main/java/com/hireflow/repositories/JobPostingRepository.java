package com.hireflow.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hireflow.entities.JobPosting;

public interface JobPostingRepository extends JpaRepository<JobPosting, Integer> {

    List<JobPosting> findByActiveTrue();

    long countByActiveTrue();

    List<JobPosting> findByActiveFalseAndDeadlineAfter(LocalDate date);

    List<JobPosting> findByActiveFalseAndDeadlineBetween(LocalDate from, LocalDate to);
}
