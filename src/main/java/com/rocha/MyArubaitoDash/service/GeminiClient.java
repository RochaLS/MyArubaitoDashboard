package com.rocha.MyArubaitoDash.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class GeminiClient {

    private final RestTemplate restTemplate;
    private final String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    public GeminiClient() {
        this.restTemplate = new RestTemplate();
    }

    public String testConnection() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the request body with a simple text prompt
        String requestBody = "{" +
                "\"contents\": [{" +
                "\"parts\": [" +
                "{\"text\": \"What is the capital of Brazil?\"}" +
                "]" +
                "}]" +
                "}";

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        String urlWithKey = geminiApiUrl + "?key=" + apiKey;

        ResponseEntity<String> response = restTemplate.postForEntity(urlWithKey, requestEntity, String.class);

        return response.getBody();
    }

    public String processImage(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Read the image file and encode it in Base64
        byte[] imageBytes = file.getBytes();
        String encodedImage = Base64.getEncoder().encodeToString(imageBytes);

        // Create the request body
        String requestBody = String.format(
                "{" +
                        "\"contents\": [{" +
                        "\"parts\": [" +
                        "{\"text\": \"Please extract the date, start time, and end time for each shift from the provided work schedule screenshot. Format the output into a JSON object as follows: \\n\\n" +
                        "- Use military time format (e.g., 22:00) for start and end times.\\n" +
                        "- Exclude the year; provide only the date, start time, end time, and month for each shift.\\n" +
                        "- If the image is not a schedule or calendar, return 0.\\n\\n" +
                        "The JSON structure should be: \\n\\n" +
                        "{\\n" +
                        "  \\\"shifts\\\": [\\n" +
                        "    {\\n" +
                        "      \\\"date\\\": \\\"DD\\\",\\n" +
                        "      \\\"start_time\\\": \\\"HHMM\\\",\\n" +
                        "      \\\"end_time\\\": \\\"HHMM\\\"\\n" +
                        "      \\\"month\\\": M\\n" +
                        "    },\\n" +
                        "    ...\\n" +
                        "  ]\\n" +
                        "}\\n\\n" +
                        "Ensure that the JSON object contains only the list of shifts with their date, start time, end time and month.\"}," +
                        "{\"inline_data\": {" +
                        "\"mime_type\": \"image/png\"," +
                        "\"data\": \"%s\"" +
                        "}}" +
                        "]" +
                        "}]" +
                        "}",
                encodedImage
        );

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        String urlWithKey = geminiApiUrl + "?key=" + apiKey;

        ResponseEntity<String> response = restTemplate.postForEntity(urlWithKey, requestEntity, String.class);

        return response.getBody();
    }
}
