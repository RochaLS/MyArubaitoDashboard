package com.rocha.MyArubaitoDash.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SessionValidationController {

    @GetMapping("/validate-session")
    public ResponseEntity<?> validateSession(HttpSession session, @RequestParam String userId) {
        String sessionUserId = (String) session.getAttribute("userId");
        System.out.println("==========\nSESSION ID: " + sessionUserId);
        if (sessionUserId != null && sessionUserId.equals(userId)) {

            return ResponseEntity.ok(sessionUserId);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(sessionUserId);
        }
    }
}
