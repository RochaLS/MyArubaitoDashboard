package com.rocha.MyArubaitoDash.controller;


import com.rocha.MyArubaitoDash.model.Session;
import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.security.SecurityUser;
import com.rocha.MyArubaitoDash.service.SessionService;
import jakarta.servlet.http.HttpSession;
import org.joda.time.DateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class LoginController {

    private final SessionService sessionService;

    public LoginController(SessionService sessionService) {
        this.sessionService = sessionService;
    }



    @GetMapping("/login")
    public String login(Authentication a, HttpSession httpSession) {
        Session session = new Session();
        session.setId(httpSession.getId());

        SecurityUser securityUser = (SecurityUser) a.getPrincipal();
        session.setTimestamp(LocalDateTime.now());
        session.setUserId(securityUser.worker.getId());


        sessionService.create(session);

        return "Session for " + securityUser.worker.getEmail() + " with session id: " + httpSession.getId() + " created.";
    }

}
