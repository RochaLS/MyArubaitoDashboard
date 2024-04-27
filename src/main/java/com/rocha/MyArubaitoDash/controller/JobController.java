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

import java.util.List;

@RestController
@RequestMapping("/api/job")
public class JobController {

    private final JobService jobService;
    private final WorkerService workerService;

    @Autowired
    public JobController(JobService jobService, WorkerService workerService) {
        this.jobService = jobService;
        this.workerService = workerService;
    }

    @GetMapping("/byWorker/{id}")
    public ResponseEntity<List<Job>> getJobByWorkerId(@PathVariable int id) {
        List<Job> jobs = jobService.getJobsByWorkerId(id);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable int id) {
        Job job = jobService.getJobById(id);

        return ResponseEntity.ok(job);
    }

    /*
    DTO is necessary here as in the payload we're only sending the id of the worker and not the full worker
    object. When sending the full object springboot was having issues in parsing it.
     */
    @PostMapping("/add")
    public ResponseEntity<String> addJob(@Valid @RequestBody JobDTO jobDTO) {
        try {
            Worker worker = workerService.getWorkerById(jobDTO.getWorkerId());

            if (worker == null) {
                return new ResponseEntity<>("Worker not found.", HttpStatus.BAD_REQUEST);
            }

            Job job = new Job();
            job.setId(jobDTO.getId());
            job.setTitle(jobDTO.getTitle());
            job.setHourlyRate(jobDTO.getHourlyRate());
            job.setWorker(worker);

            jobService.addJob(job);
            return new ResponseEntity<>("Job Added!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding job: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateJob(@PathVariable int id, @Valid @RequestBody JobDTO jobDTO) {
        try {
            jobService.updateJob(id, jobDTO);
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
