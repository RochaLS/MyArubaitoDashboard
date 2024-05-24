package com.rocha.MyArubaitoDash.controller;


import com.rocha.MyArubaitoDash.security.SecurityUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class LoginController {


    @GetMapping("/login")
    public ResponseEntity<String> login(Authentication a, HttpSession httpSession) {


        SecurityUser securityUser = (SecurityUser) a.getPrincipal();
        httpSession.setAttribute("userId", String.valueOf(securityUser.worker.getId()));

//        Cookie cookie = new Cookie("userId", String.valueOf(securityUser.worker.getId()));
//        cookie.setPath("/");



        return ResponseEntity.ok(String.valueOf(securityUser.worker.getId()));

//        return "Session for " + securityUser.worker.getEmail() + " with session id: " + httpSession.getId() + " created.";
    }

}
