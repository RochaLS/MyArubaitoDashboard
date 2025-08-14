package com.rocha.MyArubaitoDash.controller;

import com.nimbusds.jwt.JWTClaimsSet;
import com.rocha.MyArubaitoDash.dto.AppleLoginRequest;
import com.rocha.MyArubaitoDash.jwt.AppleIdTokenValidator;
import com.rocha.MyArubaitoDash.jwt.JwtTokenUtil;
import com.rocha.MyArubaitoDash.model.JwtRequest;
import com.rocha.MyArubaitoDash.model.JwtResponse;
import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.repository.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/mobile/auth")
public class JwtAuthController {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthController.class);

    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final WorkerRepository workerRepository;
    private final AppleIdTokenValidator appleIdTokenValidator;

    @Autowired
    public JwtAuthController(JwtTokenUtil jwtTokenUtil, AuthenticationManager authenticationManager,
                             UserDetailsService userDetailsService, WorkerRepository workerRepository, AppleIdTokenValidator appleIdTokenValidator) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.workerRepository = workerRepository;
        this.appleIdTokenValidator = appleIdTokenValidator;
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {
        try {
            // Log attempt without sensitive data
            logger.info("Attempting authentication for email: {}", authenticationRequest.getUsername());

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );

            // Log success
            logger.info("Authentication successful for email: {}", authenticationRequest.getUsername());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get authenticated user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Worker worker = workerRepository.findWorkerByEmail(userDetails.getUsername());

            if (worker == null) {
                logger.warn("No worker found with email: {}", userDetails.getUsername());
                return ResponseEntity.status(404).body("Worker not found for the given username.");
            }

            // Generate token
            final String token = jwtTokenUtil.generateToken(userDetails.getUsername(), worker.getId());
            return ResponseEntity.ok(new JwtResponse(token, false));

        } catch (AuthenticationException e) {
            // Log failure
            logger.error("Authentication failed for username: {}. Reason: {}",
                    authenticationRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String email = jwtTokenUtil.extractEmail(token);

            if (email != null && jwtTokenUtil.validateToken(token, email)) {
                Worker worker = workerRepository.findWorkerByEmail(email);
                final String newToken = jwtTokenUtil.generateToken(email, worker.getId());
                logger.info("Token refreshed successfully for user: {}", email);
                return ResponseEntity.ok(new JwtResponse(newToken, false));
            }

            logger.warn("Invalid token provided for refresh.");
            return ResponseEntity.status(401).body("Invalid token");

        } catch (Exception e) {
            logger.error("Token refresh failed. Reason: {}", e.getMessage());
            return ResponseEntity.status(401).body("Token refresh failed: " + e.getMessage());
        }
    }

    @PostMapping("validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String email = jwtTokenUtil.extractEmail(token);

            if (email != null && jwtTokenUtil.validateToken(token, email)) {
                logger.info("Token validation successful for user: {}", email);
                return ResponseEntity.ok().body("Token is valid");
            }

            logger.warn("Invalid token provided for validation.");
            return ResponseEntity.status(401).body("Invalid token");

        } catch (Exception e) {
            logger.error("Token validation failed. Reason: {}", e.getMessage());
            return ResponseEntity.status(401).body("Token validation failed: " + e.getMessage());
        }
    }

    @PostMapping("/apple-login")
    public ResponseEntity<?> appleLogin(@RequestBody AppleLoginRequest appleLoginRequest) {
        try {
            logger.info("Attempting Apple login for clientId: {}", appleLoginRequest.getClientId());

            // Validate the ID token using AppleIdTokenValidator
            JWTClaimsSet claims = appleIdTokenValidator.validateIdToken(appleLoginRequest.getIdToken(), appleLoginRequest.getClientId());

            // Extract user information
            String appleUserId = claims.getSubject();
            String email = claims.getStringClaim("email");

            boolean isNewUser = false;

            // Log successful validation
            logger.info("Apple ID token successfully validated. UserId: {}, Email: {}", appleUserId, email);

            // Check if the user exists in the database
            Worker worker = workerRepository.findWorkerByEmail(email);

            if (worker == null) {
                // Register the user if they don't exist
                logger.info("Worker not found, creating new worker with email: {}", email);
                isNewUser = true;
                worker = new Worker();
                worker.setEmail(email);
                String nameFromEmail = email.split("@")[0];
                worker.setName(nameFromEmail);
                worker.setAppleUserId(appleUserId);
                workerRepository.save(worker);
            }

            // Generate a JWT token for the user
            String token = jwtTokenUtil.generateToken(worker.getEmail(), worker.getId());

            logger.info("Generated JWT token for Apple user: {}", appleUserId);

            return ResponseEntity.ok(new JwtResponse(token, isNewUser));

        } catch (Exception e) {
            logger.error("Apple login failed. Reason: {}", e.getMessage());
            return ResponseEntity.status(401).body("Apple login failed: " + e.getMessage());
        }
    }
}
