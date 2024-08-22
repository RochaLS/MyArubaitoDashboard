package com.rocha.MyArubaitoDash.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rocha.MyArubaitoDash.dto.ShiftDTO;
import com.rocha.MyArubaitoDash.service.GeminiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
public class GeminiController {
    @Autowired
    private GeminiClient geminiClient;

    @GetMapping("/test-gemini-connection")
    public ResponseEntity<String> testGeminiConnection() {
        String response = geminiClient.testConnection();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/process-image")
    public ResponseEntity<String> processImage(@RequestParam("file") MultipartFile file) {
        try {
            String response = geminiClient.processImage(file);
            System.out.println(response);

            return ResponseEntity.status(200).body(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
