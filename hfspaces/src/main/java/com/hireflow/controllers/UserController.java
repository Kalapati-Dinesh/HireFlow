package com.hireflow.controllers;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hireflow.entities.User;
import com.hireflow.services.EmailService;
import com.hireflow.services.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "RECRUITER");

    @Autowired
    UserService userService;

    @Autowired
    EmailService emailService;

    @Value("${recruiter.access.code}")
    private String recruiterAccessCode;

    // ── SIGNUP ──

    @GetMapping("/signup")
    public String signupPage(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) return "redirect:/";
        return "signup";
    }

    @PostMapping("/verify-recruiter-code")
    public String verifyRecruiterCode(@RequestParam String accessCode,
                                      HttpSession session,
                                      Model model) {
        if (recruiterAccessCode.equals(accessCode.trim())) {
            session.setAttribute("recruiterVerified", true);
            return "redirect:/signup?role=RECRUITER&verified=true";
        }
        model.addAttribute("codeError", "Invalid access code. Please contact your HR administrator.");
        return "signup";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String role,
                               HttpSession session,
                               Model model) {

        if (!ALLOWED_ROLES.contains(role)) {
            model.addAttribute("registerError", "Invalid role selected.");
            return "signup";
        }

        if ("RECRUITER".equals(role)) {
            Boolean verified = (Boolean) session.getAttribute("recruiterVerified");
            if (verified == null || !verified) {
                model.addAttribute("codeError", "Recruiter access not verified. Please enter the company access code.");
                return "signup";
            }
            session.removeAttribute("recruiterVerified");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);

        String message = userService.saveUser(user);

        if ("Email already exists".equals(message)) {
            model.addAttribute("registerError", "An account with this email already exists.");
            return "signup";
        }

        emailService.sendMail(
            email,
            "Welcome to HireFlow!",
            "Dear " + name + ",\n\n"
            + "Welcome to HireFlow! Your account has been created successfully.\n\n"
            + "You can now log in and " + ("RECRUITER".equals(role) ? "start posting jobs and managing applications." : "start exploring job opportunities.") + "\n\n"
            + "Best Regards,\nHireFlow Recruitment Team"
        );

        model.addAttribute("msg", "Account created successfully! You can now sign in.");
        return "success";
    }

    // ── LOGIN ──

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) return "redirect:/";
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                            @RequestParam String password,
                            Model model,
                            HttpSession session) {

        User dbUser = userService.loginUser(email);

        if (dbUser != null && userService.checkPassword(password, dbUser.getPassword())) {
            session.setAttribute("loggedInUser", dbUser);
            if ("RECRUITER".equals(dbUser.getRole())) {
                return "redirect:/recruiter/dashboard";
            }
            return "redirect:/jobs";
        }

        model.addAttribute("msg", "Invalid email or password. Please try again.");
        return "login";
    }

    // ── LOGOUT ──

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ── FORGOT PASSWORD ──

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @Value("${app.base-url}")
    private String baseUrl;

    // Step 1: enter email → send reset link via email
    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam String email, Model model) {
        String token = userService.generateResetToken(email);
        if (token == null) {
            model.addAttribute("error", "No account found with that email address.");
            return "forgot-password";
        }
        String resetLink = baseUrl + "/reset-password?token=" + token;
        emailService.sendMail(
            email,
            "Reset Your HireFlow Password",
            "Hello,\n\nClick the link below to reset your password (valid for 30 minutes):\n\n"
            + resetLink + "\n\n"
            + "If you did not request this, please ignore this email.\n\n"
            + "Best Regards,\nHireFlow Recruitment Team"
        );
        model.addAttribute("msg", "Password reset link has been sent to your email.");
        return "success";
    }

    // Direct GET (e.g. manual navigation)
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.isBlank() || userService.findByResetToken(token) == null) {
            model.addAttribute("error", "No reset request found. Please enter your email again.");
            return "reset-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    // Step 2: submit new password
    @PostMapping("/reset-password")
    public String resetPasswordSubmit(@RequestParam String token,
                                      @RequestParam String password,
                                      Model model) {
        boolean success = userService.resetPassword(token, password);
        if (!success) {
            model.addAttribute("error", "This reset link is invalid or has expired.");
            return "reset-password";
        }
        model.addAttribute("msg", "Password reset successfully! You can now sign in with your new password.");
        return "success";
    }
}
