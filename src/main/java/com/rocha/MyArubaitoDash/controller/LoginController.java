package com.rocha.MyArubaitoDash.controller;


import com.rocha.MyArubaitoDash.model.Session;
import com.rocha.MyArubaitoDash.service.SessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private final SessionService sessionService;

    public LoginController(SessionService sessionService) {
        this.sessionService = sessionService;
    }



    @GetMapping("/login")
    public String login(Authentication a, HttpSession httpSession) {
        Session session = new Session();
        session.setUsername(a.getName());
        session.setId(httpSession.getId());

        Object principal = a.getPrincipal();
        String userId = null;
        if (principal instanceof UserDetails) {
            userId = ((UserDetails) principal).getUsername();
        } else {
            userId = principal.toString();
        }

        session.setUserId(userId);


        sessionService.create(session);

        return "Session for " + a.getName() + " with session id: " + httpSession.getId() + " created.";
    }

}
