package com.rocha.MyArubaitoDash.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rocha.MyArubaitoDash.dto.ShiftDTO;
import com.rocha.MyArubaitoDash.service.AIUsageService;
import com.rocha.MyArubaitoDash.service.GeminiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

@RestController
public class GeminiController {
    @Autowired
    private GeminiClient geminiClient;

    private final AIUsageService aiUsageService;

    public GeminiController(AIUsageService aiUsageService) {
        this.aiUsageService = aiUsageService;
    }

    @GetMapping("/test-gemini-connection")
    public ResponseEntity<String> testGeminiConnection() {
        String response = geminiClient.testConnection();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{workerId}/process-image")
    public ResponseEntity<?> processImage(@RequestParam("file") MultipartFile file, @PathVariable int workerId) {

        aiUsageService.resetImportCountIfNeeded(workerId);

        if (!aiUsageService.canImport(workerId)) {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("resetDate", aiUsageService.getOrCreateAIUsage(workerId).getResetDate());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(responseBody);
        }

        try {
            String response = geminiClient.processImage(file);

            if (response != null && !response.contains("error") && !response.isEmpty()) {
                aiUsageService.incrementImportCount(workerId);
                return ResponseEntity.status(200).body(response);
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to process image: Invalid response.");


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
