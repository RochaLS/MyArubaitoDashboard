package com.rocha.MyArubaitoDash.controller;

import com.rocha.MyArubaitoDash.dto.ShiftDTO;
import com.rocha.MyArubaitoDash.model.Shift;
import com.rocha.MyArubaitoDash.service.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shift")
public class ShiftController {

    private final ShiftService shiftService;

    @Autowired public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
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
    public ResponseEntity<String> addShift(@RequestBody ShiftDTO shiftDTO) {
        try {
            if (shiftDTO == null) {
                return new ResponseEntity<>("Invalid shift data", HttpStatus.BAD_REQUEST);
            }

            System.out.println(shiftDTO);
            shiftService.createShift(shiftDTO);
            return new ResponseEntity<>("Shift added!", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding shift. " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updatedShift(@PathVariable int id, @RequestBody ShiftDTO shiftDTO) {
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
