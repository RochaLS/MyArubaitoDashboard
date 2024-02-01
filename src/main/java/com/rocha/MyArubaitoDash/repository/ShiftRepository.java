package com.rocha.MyArubaitoDash.repository;

import com.rocha.MyArubaitoDash.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface ShiftRepository extends JpaRepository<Shift, Integer> {
    ArrayList<Shift> findAllByWorkerId(int workerId);
    ArrayList<Shift> findAllByJobId(int jobId);
}
