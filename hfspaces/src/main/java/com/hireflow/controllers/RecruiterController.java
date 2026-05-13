package com.hireflow.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hireflow.entities.JobPosting;
import com.hireflow.entities.User;
import com.hireflow.services.ApplicationService;
import com.hireflow.services.JobPostingService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/recruiter")
public class RecruiterController {

    @Autowired JobPostingService jobService;
    @Autowired ApplicationService appService;

    private String guard(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";
        if (!"RECRUITER".equals(user.getRole())) return "access-denied";
        return null;
    }

    private User currentUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    // ── DASHBOARD ──

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        String g = guard(session); if (g != null) return g;

        model.addAttribute("totalJobs",       jobService.getAllJobs().size());
        model.addAttribute("activeJobs",      jobService.countActiveJobs());
        model.addAttribute("totalApps",       appService.countAll());
        model.addAttribute("appliedApps",     appService.countByStatus("APPLIED"));
        model.addAttribute("shortlistedApps", appService.countByStatus("SHORTLISTED"));
        model.addAttribute("selectedApps",    appService.countByStatus("SELECTED"));
        model.addAttribute("recentApps",      appService.getAllApplications().stream().limit(5).toList());
        model.addAttribute("user",            currentUser(session));
        return "recruiter/dashboard";
    }

    // ── JOB MANAGEMENT ──

    @GetMapping("/jobs")
    public String manageJobs(Model model, HttpSession session) {
        String g = guard(session); if (g != null) return g;
        model.addAttribute("jobs", jobService.getAllJobs());
        model.addAttribute("user", currentUser(session));
        return "recruiter/jobs";
    }

    @GetMapping("/jobs/new")
    public String newJobForm(Model model, HttpSession session) {
        String g = guard(session); if (g != null) return g;
        model.addAttribute("job", new JobPosting());
        model.addAttribute("user", currentUser(session));
        return "recruiter/job-form";
    }

    @PostMapping("/jobs/save")
    public String saveJob(JobPosting job, HttpSession session) {
        String g = guard(session); if (g != null) return g;
        jobService.saveJob(job);
        return "redirect:/recruiter/jobs?saved=true";
    }

    @GetMapping("/jobs/{id}/edit")
    public String editJobForm(@PathVariable int id, Model model, HttpSession session) {
        String g = guard(session); if (g != null) return g;
        model.addAttribute("job", jobService.getJobById(id));
        model.addAttribute("user", currentUser(session));
        return "recruiter/job-form";
    }

    @PostMapping("/jobs/{id}/update")
    public String updateJob(@PathVariable int id, JobPosting job, HttpSession session) {
        String g = guard(session); if (g != null) return g;
        job.setId(id);
        jobService.updateJob(job);
        return "redirect:/recruiter/jobs?updated=true";
    }

    // Fix #7: Changed from @GetMapping to @PostMapping — state-changing operations must not use GET
    @PostMapping("/jobs/{id}/close")
    public String closeJob(@PathVariable int id, HttpSession session) {
        String g = guard(session); if (g != null) return g;
        jobService.closeJob(id);
        return "redirect:/recruiter/jobs?closed=true";
    }

    // Fix #7: Changed from @GetMapping to @PostMapping — state-changing operations must not use GET
    @PostMapping("/jobs/{id}/delete")
    public String deleteJob(@PathVariable int id, HttpSession session) {
        String g = guard(session); if (g != null) return g;
        jobService.deleteJob(id);
        return "redirect:/recruiter/jobs?deleted=true";
    }

    // ── APPLICATION MANAGEMENT ──

    @GetMapping("/applications")
    public String allApplications(Model model, HttpSession session) {
        String g = guard(session); if (g != null) return g;
        model.addAttribute("applications", appService.getAllApplications());
        model.addAttribute("user", currentUser(session));
        return "recruiter/applications";
    }

    @GetMapping("/jobs/{id}/applications")
    public String jobApplications(@PathVariable int id, Model model, HttpSession session) {
        String g = guard(session); if (g != null) return g;
        JobPosting job = jobService.getJobById(id);
        model.addAttribute("job", job);
        model.addAttribute("applications", appService.getApplicationsByJob(job));
        model.addAttribute("user", currentUser(session));
        return "recruiter/applications";
    }

    @GetMapping("/applications/{id}/status")
    public String updateStatus(@PathVariable int id,
                               @RequestParam String status,
                               HttpSession session) {
        String g = guard(session); if (g != null) return g;
        appService.updateStatus(id, status);
        return "redirect:/recruiter/applications?updated=true";
    }
}
