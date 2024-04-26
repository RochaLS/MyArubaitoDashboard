package com.rocha.MyArubaitoDash.controller;


import com.rocha.MyArubaitoDash.dto.IncomeDTO;
import com.rocha.MyArubaitoDash.service.IncomeService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/income")
public class IncomeController {

    final private IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GetMapping("/{workerId}/{jobId}/calculate")
    public IncomeDTO getIncomeData(@RequestParam("date") LocalDate date, @PathVariable int workerId, @PathVariable int jobId) {
        return incomeService.geIncomeDataFor(date, workerId, jobId);
    }


}
