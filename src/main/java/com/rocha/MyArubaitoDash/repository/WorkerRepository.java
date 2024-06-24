package com.rocha.MyArubaitoDash.repository;

import com.rocha.MyArubaitoDash.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Integer> {

    Worker findWorkerByName(String name);
    Worker findWorkerByEmail(String email);
    //Custom queries here
}
