package com.rocha.MyArubaitoDash.repository;

import com.rocha.MyArubaitoDash.model.WorkerSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkerSettingsRepository extends JpaRepository<WorkerSettings, Integer> {

    Optional<WorkerSettings> findWorkerSettingsByWorkerId(int workerId);
}
