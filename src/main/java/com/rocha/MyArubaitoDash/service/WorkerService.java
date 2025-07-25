package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.repository.AIUsageRepository;
import com.rocha.MyArubaitoDash.repository.JobRepository;
import com.rocha.MyArubaitoDash.repository.ShiftRepository;
import com.rocha.MyArubaitoDash.repository.WorkerRepository;
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

    @Autowired
    public WorkerService(WorkerRepository workerRepository, EncryptionService encryptionService, PasswordEncoder passwordEncoder, ShiftRepository shiftRepository, JobRepository jobRepository, AIUsageRepository aiUsageRepository) {
        this.workerRepository = workerRepository;
        this.encryptionService = encryptionService;
        this.passwordEncoder = passwordEncoder;
        this.shiftRepository = shiftRepository;
        this.jobRepository = jobRepository;
        this.aiUsageRepository = aiUsageRepository;
    }

    public Worker getWorkerById(int id) {
        Optional<Worker> worker = workerRepository.findById(id);
        if (worker.isPresent()) {
            Worker workerFound = worker.get();
            if (workerFound.getEncryptedLocation() != null) {
                workerFound.setLocation(encryptionService.decrypt(workerFound.getEncryptedLocation()));
            }

            return workerFound;
        }

        return null;
    }

    public boolean checkWorkerByEmail(String email) {
        Worker worker = workerRepository.findWorkerByEmail(email);

        return worker != null;
    }

    public Worker getWorkerByEmail(String email) {
        return workerRepository.findWorkerByEmail(email);
    }


    public void addWorker(Worker worker) {
        try {
            if (worker.getLocation() != null) {
                worker.setEncryptedLocation(encryptionService.encrypt(worker.getLocation()));
            } else {
                worker.setEncryptedLocation(null);
            }
            String hashedPassword = passwordEncoder.encode(worker.getPassword());
            System.out.println("Hashed pass: " + hashedPassword);
            worker.setPassword(hashedPassword);
            workerRepository.save(worker);
            System.out.println("Worker with id: " + worker.getId() + " successfully added.");
        } catch (Exception e) {
            System.out.println("Unexpected Error saving worker");
            e.printStackTrace();
        }
    }

    public void updateWorker(int id, Worker updatedWorker) {
        try {
            Optional<Worker> workerToBeUpdated = workerRepository.findById(id);
            if (workerToBeUpdated.isPresent()) {
                Worker worker = workerToBeUpdated.get();
                updatedWorker.setEncryptedLocation(encryptionService.encrypt(updatedWorker.getLocation()));
                updatedWorker.setId(worker.getId());
                updatedWorker.setPassword(worker.getPassword()); // Temporary solution


                workerRepository.save(updatedWorker);
                System.out.println("Worker with id: " + id + " successfully updated.");

            }



        } catch (Exception e) {
            System.out.println("Unexpected Error updating worker with id: " + id);
            e.printStackTrace();

        }
    }

    @Transactional
    public void deleteWorker(int id) {
        try {
            Optional<Worker> workerToBeDeletedFound = workerRepository.findById(id);
            if (workerToBeDeletedFound.isPresent()) {
                Worker workerToBeDeleted = workerToBeDeletedFound.get();
                deleteAllWorkerData(workerToBeDeleted.getId());
                System.out.println("Worker with id: " + workerToBeDeleted.getId() + " successfully deleted.");
            }

        } catch (Exception e) {
            System.out.println("Unexpected Error deleting worker with id: " + id);
            e.printStackTrace();
        }
    }


    public void deleteAllWorkerData(int workerId) {
        aiUsageRepository.deleteAllByWorkerId(workerId);
        shiftRepository.deleteAllByWorkerId(workerId);
        jobRepository.deleteAllByWorkerId(workerId);
        workerRepository.deleteById(workerId);
    }

}
