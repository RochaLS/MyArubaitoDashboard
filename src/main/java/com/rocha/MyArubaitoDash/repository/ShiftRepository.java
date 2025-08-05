package com.rocha.MyArubaitoDash.repository;

import com.rocha.MyArubaitoDash.model.Shift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Integer> {
    ArrayList<Shift> findAllByWorkerId(int workerId);
    ArrayList<Shift> findAllByJobId(int jobId);
    void deleteAllByWorkerId(Integer workerId);

    // Basically

    @Query(value = "SELECT * FROM shift WHERE job_id = ?1 AND worker_id = ?2 AND (start_date >= ?3) ORDER BY start_date", nativeQuery = true)
    ArrayList<Shift> findShiftsFromSpecificDate(int jobId, int workerId, LocalDate startDate);
    @Query(value = "SELECT * FROM shift WHERE  worker_id = ?1 AND (start_date >= ?2) ORDER BY start_date", nativeQuery = true)
    ArrayList<Shift> findAllShiftsByWorkerFromSpecificDate(int workerId, LocalDate startDate);

    @Query(value = "SELECT * FROM shift WHERE worker_id = ?1 AND start_date >= ?2 AND start_date <= ?3 ORDER BY start_date", nativeQuery = true)
    ArrayList<Shift> findShiftsInRange(int workerId, LocalDate startDate, LocalDate endDate);

    // Paginated queries
    @Query(value = "SELECT * FROM shift WHERE  worker_id = ?1 AND (start_date >= ?2) ORDER BY start_date", nativeQuery = true)
    Page<Shift> findAllShiftsByWorkerFromSpecificDatePaginated(int workerId, LocalDate startDate, Pageable pageable);

    @Query(value = "SELECT * FROM shift WHERE worker_id = ?1 AND " +
            "CONCAT(start_date, ' ', start_time) > ?2 " +
            "ORDER BY start_date, start_time LIMIT 1", nativeQuery = true)
    Shift findNextShiftForWorker(int workerId, LocalDateTime now);

    @Query("SELECT s FROM Shift s JOIN FETCH s.job WHERE s.worker.id = :workerId AND s.startDate >= :fromDate AND s.endDate <= :toDate")
    List<Shift> getAllShiftsInRangeWithJob(@Param("workerId") int workerId,
                                           @Param("fromDate") LocalDate fromDate,
                                           @Param("toDate") LocalDate toDate);

}
