package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.repository.WorkerRepository;
import com.rocha.MyArubaitoDash.security.SecurityUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


public class CustomUserDetailsService implements UserDetailsService {

    private final WorkerRepository workerRepository;

    public CustomUserDetailsService(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Worker worker = workerRepository.findWorkerByEmail(email);

        if (worker == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return new SecurityUser(worker);

    }
}
