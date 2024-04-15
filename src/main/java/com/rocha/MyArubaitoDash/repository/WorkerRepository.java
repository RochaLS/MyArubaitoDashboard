package com.rocha.MyArubaitoDash.repository;

import com.rocha.MyArubaitoDash.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerRepository extends JpaRepository<Worker, Integer> {

    Worker findWorkerByName(String name);
    //Custom queries here
}
