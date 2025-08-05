package com.rocha.MyArubaitoDash.controller;


import com.rocha.MyArubaitoDash.dto.IncomeDTO;
import com.rocha.MyArubaitoDash.service.IncomeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/income")
public class IncomeController {

    private static final Logger logger = LoggerFactory.getLogger(IncomeController.class);
    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GetMapping("/{workerId}/{jobId}/calculate")
    public ResponseEntity<?> getIncomeData(@RequestParam("date") LocalDate date, @PathVariable int workerId, @PathVariable int jobId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        logger.info("Request to calculate income for workerId: {}, jobId: {}, date: {}", workerId, jobId, date);

        IncomeDTO incomeData = incomeService.geIncomeDataFor(date, null, workerId, jobId);

        stopWatch.stop();
        logger.info("Execution time for /calculate endpoint: {} ms", stopWatch.getTotalTimeMillis());

        if (incomeData == null) {
            logger.warn("Income data not found for workerId: {}, jobId: {}, date: {}", workerId, jobId, date);
            return new ResponseEntity<>("Data not found.", HttpStatus.NOT_FOUND);
        }

        logger.info("Income data retrieved successfully for workerId: {}, jobId: {}, date: {}", workerId, jobId, date);
        return ResponseEntity.ok(incomeData);
    }

    @GetMapping("/{workerId}/calculate")
    public ResponseEntity<?> getAllIncomeData(@RequestParam("date") LocalDate date, @PathVariable int workerId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        logger.info("Request to calculate all income data for workerId: {}, date: {}", workerId, date);

        IncomeDTO incomeData = incomeService.geIncomeDataFor(date, null, workerId, -1);

        stopWatch.stop();
        logger.info("Execution time for /calculate (no jobId) endpoint: {} ms", stopWatch.getTotalTimeMillis());

        if (incomeData == null) {
            logger.warn("Income data not found for workerId: {}, date: {}", workerId, date);
            return new ResponseEntity<>("Data not found.", HttpStatus.NOT_FOUND);
        }

        logger.info("Income data sent to client for workerId: {}, date: {}", workerId, date);
        return ResponseEntity.ok(incomeData);
    }

    @GetMapping("/{workerId}/calculate-by-range")
    public ResponseEntity<?> getAllIncomeDataFromRange(@RequestParam("start-date") LocalDate startDate, @RequestParam("end-date") LocalDate endDate, @PathVariable int workerId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        logger.info("Request to calculate income data for workerId: {} from start-date: {} to end-date: {}", workerId, startDate, endDate);

        IncomeDTO incomeData = incomeService.geIncomeDataFor(startDate, endDate, workerId, -1);

        stopWatch.stop();
        logger.info("Execution time for /calculate-by-range endpoint: {} ms", stopWatch.getTotalTimeMillis());

        if (incomeData == null) {
            logger.warn("Income data not found for workerId: {} in the date range: {} to {}", workerId, startDate, endDate);
            return new ResponseEntity<>("Data not found.", HttpStatus.NOT_FOUND);
        }

        logger.info("Income data sent to client for workerId: {}, from start-date: {} to end-date: {}", workerId, startDate, endDate);
        return ResponseEntity.ok(incomeData);
    }

    @GetMapping("/{workerId}/calculate-all")
    public ResponseEntity<?> getAllIncomeDataFromAllTime(@PathVariable int workerId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        logger.info("Request to calculate income data for workerId: {}", workerId);

        IncomeDTO incomeData = incomeService.geIncomeDataFor(null, null, workerId, -1);

        stopWatch.stop();
        logger.info("Execution time for /calculate-all endpoint: {} ms", stopWatch.getTotalTimeMillis());

        if (incomeData == null) {
            logger.warn("Income data not found for workerId: {}", workerId);
            return new ResponseEntity<>("Data not found.", HttpStatus.NOT_FOUND);
        }

        logger.info("Income data sent to client for workerId: {}", workerId);
        return ResponseEntity.ok(incomeData);
    }
}