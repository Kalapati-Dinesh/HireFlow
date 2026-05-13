package com.hireflow.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hireflow.entities.Application;
import com.hireflow.entities.JobPosting;
import com.hireflow.entities.User;

public interface ApplicationRepository extends JpaRepository<Application, Integer> {

    List<Application> findByUser(User user);

    List<Application> findByJob(JobPosting job);

    Optional<Application> findByUserAndJob(User user, JobPosting job);

    boolean existsByUserAndJob(User user, JobPosting job);

    long countByStatus(String status);
}
