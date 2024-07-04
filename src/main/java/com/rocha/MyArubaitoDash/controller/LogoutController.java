package com.rocha.MyArubaitoDash.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LogoutController {

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        try {
            request.getSession().invalidate();
            return new ResponseEntity<>("Logout successful.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error during logout: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
