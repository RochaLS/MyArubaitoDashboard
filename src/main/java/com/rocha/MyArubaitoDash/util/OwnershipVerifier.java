package com.rocha.MyArubaitoDash.util;

import com.rocha.MyArubaitoDash.model.Job;
import com.rocha.MyArubaitoDash.model.Shift;
import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.repository.WorkerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class OwnershipVerifier {

    private final WorkerRepository workerRepository;

    @Autowired
    public OwnershipVerifier(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    public Worker getCurrentWorker() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return workerRepository.findWorkerByEmail(email);
    }

    public void checkWorkerIdOwnership(int workerId) {
        Worker current = getCurrentWorker();
        if (current.getId() != workerId) {
            throw new AccessDeniedException("Unauthorized access to this worker’s data");
        }
    }

    public void checkJobOwnership(Job job) {
        Worker current = getCurrentWorker();
        if (!job.getWorker().getEmail().equals(current.getEmail())) {
            throw new AccessDeniedException("Unauthorized access to this job");
        }
    }

    public void checkShiftOwnership(Shift shift) {
        Worker current = getCurrentWorker();
        if (!shift.getWorker().getEmail().equals(current.getEmail())) {
            throw new AccessDeniedException("Unauthorized access to this shift");
        }
    }
}