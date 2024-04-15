package com.rocha.MyArubaitoDash.controller;


import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {


    @GetMapping("/login")
    public String login(Authentication a) {
        return "Hey " + a.getName();
    }
}
