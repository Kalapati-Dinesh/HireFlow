package com.hireflow.services;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${resend.api.key:}")
    private String apiKey;

    @Value("${resend.sender.email:onboarding@resend.dev}")
    private String senderEmail;

    private final RestClient restClient = RestClient.create();

    @Async
    public void sendMail(String to, String subject, String body) {
        try {
            Map<String, Object> payload = Map.of(
                "from", "HireFlow <" + senderEmail + ">",
                "to", List.of(to),
                "subject", subject,
                "text", body
            );

            restClient.post()
                .uri("https://api.resend.com/emails")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();

        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }
}
