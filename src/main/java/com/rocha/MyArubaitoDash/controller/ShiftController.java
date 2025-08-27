package com.rocha.MyArubaitoDash.controller;

import com.rocha.MyArubaitoDash.dto.ShiftDTO;
import com.rocha.MyArubaitoDash.model.Shift;
import com.rocha.MyArubaitoDash.service.ShiftService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/shift")
public class ShiftController {

    private static final Logger logger = LoggerFactory.getLogger(ShiftController.class);
    private final ShiftService shiftService;

    @Autowired
    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping("byWorker/{id}")
    public ResponseEntity<?> getShiftByWorkerId(@PathVariable int id) {
        logger.info("Fetching shifts for worker with ID: {}", id);
        List<Shift> shifts = shiftService.getShiftsByWorkerId(id);

        if (shifts.isEmpty()) {
            logger.warn("No shifts found for worker ID: {}", id);
            return new ResponseEntity<>("Shift not found", HttpStatus.NOT_FOUND);
        }

        logger.info("Found {} shifts for worker ID: {}", shifts.size(), id);
        return ResponseEntity.ok(shifts);
    }

    @PostMapping("checkDuplicateShiftsByJob/{id}")
    public ResponseEntity<?> getShiftsByJobId(@PathVariable int id, @RequestBody ArrayList<ShiftDTO> shiftDTOs) {
        logger.info("Checking for duplicate shifts for job ID: {}", id);
        List<Shift> shiftsInDB = shiftService.getShiftsByJobId(id);
        Set<LocalDate> startDateSetInDB = shiftsInDB.stream()
                .map(Shift::getStartDate)
                .collect(Collectors.toSet());

        List<ShiftDTO> uniqueShifts = shiftDTOs.stream()
                .filter(importedShift -> {
                    LocalDate startDate = importedShift.getStartDate();
                    logger.debug("Checking date: {}", startDate);
                    return !startDateSetInDB.contains(startDate);
                }).sorted(Comparator.comparing(ShiftDTO::getStartDate)).collect(Collectors.toList());

        logger.info("Found {} unique shifts for job ID: {}", uniqueShifts.size(), id);
        return ResponseEntity.ok(uniqueShifts);
    }

    @GetMapping("byWorker-paginated/{id}")
    public ResponseEntity<?> getShiftByWorkerFromDate(
            @RequestParam("date") LocalDate date,
            @PathVariable int id,
            @RequestParam int page,
            @RequestParam int size) {
        logger.info("Fetching paginated shifts for worker ID: {} from date: {}", id, date);
        Page<Shift> shifts = shiftService.getAllShiftsByWorkerFromPaginated(date, id, page, size);

        if (shifts.isEmpty()) {
            logger.warn("No paginated shifts found for worker ID: {} from date: {}", id, date);
            return new ResponseEntity<>("Shift not found", HttpStatus.NOT_FOUND);
        }

        logger.info("Returning {} shifts for worker ID: {} on page {}", shifts.getSize(), id, page);
        return ResponseEntity.ok(shifts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getShiftById(@PathVariable int id) {
        logger.info("Fetching shift with ID: {}", id);
        Shift shift = shiftService.getShiftById(id);

        if (shift != null) {
            logger.info("Shift found with ID: {}", id);
            return ResponseEntity.ok(shift);
        }

        logger.warn("No shift found with ID: {}", id);
        return new ResponseEntity<>("Shift not found", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/range")
    public ResponseEntity<?> getShiftsInRange(
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate endDate,
            @RequestParam int workerId) {

        logger.info("Fetching shifts for worker ID: {} from {} to {}", workerId, fromDate, endDate);

        try {
            List<ShiftDTO> shifts = shiftService.getShiftsFromRange(fromDate, endDate, workerId);

            if (shifts != null && !shifts.isEmpty()) {
                logger.info("Found {} shifts for worker ID: {} in date range", shifts.size(), workerId);
                return ResponseEntity.ok(shifts);
            }

            logger.warn("No shifts found for worker ID: {} in date range {} to {}", workerId, fromDate, endDate);
            return new ResponseEntity<>("No shifts found in the specified range", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error fetching shifts for worker ID: {} in date range: {}", workerId, e.getMessage());
            return new ResponseEntity<>("Error retrieving shifts", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> addShift(@RequestBody @Valid ShiftDTO shiftDTO) {
        logger.info("Adding a new shift: {}", shiftDTO);
        try {
            shiftService.createShift(shiftDTO);
            logger.info("Shift added successfully");
            return new ResponseEntity<>("Shift added!", HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error adding shift: {}", e.getMessage(), e);
            if (e instanceof EntityNotFoundException) {
                return new ResponseEntity<>("Error adding shift. " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>("Error adding shift. " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/add-multiple")
    public ResponseEntity<?> addMultipleShifts(@RequestBody @Valid ArrayList<ShiftDTO> shiftDTOs) {
        logger.info("Adding multiple shifts: {} shifts received", shiftDTOs.size());
        try {
            for (ShiftDTO shiftDTO : shiftDTOs) {
                shiftService.createShift(shiftDTO);
                logger.debug("Added shift: {}", shiftDTO);
            }
            logger.info("All shifts added successfully");
            return new ResponseEntity<>("Imported shifts successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error adding multiple shifts: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error adding shifts. " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updatedShift(@PathVariable int id, @RequestBody @Valid ShiftDTO shiftDTO) {
        logger.info("Updating shift with ID: {}", id);
        try {
            shiftService.updateShift(id, shiftDTO);
            logger.info("Shift updated successfully for ID: {}", id);
            return new ResponseEntity<>("Shift Updated!", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating shift with ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("Error updating shift. " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteShift(@PathVariable int id) {
        logger.info("Deleting shift with ID: {}", id);
        try {
            shiftService.getShiftById(id);
            shiftService.deleteShift(id);
            logger.info("Shift deleted successfully with ID: {}", id);
            return new ResponseEntity<>("Shift deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting shift with ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("Error deleting shift: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}