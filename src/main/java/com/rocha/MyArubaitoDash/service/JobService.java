package com.rocha.MyArubaitoDash.service;
import com.rocha.MyArubaitoDash.model.Job;
import com.rocha.MyArubaitoDash.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class JobService {

    private final JobRepository jobRepository;

    @Autowired
    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public ArrayList<Job> getJobsByWorkerId(int workerId) {
        return jobRepository.findAllByWorkerId(workerId);
    }

    public Job getJobById(int id) {
        Optional<Job> job = jobRepository.findById(id);

        if (job.isPresent()) {
            return job.get();
        } else  {
            return null;
        }
    }

    public boolean addJob(Job job) {
        try {
            jobRepository.save(job);
            return true;
        } catch (Exception e) {
            System.out.println("Unexpected Error");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateJob(Job updatedJob) {
        try {
            jobRepository.save(updatedJob);
            return true;
        } catch (Exception e) {
            System.out.println("Unexpected Error");
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteJob(Job jobToBeDeleted) {
        try {
            jobRepository.delete(jobToBeDeleted);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
