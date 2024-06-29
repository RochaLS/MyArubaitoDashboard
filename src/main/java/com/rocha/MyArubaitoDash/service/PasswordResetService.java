package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.model.PasswordResetToken;
import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.repository.TokenRepository;
import com.rocha.MyArubaitoDash.repository.WorkerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    private final WorkerService workerService;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    private final WorkerRepository workerRepository;


    public PasswordResetService(WorkerService workerService, TokenRepository tokenRepository, PasswordEncoder passwordEncoder, WorkerRepository workerRepository) {
        this.workerService = workerService;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.workerRepository = workerRepository;
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

    public boolean validateToken(String token) throws NoSuchAlgorithmException {
        String hashedToken = hashToken(token);
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(hashedToken);
        return resetToken.isPresent() && resetToken.get().getExpiryDate().isAfter(LocalDateTime.now()); // Checks if expiration date is after current time, if yes it's valid if not, not valid.
    }

    public void resetPassword(String token, String newPassword) throws NoSuchAlgorithmException {
        String hashedToken = hashToken(token);
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(hashedToken);
        if (resetToken.isPresent()) {
            PasswordResetToken tokenEntity = resetToken.get();
            Worker user = workerService.getWorkerByEmail(tokenEntity.getEmail());

            user.setPassword(passwordEncoder.encode(newPassword));
            //Saving direct through de repository. I want this flow to be separate of other user general updates
            workerRepository.save(user);


            tokenRepository.delete(tokenEntity);
        }
    }
}
