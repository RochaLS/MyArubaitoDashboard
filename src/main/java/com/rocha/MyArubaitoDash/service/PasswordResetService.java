package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.model.PasswordResetToken;
import com.rocha.MyArubaitoDash.repository.TokenRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class PasswordResetService {
    private final WorkerService workerService;
    private final TokenRepository tokenRepository;


    public PasswordResetService(WorkerService workerService, TokenRepository tokenRepository) {
        this.workerService = workerService;
        this.tokenRepository = tokenRepository;
    }

    public String createPasswordResetToken(String email) throws NoSuchAlgorithmException {
        boolean hasAccount = workerService.checkWorkerByEmail(email);

        if (hasAccount) {
            String token = generateToken();
            String hashedToken = hashToken(token);
            LocalDateTime expirationTime = LocalDateTime.now().plusHours(1); // Token valid for 1 hour

            PasswordResetToken passwordResetToken = new PasswordResetToken();
            passwordResetToken.setEmail(email);
            passwordResetToken.setToken(hashedToken);
            passwordResetToken.setExpiryDate(expirationTime);

            tokenRepository.save(passwordResetToken);

            return token;
        }

        return null;

    }

    private String hashToken(String token) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
