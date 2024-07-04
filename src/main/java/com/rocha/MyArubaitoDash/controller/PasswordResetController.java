package com.rocha.MyArubaitoDash.controller;

import com.rocha.MyArubaitoDash.service.EmailService;
import com.rocha.MyArubaitoDash.service.PasswordResetService;
import com.rocha.MyArubaitoDash.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.NoSuchAlgorithmException;


@RestController
@RequestMapping("/password-reset")
public class PasswordResetController {

    @Value("${RESET_PASSWORD_URL}")
    private String resetPasswordUrl;

    private final EmailService emailService;
    private final WorkerService workerService;
    private final PasswordResetService passwordResetService;



    @Autowired
    public PasswordResetController(PasswordResetService passwordResetService, WorkerService workerService, EmailService emailService) {
        this.passwordResetService = passwordResetService;
        this.workerService = workerService;
        this.emailService = emailService;

    }

    @PostMapping("/request")
    public String requestPasswordReset(@RequestBody String email) {

        boolean hasAccount = workerService.checkWorkerByEmail(email);
        // Generate and store password reset token
        // Send email with password reset link containing token
        try {
            String token = passwordResetService.createPasswordResetToken(email);
            if (token != null) {
                String resetLink = resetPasswordUrl + token;
                emailService.sendSimpleMessage(email, "Password Reset Request",
                        "Click the link below to reset your password:\n" + resetLink);

                return "Password reset instructions sent to your email.";
            }

            return "No user with this email found.";

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestBody String newPassword) {
        try {
            boolean isTokenValid = passwordResetService.validateToken(token);
            if (isTokenValid) {
                passwordResetService.resetPassword(token, newPassword);
                return ResponseEntity.ok("Password reset successfully.");
            } else {
                return ResponseEntity.badRequest().body("Invalid or expired token.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }



}
