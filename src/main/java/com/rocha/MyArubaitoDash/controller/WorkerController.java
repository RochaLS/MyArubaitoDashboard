package com.rocha.MyArubaitoDash.controller;

import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.service.WorkerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/worker")
public class WorkerController {

    private static final Logger logger = LoggerFactory.getLogger(WorkerController.class);

    private final WorkerService workerService;

    @Autowired
    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @GetMapping()
    public ResponseEntity<Worker> getWorkerById(@RequestParam int userId) {
        logger.info("Received request to fetch worker with ID: {}", userId);
        try {
            Worker worker = workerService.getWorkerById(userId);
            logger.info("Worker found with ID: {}", userId);
            return ResponseEntity.ok(worker);
        } catch (Exception e) {
            logger.error("Error fetching worker with ID: {}. Error: {}", userId, e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> addWorker(@RequestBody Worker worker) {
        logger.info("Received request to add worker: {}", worker);
        try {
            workerService.addWorker(worker);
            logger.info("Worker added successfully: {}", worker.getEmail());
            return new ResponseEntity<>("Worker added successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error adding worker: {}. Error: {}", worker, e.getMessage());
            return new ResponseEntity<>("Error adding worker: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateWorker(@PathVariable int id, @Valid @RequestBody Worker worker) {
        logger.info("Received request to update worker with ID: {}", id);

        if (worker.getLocation() == null || worker.getLocation().isBlank()) {
            logger.warn("Error updating worker: Location can't be null or blank.");
            return new ResponseEntity<>("Error updating worker: Location can't be null or blank.", HttpStatus.BAD_REQUEST);
        }

        try {
            workerService.updateWorker(id, worker);
            logger.info("Worker updated successfully with ID: {}", id);
            return new ResponseEntity<>("Worker updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating worker with ID: {}. Error: {}", id, e.getMessage());
            return new ResponseEntity<>("Error updating worker: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteWorker(@PathVariable int id) {
        logger.info("Received request to delete worker with ID: {}", id);
        try {
            workerService.getWorkerById(id); // Ensure worker exists before deleting
            workerService.deleteWorker(id);
            logger.info("Worker deleted successfully with ID: {}", id);
            return new ResponseEntity<>("Worker deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting worker with ID: {}. Error: {}", id, e.getMessage());
            return new ResponseEntity<>("Error deleting worker: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/check-account")
    public ResponseEntity<Boolean> checkIfUserHasAccount(@RequestBody String email) {
        email = email.trim();
        logger.info("Received request to check if account exists for email: {}", email);
        try {
            boolean hasAccount = workerService.checkWorkerByEmail(email);
            logger.info("Account existence for email {}: {}", email, hasAccount);
            return ResponseEntity.ok(hasAccount);
        } catch (Exception e) {
            logger.error("Error checking account for email: {}. Error: {}", email, e.getMessage());
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}