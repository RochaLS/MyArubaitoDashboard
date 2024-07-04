package com.rocha.MyArubaitoDash.controller;


import com.rocha.MyArubaitoDash.security.SecurityUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class LoginController {


    @PostMapping("/login")
    public ResponseEntity<String> login(Authentication a, HttpSession httpSession) {


        SecurityUser securityUser = (SecurityUser) a.getPrincipal();
        httpSession.setAttribute("userId", String.valueOf(securityUser.worker.getId()));


        return new ResponseEntity<>(String.valueOf(securityUser.worker.getId()),  HttpStatus.OK);




//        return "Session for " + securityUser.worker.getEmail() + " with session id: " + httpSession.getId() + " created.";
    }

}
