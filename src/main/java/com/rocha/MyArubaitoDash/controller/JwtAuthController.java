package com.rocha.MyArubaitoDash.controller;

import com.rocha.MyArubaitoDash.jwt.JwtTokenUtil;
import com.rocha.MyArubaitoDash.model.JwtRequest;
import com.rocha.MyArubaitoDash.model.JwtResponse;
import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.repository.WorkerRepository;
import com.rocha.MyArubaitoDash.service.CustomUserDetailsService;
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

import static org.apache.http.impl.auth.BasicScheme.authenticate;

@RestController
@RequestMapping("/api/mobile/auth")
public class JwtAuthController {

    final private JwtTokenUtil jwtTokenUtil;
    final private AuthenticationManager authenticationManager;
    final private UserDetailsService userDetailsService;
    private final WorkerRepository workerRepository;

    @Autowired
    public JwtAuthController(JwtTokenUtil jwtTokenUtil, AuthenticationManager authenticationManager, UserDetailsService userDetailsService, WorkerRepository workerRepository) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.workerRepository = workerRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {
        try {
            // Spring Security handles credential verification
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );

            // If we get here, authentication was successful
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get authenticated user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            System.out.println(userDetails.getUsername());

            // Username is email, sorry i know it's weird
            Worker worker = workerRepository.findWorkerByEmail(userDetails.getUsername());



            // Generate token
            final String token = jwtTokenUtil.generateToken(userDetails.getUsername(), worker.getId());

            return ResponseEntity.ok(new JwtResponse(token));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String email = jwtTokenUtil.extractEmail(token);

            // Check if token is valid
            if (email != null && jwtTokenUtil.validateToken(token, email)) {
                Worker worker = workerRepository.findWorkerByEmail(email);
                final String newToken = jwtTokenUtil.generateToken(email, worker.getId());
                return ResponseEntity.ok(new JwtResponse(newToken));
            }

            return ResponseEntity.status(401).body("Invalid token");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token refresh failed: " + e.getMessage());
        }
    }

    @PostMapping("validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7);
            String email = jwtTokenUtil.extractEmail(token);

            // Check if token is valid
            if (email != null && jwtTokenUtil.validateToken(token, email)) {
                return ResponseEntity.ok().body("Token is valid");
            }

            return ResponseEntity.status(401).body("Invalid token");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token refresh failed: " + e.getMessage());
        }
    }



}
