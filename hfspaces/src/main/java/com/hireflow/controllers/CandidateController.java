package com.hireflow.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CandidateController {

    @GetMapping("/viewCandidates")
    public String viewCandidates() {
        return "redirect:/recruiter/applications";
    }

    @GetMapping("/addCandidate")
    public String addCandidate() {
        return "redirect:/recruiter/jobs/new";
    }
}
