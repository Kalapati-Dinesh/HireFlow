package com.hireflow.controllers;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.hireflow.entities.JobPosting;
import com.hireflow.entities.User;
import com.hireflow.services.ApplicationService;
import com.hireflow.services.JobPostingService;
import com.hireflow.services.S3Service;

import jakarta.servlet.http.HttpSession;

@Controller
public class JobController {

    @Autowired JobPostingService jobService;
    @Autowired ApplicationService appService;
    @Autowired S3Service s3Service;

    @GetMapping("/jobs")
    public String jobsPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";
        if ("RECRUITER".equals(user.getRole())) return "redirect:/recruiter/dashboard";

        boolean hasUpdates = appService.getApplicationsByUser(user).stream()
                .anyMatch(a -> !"APPLIED".equals(a.getStatus()));

        model.addAttribute("activeJobs", jobService.getActiveJobs());
        model.addAttribute("expiredJobs", jobService.getRecentlyExpiredJobs());
        model.addAttribute("user", user);
        model.addAttribute("hasUpdates", hasUpdates);
        return "user/jobs";
    }

    @GetMapping("/jobs/{id}")
    public String jobDetail(@PathVariable int id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";
        if ("RECRUITER".equals(user.getRole())) return "redirect:/recruiter/dashboard";

        JobPosting job = jobService.getJobById(id);
        boolean alreadyApplied = appService.getAllApplications().stream()
                .anyMatch(a -> Integer.valueOf(a.getUser().getId()).equals(user.getId())
                            && a.getJob().getId().equals(job.getId()));

        boolean hasUpdates = appService.getApplicationsByUser(user).stream()
                .anyMatch(a -> !"APPLIED".equals(a.getStatus()));

        model.addAttribute("job", job);
        model.addAttribute("alreadyApplied", alreadyApplied);
        model.addAttribute("user", user);
        model.addAttribute("hasUpdates", hasUpdates);
        return "user/job-detail";
    }

    @PostMapping("/jobs/{id}/apply")
    public String applyForJob(@PathVariable int id,
                              @RequestParam(required = false) MultipartFile resume,
                              HttpSession session,
                              Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        JobPosting job = jobService.getJobById(id);
        if (!job.isActive()) {
            model.addAttribute("error", "This job posting is no longer accepting applications.");
            model.addAttribute("job", job);
            model.addAttribute("alreadyApplied", false);
            return "user/job-detail";
        }

        String resumePath = null;
        if (resume != null && !resume.isEmpty()) {
            String originalName = resume.getOriginalFilename();
            String ext = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase()
                    : ".pdf";

            if (!ext.matches("\\.(pdf|doc|docx)")) {
                model.addAttribute("error", "Only PDF, DOC, and DOCX files are allowed.");
                model.addAttribute("job", job);
                model.addAttribute("alreadyApplied", false);
                return "user/job-detail";
            }

            String s3Key = "resumes/" + UUID.randomUUID() + "_" + user.getId() + ext;
            try {
                s3Service.uploadFile(s3Key, resume.getInputStream(), resume.getSize(), resume.getContentType());
                resumePath = s3Key;
            } catch (IOException | RuntimeException e) {
                model.addAttribute("error", "Failed to upload resume. Please try again.");
                model.addAttribute("job", job);
                model.addAttribute("alreadyApplied", false);
                return "user/job-detail";
            }
        }

        String result = appService.applyForJob(user, job, resumePath);
        if ("ALREADY_APPLIED".equals(result)) {
            return "redirect:/jobs/" + id + "?alreadyApplied=true";
        }
        return "redirect:/my-applications?applied=true";
    }

    @GetMapping("/my-applications")
    public String myApplications(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";
        if ("RECRUITER".equals(user.getRole())) return "redirect:/recruiter/dashboard";

        model.addAttribute("applications", appService.getApplicationsByUser(user));
        model.addAttribute("user", user);
        return "user/my-applications";
    }
}
