package com.hireflow.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hireflow.entities.Candidate;
import com.hireflow.repositories.CandidateRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class CandidateService {
	
	@Autowired
	EmailService emailService;

    @Autowired
    CandidateRepository candidateRepo;

    public void saveCandidate(Candidate candidate) {

        candidate.setAppliedDate(LocalDate.now());

        candidate.setStatus("APPLIED");

        candidateRepo.save(candidate);
    }

    public List<Candidate> getAllCandidates() {
        return candidateRepo.findAll();
    }
    
    public Candidate getCandidateById(int id) {

        return candidateRepo.findById(id)
                .orElseThrow(() -> new RuntimeException(
                    "Candidate not found with id: " + id));
    }

    public void updateStatus(int id, String status) {

        Candidate candidate = getCandidateById(id);

        candidate.setStatus(status);

        candidateRepo.save(candidate);

        String subject = "";
        String body = "";

        if(status.equals("REJECTED")) {

            subject = candidate.getCompanyName() 
                    + " | Application Status Update";

            body = "Dear " + candidate.getStudentName()
                    + ",\n\n"
                    
                    + "Thank you for your interest in the "
                    + candidate.getPosition()
                    + " role at "
                    + candidate.getCompanyName()
                    + ".\n\n"
                    
                    + "We appreciate the time and effort you invested "
                    + "throughout the recruitment process.\n\n"
                    
                    + "After careful consideration, we regret to inform "
                    + "you that you have not been selected for the "
                    + candidate.getPosition()
                    + " position at this time.\n\n"
                    
                    + "We encourage you to apply for future opportunities "
                    + "that match your skills and experience.\n\n"
                    
                    + "We wish you all the best in your career journey.\n\n"
                    
                    + "Best Regards,\n"
                    + candidate.getCompanyName()
                    + " Recruitment Team";

        }
        else if(status.equals("SELECTED")) {

            subject = candidate.getCompanyName()
                    + " | Offer Confirmation";

            body = "Dear " + candidate.getStudentName()
                    + ",\n\n"
                    
                    + "Congratulations!\n\n"
                    
                    + "We are pleased to inform you that you have been "
                    + "selected for the "
                    + candidate.getPosition()
                    + " role at "
                    + candidate.getCompanyName()
                    + ".\n\n"
                    
                    + "Our team will contact you shortly regarding "
                    + "the next steps in the hiring process.\n\n"
                    
                    + "We look forward to having you as part of our team.\n\n"
                    
                    + "Best Regards,\n"
                    + candidate.getCompanyName()
                    + " Recruitment Team";
        }

        if(!status.equals("IN_PROGRESS")) {

            emailService.sendMail(
                    candidate.getStudentEmail(),
                    subject,
                    body
            );
        }
    }
    
    public long totalCandidates() {
        return candidateRepo.count();
    }

    public long selectedCandidates() {
        return candidateRepo.countByStatus("SELECTED");
    }

    public long rejectedCandidates() {
        return candidateRepo.countByStatus("REJECTED");
    }

    public long appliedCandidates() {
        return candidateRepo.countByStatus("APPLIED");
    }
    
    public List<Candidate> searchByEmail(String email) {

        return candidateRepo
                .findByStudentEmailContaining(email);
    }

    public List<Candidate> filterByStatus(String status) {

        return candidateRepo.findByStatus(status);
    }
    
    public Page<Candidate> getCandidatesByPage(int page) {

        Pageable pageable =
                PageRequest.of(page, 5);

        return candidateRepo.findAll(pageable);
    }
    
    public List<Candidate> getCandidatesByStudentEmail(
            String email) {

        return candidateRepo
                .findByStudentEmail(email);
    }
    public Candidate getCandidate(int id) {

        return candidateRepo.findById(id).orElse(null);
    }
    public void deleteCandidate(int id) {

        candidateRepo.deleteById(id);
    }
}