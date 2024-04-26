package com.rocha.MyArubaitoDash.repository;

import com.rocha.MyArubaitoDash.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Integer> {
    ArrayList<Shift> findAllByWorkerId(int workerId);
    ArrayList<Shift> findAllByJobId(int jobId);

    // Basically

    @Query(value = "SELECT * FROM shift WHERE job_id = ?1 AND worker_id = ?2 AND (start_date >= ?3) ORDER BY start_date", nativeQuery = true)
    ArrayList<Shift> findShiftsFromSpecificDate(int jobId, int workerId, LocalDate startDate);
}
