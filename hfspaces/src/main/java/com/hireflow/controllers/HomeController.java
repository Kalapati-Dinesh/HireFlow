package com.hireflow.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.hireflow.entities.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null) {
            if ("RECRUITER".equals(user.getRole())) return "redirect:/recruiter/dashboard";
            return "redirect:/jobs";
        }
        return "index";
    }
}
