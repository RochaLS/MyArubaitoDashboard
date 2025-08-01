package com.rocha.MyArubaitoDash.service;
import com.rocha.MyArubaitoDash.dto.JobDTO;
import com.rocha.MyArubaitoDash.model.Job;
import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.repository.JobRepository;
import com.rocha.MyArubaitoDash.util.OwnershipVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final EncryptionService encryptionService;
    private final WorkerService workerService;
    private final OwnershipVerifier ownershipVerifier;

    @Autowired
    public JobService(JobRepository jobRepository, EncryptionService encService, WorkerService workerService, OwnershipVerifier ownershipVerifier) {
        this.jobRepository = jobRepository;
        this.encryptionService = encService;
        this.workerService = workerService;
        this.ownershipVerifier = ownershipVerifier;
    }

    public ArrayList<Job> getJobsByWorkerId(int workerId) {
        ownershipVerifier.checkWorkerIdOwnership(workerId);

        ArrayList<Job> jobs = jobRepository.findAllByWorkerId(workerId);
        for (Job job : jobs) {
            job.setHourlyRate(new BigDecimal(encryptionService.decrypt(job.getEncryptedHourlyRate())));
            job.setTitle(encryptionService.decrypt(job.getEncryptedTitle()));
        }

        return jobs;
    }

    public Job getJobById(int id) {
        Optional<Job> jobOpt = jobRepository.findById(id);

        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();

            ownershipVerifier.checkJobOwnership(job);

            job.setHourlyRate(new BigDecimal(encryptionService.decrypt(job.getEncryptedHourlyRate())));
            job.setTitle(encryptionService.decrypt(job.getEncryptedTitle()));
            return job;
        }
        return null;
    }

    public void addJob(Job job) {
        try {
            Worker currentWorker = ownershipVerifier.getCurrentWorker();
            if (job.getWorker() == null || job.getWorker().getId() != currentWorker.getId()) {
                throw new AccessDeniedException("You can only add jobs for your own account.");
            }

            job.setEncryptedHourlyRate(encryptionService.encrypt(job.getHourlyRate().toString()));
            job.setHourlyRate(null);
            job.setEncryptedTitle(encryptionService.encrypt(job.getTitle()));
            job.setTitle(null);

            jobRepository.save(job);
        } catch (Exception e) {
            System.out.println("Unexpected Error");
            e.printStackTrace();
        }
    }

    public void updateJob(int id, JobDTO updatedJob) {
        try {
            Optional<Job> optionalJob = jobRepository.findById(id);

            if (optionalJob.isPresent()) {
                Job jobToBeUpdated = optionalJob.get();

                ownershipVerifier.checkJobOwnership(jobToBeUpdated);

                Worker currentWorker = ownershipVerifier.getCurrentWorker();

                jobToBeUpdated.setTitle(updatedJob.getTitle());
                jobToBeUpdated.setHourlyRate(updatedJob.getHourlyRate());
                jobToBeUpdated.setWorker(currentWorker);
                jobToBeUpdated.setEncryptedHourlyRate(encryptionService.encrypt(updatedJob.getHourlyRate().toString()));
                jobToBeUpdated.setEncryptedTitle(encryptionService.encrypt(updatedJob.getTitle()));

                jobRepository.save(jobToBeUpdated);
            }
        } catch (Exception e) {
            System.out.println("Unexpected Error");
            e.printStackTrace();
        }
    }

    public void deleteJob(int id) {
        try {
            Optional<Job> jobToBeDeletedFound = jobRepository.findById(id);
            if (jobToBeDeletedFound.isPresent()) {
                Job jobToBeDeleted = jobToBeDeletedFound.get();

                // 👮 Enforce ownership
                ownershipVerifier.checkJobOwnership(jobToBeDeleted);

                jobRepository.delete(jobToBeDeleted);
                System.out.println("Job of id: " + jobToBeDeleted.getId() + " deleted!");
            }
        } catch (Exception e) {
            System.out.println("Error deleting job.");
            e.printStackTrace();
        }
    }
}
