package com.hireflow.services;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hireflow.entities.Application;
import com.hireflow.entities.JobPosting;
import com.hireflow.entities.User;
import com.hireflow.repositories.ApplicationRepository;

@Service
public class ApplicationService {

    @Autowired
    ApplicationRepository appRepo;

    @Autowired
    EmailService emailService;

    public String applyForJob(User user, JobPosting job, String resumePath) {
        if (appRepo.existsByUserAndJob(user, job)) {
            return "ALREADY_APPLIED";
        }
        Application app = new Application();
        app.setUser(user);
        app.setJob(job);
        app.setResumePath(resumePath);
        app.setStatus("APPLIED");
        app.setAppliedDate(LocalDate.now());
        appRepo.save(app);

        emailService.sendMail(
            user.getEmail(),
            "Application Received — " + job.getTitle(),
            "Dear " + user.getName() + ",\n\n"
            + "Thank you for applying for the " + job.getTitle() + " position.\n\n"
            + "We have received your application and will review it shortly.\n\n"
            + "You can track your application status by logging into HireFlow.\n\n"
            + "Best Regards,\nHireFlow Recruitment Team"
        );
        return "SUCCESS";
    }

    public List<Application> getApplicationsByUser(User user) {
        return appRepo.findByUser(user);
    }

    public List<Application> getApplicationsByJob(JobPosting job) {
        return appRepo.findByJob(job);
    }

    public List<Application> getAllApplications() {
        return appRepo.findAll();
    }

    public Application getApplicationById(int id) {
        return appRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found: " + id));
    }

    public void updateStatus(int id, String status) {
        Application app = getApplicationById(id);
        app.setStatus(status);
        app.setLastUpdated(new Date()); // track when status changed for red dot logic
        appRepo.save(app);

        String subject = "";
        String body = "";
        String name = app.getUser().getName();
        String title = app.getJob().getTitle();

        switch (status) {
            case "UNDER_REVIEW":
                subject = "Your Application is Under Review — " + title;
                body = "Dear " + name + ",\n\nYour application for " + title
                     + " is currently under review. We will get back to you soon.\n\nBest Regards,\nHireFlow Recruitment Team";
                break;
            case "SHORTLISTED":
                subject = "Congratulations! You've Been Shortlisted — " + title;
                body = "Dear " + name + ",\n\nWe are pleased to inform you that you have been shortlisted for the "
                     + title + " position. Our team will contact you shortly with next steps.\n\nBest Regards,\nHireFlow Recruitment Team";
                break;
            case "SELECTED":
                subject = "Offer Confirmation — " + title;
                body = "Dear " + name + ",\n\nCongratulations! You have been selected for the "
                     + title + " position. Our team will reach out to you with the offer details.\n\nBest Regards,\nHireFlow Recruitment Team";
                break;
            case "REJECTED":
                subject = "Application Status Update — " + title;
                body = "Dear " + name + ",\n\nThank you for your interest in the " + title
                     + " position. After careful consideration, we regret to inform you that we will not be moving forward with your application at this time.\n\n"
                     + "We encourage you to apply for future openings.\n\nBest Regards,\nHireFlow Recruitment Team";
                break;
            default:
                return;
        }
        emailService.sendMail(app.getUser().getEmail(), subject, body);
    }

    public long countByStatus(String status) {
        return appRepo.countByStatus(status);
    }

    public long countAll() {
        return appRepo.count();
    }
}
