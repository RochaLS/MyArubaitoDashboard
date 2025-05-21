package com.rocha.MyArubaitoDash.controller;

import com.rocha.MyArubaitoDash.dto.JobDTO;
import com.rocha.MyArubaitoDash.model.Job;
import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.service.JobService;
import com.rocha.MyArubaitoDash.service.WorkerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/job")
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    private final JobService jobService;
    private final WorkerService workerService;

    @Autowired
    public JobController(JobService jobService, WorkerService workerService) {
        this.jobService = jobService;
        this.workerService = workerService;
    }

    @GetMapping("/byWorker/{id}")
    public ResponseEntity<List<Job>> getJobByWorkerId(@PathVariable int id) {
        logger.info("Fetching jobs for Worker ID: {}", id);
        List<Job> jobs = jobService.getJobsByWorkerId(id);
        logger.info("Found {} jobs for Worker ID: {}", jobs.size(), id);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable int id) {
        logger.info("Fetching job with ID: {}", id);
        Job job = jobService.getJobById(id);
        logger.info("Job found: {}", job);
        return ResponseEntity.ok(job);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addJob(@Valid @RequestBody JobDTO jobDTO) {
        logger.info("Request to add a job with details: {}", jobDTO);
        try {
            Worker worker = workerService.getWorkerById(jobDTO.getWorkerId());
            if (worker == null) {
                logger.warn("Worker not found for Worker ID: {}", jobDTO.getWorkerId());
                return new ResponseEntity<>("Worker not found.", HttpStatus.BAD_REQUEST);
            }

            Job job = new Job();
            job.setId(jobDTO.getId());
            job.setTitle(jobDTO.getTitle());
            job.setHourlyRate(jobDTO.getHourlyRate());
            job.setWorker(worker);

            jobService.addJob(job);
            logger.info("Job added successfully for Worker ID: {}", jobDTO.getWorkerId());
            return new ResponseEntity<>("Job Added!", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error adding job: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error adding job: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateJob(@PathVariable int id, @Valid @RequestBody JobDTO jobDTO) {
        logger.info("Request to update job with ID: {} and details: {}", id, jobDTO);
        try {
            jobService.updateJob(id, jobDTO);
            logger.info("Job updated successfully with ID: {}", id);
            return new ResponseEntity<>("Job updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating job with ID: {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("Error updating job: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable int id) {
        logger.info("Request to delete job with ID: {}", id);
        try {
            jobService.getJobById(id); // Ensure job exists
            jobService.deleteJob(id);
            logger.info("Job deleted successfully with ID: {}", id);
            return new ResponseEntity<>("Job deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting job with ID: {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("Error deleting job: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
