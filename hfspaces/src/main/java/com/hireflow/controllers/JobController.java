package com.hireflow.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import jakarta.servlet.http.HttpSession;

@Controller
public class JobController {

    @Autowired JobPostingService jobService;
    @Autowired ApplicationService appService;

    @Value("${app.upload.dir:uploads/resumes}")
    private String uploadDir;

    @GetMapping("/jobs")
    public String jobsPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";
        if ("RECRUITER".equals(user.getRole())) return "redirect:/recruiter/dashboard";

        // Check if user has any status updates (not APPLIED)
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

        // Check if user has any status updates
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

        // Save resume file
        String resumePath = null;
        if (resume != null && !resume.isEmpty()) {
            String originalName = resume.getOriginalFilename();
            String ext = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".pdf";
            String fileName = UUID.randomUUID() + "_" + user.getId() + ext;
            try {
                Path dirPath = Paths.get(uploadDir);
                Files.createDirectories(dirPath);
                Files.copy(resume.getInputStream(), dirPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                resumePath = fileName;
            } catch (IOException e) {
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
