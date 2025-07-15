package com.rocha.MyArubaitoDash.repository;

import com.rocha.MyArubaitoDash.model.AIUsage;
import com.rocha.MyArubaitoDash.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AIUsageRepository extends JpaRepository<AIUsage, Integer> {
    Optional<AIUsage> findByWorker(Worker worker);
    void deleteAllByWorkerId(Integer workerId);
}
