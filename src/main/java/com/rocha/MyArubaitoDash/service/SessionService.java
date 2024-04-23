package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.model.Session;
import com.rocha.MyArubaitoDash.repository.SessionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Session getById(String id) {

        return sessionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Session not found."));
    }

    public Session getByUsername(String username) {
        Session session = sessionRepository.findByUsername(username);

        if (session == null) {
            throw new EntityNotFoundException("Session not found.");
        }

        return session;
    }

    public void create(Session session) {
        sessionRepository.save(session);
    }

    public void remove(String id) {
        sessionRepository.deleteById(id);
    }
}
