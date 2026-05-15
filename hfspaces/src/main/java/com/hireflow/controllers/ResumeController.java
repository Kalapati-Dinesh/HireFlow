package com.hireflow.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hireflow.entities.User;
import com.hireflow.services.S3Service;

import jakarta.servlet.http.HttpSession;

@Controller
public class ResumeController {

    @Autowired
    S3Service s3Service;

    // filename here is the full S3 key encoded as a path param (e.g. resumes/uuid_id.pdf)
    // Recruiter clicks a link like /resumes/resumes%2Fuuid_id.pdf and gets redirected to S3
    @GetMapping("/resumes/{filename:.+}")
    public ResponseEntity<Void> downloadResume(@PathVariable String filename,
                                               HttpSession session) {
        if (session == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!"RECRUITER".equals(user.getRole())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (filename == null || filename.isBlank()) return ResponseEntity.badRequest().build();

        // S3 key is stored as "resumes/filename" in DB
        String s3Key = filename.startsWith("resumes/") ? filename : "resumes/" + filename;

        String presignedUrl = s3Service.generatePresignedUrl(s3Key);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", presignedUrl)
                .build();
    }
}
