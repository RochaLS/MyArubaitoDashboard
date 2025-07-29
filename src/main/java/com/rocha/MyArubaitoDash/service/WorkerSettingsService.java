package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.model.WorkerSettings;
import com.rocha.MyArubaitoDash.repository.WorkerRepository;
import com.rocha.MyArubaitoDash.repository.WorkerSettingsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WorkerSettingsService {

    private final WorkerSettingsRepository workerSettingsRepository;
    private final WorkerRepository workerRepository;

    public WorkerSettingsService(WorkerSettingsRepository workerSettingsRepository,
                                 WorkerRepository workerRepository) {
        this.workerSettingsRepository = workerSettingsRepository;
        this.workerRepository = workerRepository;
    }

    // Get settings by worker ID (create default if missing)
    public WorkerSettings getSettingsByWorkerId(int workerId) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found with ID: " + workerId));

        return workerSettingsRepository.findWorkerSettingsByWorkerId(worker.getId())
                .orElseGet(() -> createDefaultSettings(worker));
    }

    // Update pay multiplier
    public WorkerSettings updateSettings(int workerId, BigDecimal newMultiplier) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found with ID: " + workerId));

        WorkerSettings settings = workerSettingsRepository.findWorkerSettingsByWorkerId(worker.getId())
                .orElseGet(() -> createDefaultSettings(worker));

        settings.setPayMultiplier(newMultiplier != null ? newMultiplier : BigDecimal.valueOf(1.5));
        return workerSettingsRepository.save(settings);
    }

    // Internal default creator
    private WorkerSettings createDefaultSettings(Worker worker) {
        WorkerSettings settings = new WorkerSettings();
        settings.setWorker(worker);
        settings.setPayMultiplier(BigDecimal.valueOf(1.5));
        return workerSettingsRepository.save(settings);
    }
}