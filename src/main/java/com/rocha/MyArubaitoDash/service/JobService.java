package com.rocha.MyArubaitoDash.service;
import com.rocha.MyArubaitoDash.model.Job;
import com.rocha.MyArubaitoDash.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final EncryptionService encryptionService;

    @Autowired
    public JobService(JobRepository jobRepository, EncryptionService encService) {

        this.jobRepository = jobRepository;
        this.encryptionService = encService;
    }

    public ArrayList<Job> getJobsByWorkerId(int workerId) {
        return jobRepository.findAllByWorkerId(workerId);
    }

    public Job getJobById(int id) {
        Optional<Job> job = jobRepository.findById(id);

        if (job.isPresent()) {
            Job jobFound = job.get();
            jobFound.setHourlyRate(new BigDecimal(encryptionService.decrypt(jobFound.getEncryptedHourlyRate())));
            jobFound.setTitle(encryptionService.decrypt(jobFound.getEncryptedTitle()));
            return job.get();
        } else  {
            return null;
        }
    }

    public void addJob(Job job) {
        try {
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

    public void updateJob(int id, Job updatedJob) {
        try {
            Optional<Job> optionalJob = jobRepository.findById(id);

            if (optionalJob.isPresent()) {
                Job jobToBeUpdated = optionalJob.get();

                if (updatedJob.getTitle() != null) {
                    jobToBeUpdated.setTitle(updatedJob.getTitle());
                }

                if (updatedJob.getHourlyRate() != null) {
                    jobToBeUpdated.setHourlyRate(updatedJob.getHourlyRate());
                }

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
                jobRepository.delete(jobToBeDeleted);

                System.out.println("Job of id: " + jobToBeDeleted.getId() + " deleted!");
            }
        } catch (Exception e) {
            System.out.println("Error deleting job.");
            e.printStackTrace();
        }
    }
}
