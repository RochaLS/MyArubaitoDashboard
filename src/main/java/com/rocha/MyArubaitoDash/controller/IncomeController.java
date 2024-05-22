package com.rocha.MyArubaitoDash.controller;


import com.rocha.MyArubaitoDash.dto.IncomeDTO;
import com.rocha.MyArubaitoDash.service.IncomeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/income")
public class IncomeController {

    final private IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GetMapping("/{workerId}/{jobId}/calculate")
    public ResponseEntity<?> getIncomeData(@RequestParam("date") LocalDate date, @PathVariable int workerId, @PathVariable int jobId) {
        IncomeDTO incomeData = incomeService.geIncomeDataFor(date, workerId, jobId);
        if (incomeData == null) {
            return new ResponseEntity<>("Data not found.", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(incomeData);
    }


}
