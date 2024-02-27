package com.rocha.MyArubaitoDash.controller;

import com.rocha.MyArubaitoDash.model.Job;
import com.rocha.MyArubaitoDash.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/job")
public class JobController {

    private final JobService jobService;

    @Autowired
    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable int id) {
        Job job = jobService.getJobById(id);

        return ResponseEntity.ok(job);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addJob(@RequestBody Job job) {
        try {
            jobService.addJob(job);
            return new ResponseEntity<>("Job Added!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding job: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateJob(@PathVariable int id, @RequestBody Job job) {
        try {
            jobService.updateJob(id, job);
            return new ResponseEntity<>("Job updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating job: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable int id) {
        try {
            jobService.getJobById(id); // Ensure job exists
            jobService.deleteJob(id);
            return new ResponseEntity<>("Job deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting job: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





}
