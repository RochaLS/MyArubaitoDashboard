package com.rocha.MyArubaitoDash.controller;

import com.rocha.MyArubaitoDash.model.PasswordResetToken;
import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.repository.TokenRepository;
import com.rocha.MyArubaitoDash.service.EmailService;
import com.rocha.MyArubaitoDash.service.PasswordResetService;
import com.rocha.MyArubaitoDash.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/password-reset")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class PasswordResetController {

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
                String resetLink = "http://localhost:3000/password-reset/" + token;
                emailService.sendSimpleMessage(email, "Password Reset Request",
                        "Click the link below to reset your password:\n" + resetLink);

                return "Password reset instructions sent to your email.";
            }

            return "No user with this email found.";

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }


    }

    private String hashToken(String token) throws NoSuchAlgorithmException {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
    }
}
