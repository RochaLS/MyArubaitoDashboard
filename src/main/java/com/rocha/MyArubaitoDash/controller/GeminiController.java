package com.rocha.MyArubaitoDash.controller;


import com.rocha.MyArubaitoDash.service.AIUsageService;
import com.rocha.MyArubaitoDash.service.GeminiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class GeminiController {

    private static final Logger logger = LoggerFactory.getLogger(GeminiController.class);

    @Autowired
    private GeminiClient geminiClient;

    private final AIUsageService aiUsageService;

    public GeminiController(AIUsageService aiUsageService) {
        this.aiUsageService = aiUsageService;
    }

    @GetMapping("/test-gemini-connection")
    public ResponseEntity<String> testGeminiConnection() {
        logger.info("Received request to test Gemini connection.");
        String response = geminiClient.testConnection();
        logger.info("Gemini connection test response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{workerId}/process-image")
    public ResponseEntity<?> processImage(@RequestParam("file") MultipartFile file, @PathVariable int workerId) {
        logger.info("Processing image for workerId: {}", workerId);

        // Reset import count if needed
        logger.debug("Resetting import count for workerId: {}", workerId);
        aiUsageService.resetImportCountIfNeeded(workerId);

        // Check if the worker can import
        if (!aiUsageService.canImport(workerId)) {
            logger.warn("WorkerId: {} has exceeded their import limit.", workerId);
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("resetDate", aiUsageService.getOrCreateAIUsage(workerId).getResetDate());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(responseBody);
        }

        try {
            logger.info("Sending image to GeminiClient for processing.");
            String response = geminiClient.processImage(file);

            if (response != null && !response.contains("error") && !response.isEmpty()) {
                logger.info("Image processed successfully for workerId: {}", workerId);
                aiUsageService.incrementImportCount(workerId);
                return ResponseEntity.status(200).body(response);
            }

            logger.error("Failed to process image: Invalid response for workerId: {}", workerId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to process image: Invalid response.");

        } catch (IOException e) {
            logger.error("Error while processing image for workerId: {}", workerId, e);
            throw new RuntimeException("Error processing image", e);
        }
    }
}
