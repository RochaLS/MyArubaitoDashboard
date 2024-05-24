package com.rocha.MyArubaitoDash.controller;

import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.service.WorkerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/worker")
public class WorkerController {

    private final WorkerService workerService;

    @Autowired
    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @GetMapping()
    public ResponseEntity<Worker> getWorkerById(@RequestParam int userId) {
        Worker worker = workerService.getWorkerById(userId);

        return ResponseEntity.ok(worker);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addWorker(@RequestBody Worker worker) {
        try {
            workerService.addWorker(worker);
            return new ResponseEntity<>("Worker added successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding worker: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateWorker(@PathVariable int id, @Valid @RequestBody Worker worker) {

        // I need to validate it here, as I'm marking location as @Transient. So I can't use validation annotations.
        // Maybe a DTO can fix that in the future
        if (worker.getLocation() == null || worker.getLocation().isBlank()) {
            return new ResponseEntity<>("Error updating worker: Location can't be null or blank.", HttpStatus.BAD_REQUEST);
        }
        try {
            workerService.updateWorker(id, worker);
            return new ResponseEntity<>("Worker updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating worker: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteWorker(@PathVariable int id) {
        try {
            workerService.getWorkerById(id);
            workerService.deleteWorker(id);

            return new ResponseEntity<>("Worker deleted successfully", HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting worker: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
