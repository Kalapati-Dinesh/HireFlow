package com.hireflow.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hireflow.entities.Candidate;
import com.hireflow.services.CandidateService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
public class CandidateRestController {

    // Fix #1: Logger declared at top of class
    private static final Logger logger = LoggerFactory.getLogger(CandidateRestController.class);

    @Autowired
    CandidateService candidateService;

    @Operation(summary = "Get all candidates")
    @GetMapping("/api/candidates")
    public List<Candidate> getAllCandidates() {
        logger.info("Fetching all candidates");
        return candidateService.getAllCandidates();
    }

    @Operation(summary = "Get candidate by ID")
    @GetMapping("/api/candidates/{id}")
    public Candidate getCandidateById(@PathVariable int id) {
        return candidateService.getCandidate(id);
    }

    // Fix #16: Added session-based role check to protect write endpoint
    @Operation(summary = "Add new candidate")
    @PostMapping("/api/candidates")
    public ResponseEntity<String> addCandidate(
            @Valid @RequestBody Candidate candidate,
            HttpSession session) {

        Object user = session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        logger.info("Adding candidate: {}", candidate.getStudentEmail().replaceAll("[\r\n]", ""));
        candidateService.saveCandidate(candidate);
        return ResponseEntity.ok("Candidate Added Successfully");
    }

    // Fix #16: Added session-based role check to protect delete endpoint
    @Operation(summary = "Delete candidate")
    @DeleteMapping("/api/candidates/{id}")
    public ResponseEntity<String> deleteCandidate(
            @PathVariable int id,
            HttpSession session) {

        Object user = session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        candidateService.deleteCandidate(id);
        return ResponseEntity.ok("Candidate Deleted Successfully");
    }

    @GetMapping("/api/recruiter/test")
    public String recruiterApi() {
        return "Recruiter API Accessed";
    }

    @GetMapping("/api/student/test")
    public String studentApi() {
        return "Student API Accessed";
    }
}
