package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.repository.*;
import com.rocha.MyArubaitoDash.util.OwnershipVerifier;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final AIUsageRepository aiUsageRepository;
    private final JobRepository jobRepository;
    private final EncryptionService encryptionService;
    private final PasswordEncoder passwordEncoder;
    private final ShiftRepository shiftRepository;
    private final OwnershipVerifier ownershipVerifier;
    private final WorkerSettingsRepository workerSettingsRepository;

    @Autowired
    public WorkerService(
            WorkerRepository workerRepository,
            EncryptionService encryptionService,
            PasswordEncoder passwordEncoder,
            ShiftRepository shiftRepository,
            JobRepository jobRepository,
            AIUsageRepository aiUsageRepository,
            OwnershipVerifier ownershipVerifier,
            WorkerSettingsRepository workerSettingsRepository
    ) {
        this.workerRepository = workerRepository;
        this.encryptionService = encryptionService;
        this.passwordEncoder = passwordEncoder;
        this.shiftRepository = shiftRepository;
        this.jobRepository = jobRepository;
        this.aiUsageRepository = aiUsageRepository;
        this.ownershipVerifier = ownershipVerifier;
        this.workerSettingsRepository = workerSettingsRepository;
    }

    public Worker getWorkerById(int id) {
        ownershipVerifier.checkWorkerIdOwnership(id);

        Worker worker = workerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Worker not found"));

        if (worker.getEncryptedLocation() != null) {
            worker.setLocation(encryptionService.decrypt(worker.getEncryptedLocation()));
        }

        return worker;
    }

    public boolean checkWorkerByEmail(String email) {
        return workerRepository.findWorkerByEmail(email) != null;
    }

    public Worker getWorkerByEmail(String email) {
        return workerRepository.findWorkerByEmail(email);
    }

    public void addWorker(Worker worker) {
        try {
            if (worker.getLocation() != null) {
                worker.setEncryptedLocation(encryptionService.encrypt(worker.getLocation()));
            }

            String hashedPassword = passwordEncoder.encode(worker.getPassword());
            worker.setPassword(hashedPassword);

            workerRepository.save(worker);
            System.out.println("Worker with id: " + worker.getId() + " successfully added.");
        } catch (Exception e) {
            System.out.println("Unexpected error saving worker");
            e.printStackTrace();
        }
    }

    public void updateWorker(int id, Worker updatedWorker) {
        ownershipVerifier.checkWorkerIdOwnership(id);

        try {
            Worker worker = workerRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Worker not found"));

            updatedWorker.setId(worker.getId());
            updatedWorker.setEncryptedLocation(encryptionService.encrypt(updatedWorker.getLocation()));
            updatedWorker.setPassword(worker.getPassword()); // preserve existing password

            workerRepository.save(updatedWorker);
            System.out.println("Worker with id: " + id + " successfully updated.");

        } catch (Exception e) {
            System.out.println("Unexpected error updating worker with id: " + id);
            e.printStackTrace();
        }
    }

    @Transactional
    public void deleteWorker(int id) {
        ownershipVerifier.checkWorkerIdOwnership(id);

        try {
            Worker worker = workerRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Worker not found"));

            deleteAllWorkerData(worker.getId());
            System.out.println("Worker with id: " + id + " successfully deleted.");
        } catch (Exception e) {
            System.out.println("Unexpected error deleting worker with id: " + id);
            e.printStackTrace();
        }
    }

    public void deleteAllWorkerData(int workerId) {
        ownershipVerifier.checkWorkerIdOwnership(workerId);

        aiUsageRepository.deleteAllByWorkerId(workerId);
        shiftRepository.deleteAllByWorkerId(workerId);
        jobRepository.deleteAllByWorkerId(workerId);
        workerSettingsRepository.deleteAllByWorkerId(workerId);
        workerRepository.deleteById(workerId);
    }
}