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

@RestController
@RequestMapping("/api/shift")
public class ShiftController {

    private final ShiftService shiftService;

    @Autowired public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping("byWorker/{id}")
    public ResponseEntity<?> getShiftByWorkerId(@PathVariable int id) {
        List<Shift> shifts = shiftService.getShiftsByWorkerId(id);

        if (shifts.isEmpty()) {
            return new ResponseEntity<>("Shift not found", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(shifts);
    }

    @PostMapping("checkDuplicateShiftsByJob/{id}")
    public ResponseEntity<?> getShiftsByJobId(@PathVariable int id, @RequestBody ArrayList<ShiftDTO> shiftDTOs) {
        List<Shift> shiftsInDB = shiftService.getShiftsByJobId(id);
        Set<LocalDate> startDateSetInDB = shiftsInDB.stream()
                .map(Shift::getStartDate)
                .collect(Collectors.toSet());


        // Find unique shifts by filtering out those with startDate already present in startDateSetInDB
        List<ShiftDTO> uniqueShifts = shiftDTOs.stream()
                .filter(importedShift -> {
                    LocalDate startDate = importedShift.getStartDate();
                    System.out.println("Checking date: " + startDate);
                    return !startDateSetInDB.contains(startDate);
                }).sorted(Comparator.comparing(ShiftDTO::getStartDate)).collect(Collectors.toList());

        return ResponseEntity.ok(uniqueShifts);

    }

    @GetMapping("byWorker-paginated/{id}")
    public ResponseEntity<?> getShiftByWorkerFromDate(@RequestParam("date") LocalDate date, @PathVariable int id, @RequestParam int page, @RequestParam int size) {
        Page<Shift> shifts = shiftService.getAllShiftsByWorkerFromPaginated(date, id, page, size);

        if (shifts.isEmpty()) {
            return new ResponseEntity<>("Shift not found", HttpStatus.NOT_FOUND);
        }

        System.out.println("Paginated shifts: " + shifts.get());

        return ResponseEntity.ok(shifts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getShiftById(@PathVariable int id) {
        Shift shift = shiftService.getShiftById(id);

        if (shift != null) {
            return ResponseEntity.ok(shift);
        }

        return new ResponseEntity<>("Shift not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addShift(@RequestBody @Valid ShiftDTO shiftDTO) {
        try {
            if (shiftDTO == null) {
                return new ResponseEntity<>("Invalid shift data", HttpStatus.BAD_REQUEST);
            }

            System.out.println(shiftDTO);
            shiftService.createShift(shiftDTO);
            return new ResponseEntity<>("Shift added!", HttpStatus.CREATED);
        } catch (Exception e) {
            if (e instanceof EntityNotFoundException) {
                return new ResponseEntity<>("Error adding shift. " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>("Error adding shift. " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/add-multiple")
    public ResponseEntity<?> addMultipleShifts(@RequestBody @Valid ArrayList<ShiftDTO> shiftDTOs) {
        try {
            if (shiftDTOs.isEmpty()) {
                return new ResponseEntity<>("Empty shift data", HttpStatus.BAD_REQUEST);
            }

            //Save each shift
            for (ShiftDTO shiftDTO : shiftDTOs) {
                shiftService.createShift(shiftDTO);
            }

            return new ResponseEntity<>("Imported shifts successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding shifts. " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updatedShift(@PathVariable int id, @RequestBody @Valid ShiftDTO shiftDTO) {
        try {
            shiftService.updateShift(id, shiftDTO);
            return new ResponseEntity<>("Shift Updated!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding shift. " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
        public ResponseEntity<String> deleteShift(@PathVariable int id) {
        try {
            shiftService.getShiftById(id);
            shiftService.deleteShift(id);

            return new ResponseEntity<>("Shift deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting shift: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
