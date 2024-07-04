package com.rocha.MyArubaitoDash.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SessionValidationController {

    @GetMapping("/validate-session")
    public ResponseEntity<?> validateSession(HttpSession session, @RequestParam String userId) {
        System.out.println("IM HEREEEEEEEEEEEEE");
        String sessionUserId = (String) session.getAttribute("userId");
        if (sessionUserId != null && sessionUserId.equals(userId)) {

            return ResponseEntity.ok(sessionUserId);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(sessionUserId);
        }
    }
}
