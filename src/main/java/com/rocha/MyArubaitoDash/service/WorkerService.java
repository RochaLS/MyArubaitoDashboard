package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.repository.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final EncryptionService encryptionService;

    @Autowired
    public WorkerService(WorkerRepository workerRepository, EncryptionService encryptionService) {
        this.workerRepository = workerRepository;
        this.encryptionService = encryptionService;
    }

    public Worker getWorkerById(int id) {
        Optional<Worker> worker = workerRepository.findById(id);
        if (worker.isPresent()) {
            Worker workerFound = worker.get();
            workerFound.setLocation(encryptionService.decrypt(workerFound.getEncryptedLocation()));
            return workerFound;
        }

        return null;
    }

    public void addWorker(Worker worker) {
        try {
            worker.setEncryptedLocation(encryptionService.encrypt(worker.getLocation()));
            worker.setLocation(null);
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

                // The below conditionals check which fields are necessary to update...
                // If we don't do this the fields that are not being updated might be overridden to null.
                if (updatedWorker.getName() != null) {
                    worker.setName(updatedWorker.getName());
                }

                if (updatedWorker.getLocation() != null) {
                    worker.setEncryptedLocation(encryptionService.encrypt(updatedWorker.getLocation()));
                }

//                if (updatedWorker.getHourlyRate() != null) {
//                    worker.setHourlyRate(updatedWorker.getHourlyRate());
//                }


                workerRepository.save(worker);
                System.out.println("Worker with id: " + id + " successfully updated.");

            }



        } catch (Exception e) {
            System.out.println("Unexpected Error updating worker with id: " + id);
            e.printStackTrace();

        }
    }

    public void deleteWorker(int id) {
        try {
            Optional<Worker> workerToBeDeletedFound = workerRepository.findById(id);
            if (workerToBeDeletedFound.isPresent()) {
                Worker workerToBeDeleted = workerToBeDeletedFound.get();
                workerRepository.delete(workerToBeDeleted);
                System.out.println("Worker with id: " + workerToBeDeleted.getId() + " successfully deleted.");
            }

        } catch (Exception e) {
            System.out.println("Unexpected Error deleting worker with id: " + id);
            e.printStackTrace();
        }
    }

}
