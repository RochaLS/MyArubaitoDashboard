package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.repository.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;

    @Autowired
    public WorkerService(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    public Worker getWorkerById(int id) {
        Optional<Worker> worker = workerRepository.findById(id);
        if (worker.isPresent()) {
            return worker.get();
        }

        return null;
    }

    public boolean addWorker(Worker worker) {
        try {
            workerRepository.save(worker);
            System.out.println("Worker with id: " + worker.getId() + "successfully added.");
            return true;
        } catch (Exception e) {
            System.out.println("Unexpected Error saving worker");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateWorker(Worker updatedWorker) {
        try {
            workerRepository.save(updatedWorker);
            System.out.println("Worker with id: " + updatedWorker.getId() + "successfully updated.");
            return true;
        } catch (Exception e) {
            System.out.println("Unexpected Error updating worker with id: " + updatedWorker.getId());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteWorker(Worker workerToBeDeleted) {
        try {
            workerRepository.delete(workerToBeDeleted);
            System.out.println("Worker with id: " + workerToBeDeleted.getId() + "successfully deleted.");
            return true;
        } catch (Exception e) {
            System.out.println("Unexpected Error deleting worker with id: " + workerToBeDeleted.getId());
            e.printStackTrace();
            return false;
        }
    }

}
