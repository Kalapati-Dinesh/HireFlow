package com.hireflow.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hireflow.entities.JobPosting;
import com.hireflow.repositories.JobPostingRepository;

@Service
public class JobPostingService {

    @Autowired
    JobPostingRepository jobRepo;

    public void saveJob(JobPosting job) {
        job.setPostedDate(LocalDate.now());
        job.setActive(true);
        jobRepo.save(job);
    }

    // Fix #11: Preserve postedDate from the existing record so it isn't wiped on update
    public void updateJob(JobPosting job) {
        JobPosting existing = getJobById(job.getId());
        job.setPostedDate(existing.getPostedDate());
        jobRepo.save(job);
    }

    public JobPosting getJobById(int id) {
        return jobRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
    }

    public List<JobPosting> getAllJobs() {
        return jobRepo.findAll();
    }

    public List<JobPosting> getActiveJobs() {
        return jobRepo.findByActiveTrue();
    }

    public List<JobPosting> getRecentlyExpiredJobs() {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        return jobRepo.findByActiveFalseAndDeadlineBetween(weekAgo, today);
    }

    public void closeJob(int id) {
        JobPosting job = getJobById(id);
        job.setActive(false);
        jobRepo.save(job);
    }

    public void deleteJob(int id) {
        jobRepo.deleteById(id);
    }

    // Fix #12: Use countByActiveTrue() instead of loading all records into memory
    public long countActiveJobs() {
        return jobRepo.countByActiveTrue();
    }
}
