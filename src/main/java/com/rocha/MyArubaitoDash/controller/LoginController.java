package com.rocha.MyArubaitoDash.controller;


import com.rocha.MyArubaitoDash.security.SecurityUser;
import com.rocha.MyArubaitoDash.service.IncomeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class LoginController {




    @GetMapping("/login")
    public String login(Authentication a, HttpSession httpSession) {


        SecurityUser securityUser = (SecurityUser) a.getPrincipal();
        httpSession.setAttribute("userId", String.valueOf(securityUser.worker.getId()));



        return "Session for " + securityUser.worker.getEmail() + " with session id: " + httpSession.getId() + " created.";
    }

}
