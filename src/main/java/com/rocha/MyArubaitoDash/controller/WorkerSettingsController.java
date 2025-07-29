package com.rocha.MyArubaitoDash.controller;

import com.rocha.MyArubaitoDash.dto.WorkerSettingsDTO;
import com.rocha.MyArubaitoDash.model.UpdatePayMultiplierRequest;
import com.rocha.MyArubaitoDash.model.WorkerSettings;
import com.rocha.MyArubaitoDash.service.WorkerSettingsService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/worker-settings")
public class WorkerSettingsController {

    private final WorkerSettingsService workerSettingsService;

    private static final Logger logger = LoggerFactory.getLogger(WorkerSettingsController.class);

    public WorkerSettingsController(WorkerSettingsService workerSettingsService) {
        this.workerSettingsService = workerSettingsService;
    }


    @GetMapping("byWorker/{id}")
    public ResponseEntity<?> getSettingsByWorkerId(@PathVariable int id) {

        logger.info("Fetching worker settings for worker: {}", id);
        WorkerSettings workerSettings = workerSettingsService.getSettingsByWorkerId(id);

        logger.info("Worker settings: {}", workerSettings);

        WorkerSettingsDTO workerSettingsDTO = new WorkerSettingsDTO(workerSettings.getId(), workerSettings.getPayMultiplier(), workerSettings.getWorker().getId());
        return ResponseEntity.ok(workerSettingsDTO);

    }

    @PutMapping("/updatePayMultiplier")
    public ResponseEntity<?> updatePayMultiplier(@Valid @RequestBody UpdatePayMultiplierRequest request) {
        int workerId = request.getWorkerId();
        BigDecimal newMultiplier = request.getPayMultiplier();

        logger.info("Updating pay multiplier for worker: {}, new multiplier: {}", workerId, newMultiplier);

        try {
            WorkerSettings updatedSettings = workerSettingsService.updateSettings(workerId, newMultiplier);

            WorkerSettingsDTO responseDTO = new WorkerSettingsDTO(
                    updatedSettings.getId(),
                    updatedSettings.getPayMultiplier(),
                    updatedSettings.getWorker().getId()
            );

            return ResponseEntity.ok(responseDTO);
        } catch (IllegalArgumentException e) {
            logger.error("Error updating pay multiplier: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
