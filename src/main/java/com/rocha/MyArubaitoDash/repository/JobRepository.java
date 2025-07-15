package com.rocha.MyArubaitoDash.repository;

import com.rocha.MyArubaitoDash.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface JobRepository extends JpaRepository<Job, Integer> {
    //Custom queries here...
    ArrayList<Job> findAllByWorkerId(int workerId);
    void deleteAllByWorkerId(Integer workerId);
}
