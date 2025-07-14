package com.rocha.MyArubaitoDash.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafeLocalTimeDeserializer extends JsonDeserializer<LocalTime> {

    private static final Logger logger = LoggerFactory.getLogger(SafeLocalTimeDeserializer.class);
    private static final DateTimeFormatter[] formatters = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("H:mm:ss"),
            DateTimeFormatter.ofPattern("HHmm"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("Hmm"),
            DateTimeFormatter.ofPattern("H:mm")
    };

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String raw = p.getText().trim();

        // Remove suspicious characters
        String cleaned = raw.replaceAll("[^0-9:]", "");

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalTime.parse(cleaned, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        logger.warn("⛔️ Failed to parse LocalTime from: '{}'. Cleaned: '{}'", raw, cleaned);
        return null; // Optional: throw custom exception instead
    }
}