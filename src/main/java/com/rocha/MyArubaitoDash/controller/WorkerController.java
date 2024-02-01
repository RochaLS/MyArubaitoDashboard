package com.rocha.MyArubaitoDash.controller;

import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/worker")
public class WorkerController {

    private final WorkerService workerService;

    @Autowired
    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Worker> getWorkerById(@PathVariable int id) {
        Worker worker = workerService.getWorkerById(id);

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
    public ResponseEntity<String> updateWorker(@PathVariable int id, @RequestBody Worker worker) {
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
