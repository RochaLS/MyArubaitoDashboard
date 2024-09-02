package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.model.AIUsage;
import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.repository.AIUsageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AIUsageService {

    private final WorkerService workerService;
    private final AIUsageRepository aiUsageRepository;

    public AIUsageService(WorkerService workerService, AIUsageRepository aiUsageRepository) {
        this.workerService = workerService;
        this.aiUsageRepository = aiUsageRepository;
    }

    //This function either gets the already existent Usage or creates a brand new one.
    public AIUsage getOrCreateAIUsage(int workerId) {
        Worker worker = workerService.getWorkerById(workerId);
        return aiUsageRepository.findByWorker(worker).orElseGet(() -> {
            AIUsage aiUsage = new AIUsage();
            aiUsage.setWorker(worker);
            aiUsage.setImportCount(0);
            aiUsage.setResetDate(LocalDate.now().plusMonths(1));

            return aiUsageRepository.save(aiUsage);
        });
    }

    public boolean canImport(int workerId) {
        AIUsage aiUsage = getOrCreateAIUsage(workerId);
        return aiUsage.getImportCount() < getMaxImports();
    }

    public void incrementImportCount(int workerId) {
        AIUsage aiUsage = getOrCreateAIUsage(workerId);
        aiUsage.setImportCount(aiUsage.getImportCount() + 1);
        aiUsageRepository.save(aiUsage);
    }

    public void resetImportCountIfNeeded(int workerId) {
        AIUsage aiUsage = getOrCreateAIUsage(workerId);

        if (LocalDate.now().isAfter(aiUsage.getResetDate())) {
            aiUsage.setImportCount(0);
            aiUsage.setResetDate(LocalDate.now().plusMonths(1));
            aiUsageRepository.save(aiUsage);
        }

    }

    public int getMaxImports() {
        return 100;
    }
}
