package com.rocha.MyArubaitoDash.controller;


import com.rocha.MyArubaitoDash.dto.IncomeDTO;
import com.rocha.MyArubaitoDash.service.IncomeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

//@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/income")
public class IncomeController {

    final private IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GetMapping("/{workerId}/{jobId}/calculate")
    public ResponseEntity<?> getIncomeData(@RequestParam("date") LocalDate date, @PathVariable int workerId, @PathVariable int jobId) {
        IncomeDTO incomeData = incomeService.geIncomeDataFor(date, null, workerId, jobId);
        if (incomeData == null) {
            return new ResponseEntity<>("Data not found.", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(incomeData);
    }

    @GetMapping("/{workerId}/calculate")
    public ResponseEntity<?> getAllIncomeData(@RequestParam("date") LocalDate date, @PathVariable int workerId) {
        IncomeDTO incomeData = incomeService.geIncomeDataFor(date, null, workerId, -1);
        if (incomeData == null) {

            System.out.println("Data not found");
            return new ResponseEntity<>("Data not found.", HttpStatus.NOT_FOUND);
        }

        System.out.println("Data sent to client: " + incomeData);

        return ResponseEntity.ok(incomeData);
    }

    @GetMapping("/{workerId}/calculate-by-range")
    public ResponseEntity<?> getAllIncomeDataFromRange(@RequestParam("start-date") LocalDate startDate, @RequestParam("end-date") LocalDate endDate, @PathVariable int workerId) {
        IncomeDTO incomeData = incomeService.geIncomeDataFor(startDate, endDate, workerId, -1);
        System.out.println("Searching for shifts from " + startDate + " to " + endDate);
        if (incomeData == null) {
            return new ResponseEntity<>("Data not found.", HttpStatus.NOT_FOUND);
        }

        System.out.println("Data sent to client: " + incomeData);

        return ResponseEntity.ok(incomeData);
    }




}
