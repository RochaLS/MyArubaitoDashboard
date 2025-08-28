package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.dto.IncomeDTO;
import com.rocha.MyArubaitoDash.dto.ShiftDTO;
import com.rocha.MyArubaitoDash.model.Job;
import com.rocha.MyArubaitoDash.model.Shift;
import com.rocha.MyArubaitoDash.util.OwnershipVerifier;
import com.rocha.MyArubaitoDash.util.ShiftHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class IncomeService {

    private final ShiftService shiftService;
    private final JobService jobService;
    private final WorkerSettingsService workerSettingsService;
    private final OwnershipVerifier ownershipVerifier;
    private final ShiftHelper shiftHelper;

    private static final Logger logger = LoggerFactory.getLogger(IncomeService.class);

    @Autowired
    public IncomeService(ShiftService shiftService,
                         JobService jobService,
                         WorkerSettingsService workerSettingsService,
                         OwnershipVerifier ownershipVerifier, ShiftHelper shiftHelper) {
        this.shiftService = shiftService;
        this.jobService = jobService;
        this.workerSettingsService = workerSettingsService;
        this.ownershipVerifier = ownershipVerifier;
        this.shiftHelper = shiftHelper;
    }

    public IncomeDTO geIncomeDataFor(LocalDate fromDate, LocalDate endDate, int workerId, int jobId) {
        // 🔐 Check worker ownership
        ownershipVerifier.checkWorkerIdOwnership(workerId);

        if (jobId != -1) {
            Job job = jobService.getJobById(jobId);
            if (job != null) {
                ownershipVerifier.checkJobOwnership(job);
            }
        }

        BigDecimal holidayMultiplier = workerSettingsService.getSettingsByWorkerId(workerId).getPayMultiplier();
        List<Shift> shifts = getShifts(fromDate, endDate, workerId, jobId);

        if (shifts.isEmpty()) {
            return null;
        }

        Map<Integer, Job> jobMap = shiftHelper.getJobMapForShifts(shifts);
//        List<ShiftDTO> shiftDTOs = shiftHelper.createShiftDTOs(shifts, workerId, holidayMultiplier, jobMap);

        BigDecimal grossPay = calculateGrossPay(shifts, holidayMultiplier, jobMap);
        float totalHours = calculateTotalHours(shifts);

        // Get next shift
        Shift nextShift = shiftService.getNextShiftForWorker(workerId);
        ShiftDTO nextShiftDTO = null;
        float nextShiftDuration = 0;
        BigDecimal nextShiftPay = BigDecimal.ZERO;

        if (nextShift != null) {
            ownershipVerifier.checkShiftOwnership(nextShift);
            Job nextJob = jobService.getJobById(nextShift.getJob().getId()); // fetch once
            jobMap.putIfAbsent(nextJob.getId(), nextJob);

            List<ShiftDTO> nextShiftDTOs = shiftHelper.createShiftDTOs(
                    List.of(nextShift), workerId, holidayMultiplier, jobMap
            );
            nextShiftDTO = !nextShiftDTOs.isEmpty() ? nextShiftDTOs.get(0) : null;

            if (nextShiftDTO != null) {
                nextShiftDuration = calculateShiftDuration(nextShift);
                BigDecimal hourlyRate = jobMap.get(nextJob.getId()).getHourlyRate();
                BigDecimal bonusRate = nextShiftDTO.getIsHoliday() ? holidayMultiplier : BigDecimal.ONE;
                nextShiftPay = BigDecimal.valueOf(nextShiftDuration).multiply(hourlyRate.multiply(bonusRate));
            }
        }

        // Count openings, mids, closings
        int openingCount = 0;
        int midCount = 0;
        int closingCount = 0;
        int completedShifts = 0;

        LocalDateTime now = LocalDateTime.now();

        for (Shift shift : shifts) {
            String type = shift.getShiftType();
            if (type == null) continue;

            switch (type.toLowerCase()) {
                case "opening":
                    openingCount++;
                    break;
                case "mid":
                    midCount++;
                    break;
                case "closing":
                    closingCount++;
                    break;
            }

            LocalDateTime shiftEnd = LocalDateTime.of(shift.getEndDate(), shift.getEndTime());
            if (shiftEnd.isBefore(now)) {
                completedShifts++;
            }
        }



        return new IncomeDTO(grossPay, shifts.size(), openingCount, midCount, closingCount, completedShifts, nextShiftDTO, nextShiftDuration, nextShiftPay, totalHours);
    }

    // ———————————————————— Internal Helpers ————————————————————

    private List<Shift> getShifts(LocalDate fromDate, LocalDate endDate, int workerId, int jobId) {
        if (jobId == -1) {
            return fromDate == null
                    ? shiftService.getShiftsByWorkerId(workerId)
                    : (endDate == null
                    ? shiftService.getAllShiftsByWorkerFrom(fromDate, workerId)
                    : shiftService.getAllShiftsInRangeByWorker(workerId, fromDate, endDate));
        } else {
            return shiftService.getShiftsFrom(fromDate, workerId, jobId);
        }
    }

//    public Map<Integer, Job> getJobMapForShifts(List<Shift> shifts) {
//        Set<Integer> jobIds = shifts.stream()
//                .map(shift -> shift.getJob().getId())
//                .collect(Collectors.toSet());
//
//        List<Job> jobs = jobService.getJobsByIds(jobIds); // You need to implement this
//        return jobs.stream().collect(Collectors.toMap(Job::getId, Function.identity()));
//    }

//    public List<ShiftDTO> createShiftDTOs(List<Shift> shifts, int workerId, BigDecimal holidayMultiplier, Map<Integer, Job> jobMap) {
//        List<ShiftDTO> shiftDTOs = new ArrayList<>();
//
//        for (Shift shift : shifts) {
//            BigDecimal duration = BigDecimal.valueOf(calculateShiftDuration(shift));
//            Job job = jobMap.get(shift.getJob().getId());
//            BigDecimal hourlyRate = job.getHourlyRate();
//            BigDecimal multiplier = shift.getIsHoliday() ? holidayMultiplier : BigDecimal.ONE;
//            BigDecimal moneyValue = duration.multiply(hourlyRate).multiply(multiplier);
//
//            shiftDTOs.add(new ShiftDTO(
//                    workerId,
//                    job.getId(),
//                    shift.getStartDate(),
//                    shift.getStartTime(),
//                    shift.getEndDate(),
//                    shift.getEndTime(),
//                    shift.getShiftType(),
//                    shift.getIsHoliday(),
//                    shift.getId(),
//                    moneyValue
//            ));
//        }
//
//        return shiftDTOs;
//    }

    private BigDecimal calculateGrossPay(List<Shift> shifts, BigDecimal holidayMultiplier, Map<Integer, Job> jobMap) {
        BigDecimal grossPay = BigDecimal.ZERO;

        for (Shift shift : shifts) {
            BigDecimal bonusRate = shift.getIsHoliday() ? holidayMultiplier : BigDecimal.ONE;
            BigDecimal jobHourlyRate = jobMap.get(shift.getJob().getId()).getHourlyRate();
            BigDecimal shiftDuration = BigDecimal.valueOf(calculateShiftDuration(shift));
            grossPay = grossPay.add(shiftDuration.multiply(jobHourlyRate).multiply(bonusRate));
        }

        return grossPay;
    }

    private float calculateTotalHours(List<Shift> shifts) {
        return (float) shifts.stream()
                .mapToDouble(this::calculateShiftDuration)
                .sum();
    }

    private float calculateShiftDuration(Shift shift) {
        if (shift == null) return 0;
        long minutes = ChronoUnit.MINUTES.between(shift.getStartTime(), shift.getEndTime());
        float hours = minutes / 60.0f;
        return hours >= 5 ? hours - 0.5f : hours; // 30 min break rule
    }
}