package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.controller.IncomeController;
import com.rocha.MyArubaitoDash.dto.IncomeDTO;
import com.rocha.MyArubaitoDash.dto.ShiftDTO;
import com.rocha.MyArubaitoDash.model.Job;
import com.rocha.MyArubaitoDash.model.Shift;
import com.rocha.MyArubaitoDash.util.OwnershipVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class IncomeService {

    private final ShiftService shiftService;
    private final JobService jobService;
    private final WorkerSettingsService workerSettingsService;
    private final OwnershipVerifier ownershipVerifier;

    private static final Logger logger = LoggerFactory.getLogger(IncomeService.class);

    @Autowired
    public IncomeService(ShiftService shiftService,
                         JobService jobService,
                         WorkerSettingsService workerSettingsService,
                         OwnershipVerifier ownershipVerifier) {
        this.shiftService = shiftService;
        this.jobService = jobService;
        this.workerSettingsService = workerSettingsService;
        this.ownershipVerifier = ownershipVerifier;
    }

    public IncomeDTO geIncomeDataFor(LocalDate fromDate, LocalDate endDate, int workerId, int jobId) {
        ownershipVerifier.checkWorkerIdOwnership(workerId);

        if (jobId != -1) {
            Job job = jobService.getJobById(jobId);
            if (job != null) {
                ownershipVerifier.checkJobOwnership(job);
            }
        }

        BigDecimal holidayMultiplier = workerSettingsService.getSettingsByWorkerId(workerId).getPayMultiplier();

        List<Shift> shifts = getShifts(fromDate, endDate, workerId, jobId);
        if (shifts.isEmpty()) return null;

        HashMap<Integer, BigDecimal> jobHourlyRateMap = createJobHourlyRateMap(shifts);

        List<ShiftDTO> shiftDTOs = new ArrayList<>();
        BigDecimal grossPay = BigDecimal.ZERO;
        float totalHours = 0f;

        for (Shift shift : shifts) {
            float hours = calculateShiftDuration(shift);
            int jobIdKey = shift.getJob().getId();
            BigDecimal hourlyRate = jobHourlyRateMap.get(jobIdKey);
            BigDecimal bonus = shift.getIsHoliday() ? holidayMultiplier : BigDecimal.ONE;
            BigDecimal money = BigDecimal.valueOf(hours).multiply(hourlyRate.multiply(bonus));

            shiftDTOs.add(new ShiftDTO(
                    workerId,
                    jobIdKey,
                    shift.getStartDate(),
                    shift.getStartTime(),
                    shift.getEndDate(),
                    shift.getEndTime(),
                    shift.getShiftType(),
                    shift.getIsHoliday(),
                    shift.getId(),
                    money
            ));

            grossPay = grossPay.add(money);
            totalHours += hours;
        }

        Shift nextShift = shiftService.getNextShiftForWorker(workerId);
        ShiftDTO nextShiftDTO = null;
        float nextShiftDuration = 0f;
        BigDecimal nextShiftPay = BigDecimal.ZERO;

        if (nextShift != null) {
            ownershipVerifier.checkShiftOwnership(nextShift);

            int nextJobId = nextShift.getJob().getId();
            BigDecimal hourlyRate = jobHourlyRateMap.getOrDefault(
                    nextJobId,
                    jobService.getJobById(nextJobId).getHourlyRate()
            );
            jobHourlyRateMap.putIfAbsent(nextJobId, hourlyRate);

            BigDecimal bonus = nextShift.getIsHoliday() ? holidayMultiplier : BigDecimal.ONE;
            nextShiftDuration = calculateShiftDuration(nextShift);
            nextShiftPay = BigDecimal.valueOf(nextShiftDuration).multiply(hourlyRate.multiply(bonus));

            nextShiftDTO = new ShiftDTO(
                    workerId,
                    nextJobId,
                    nextShift.getStartDate(),
                    nextShift.getStartTime(),
                    nextShift.getEndDate(),
                    nextShift.getEndTime(),
                    nextShift.getShiftType(),
                    nextShift.getIsHoliday(),
                    nextShift.getId(),
                    nextShiftPay
            );
        }

        return new IncomeDTO(grossPay, shiftDTOs, nextShiftDTO, nextShiftDuration, nextShiftPay, totalHours);
    }

    // ——————————————————————————————— Internal Helpers ———————————————————————————————

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

    private HashMap<Integer, BigDecimal> createJobHourlyRateMap(List<Shift> shifts) {
        Set<Integer> jobIds = shifts.stream()
                .map(shift -> shift.getJob().getId())
                .collect(Collectors.toSet());

        List<Job> jobs = jobService.getJobsByIds(jobIds);

        return jobs.stream()
                .collect(Collectors.toMap(
                        Job::getId,
                        Job::getHourlyRate,
                        (existing, replacement) -> existing,
                        HashMap::new
                ));
    }

    private float calculateShiftDuration(Shift shift) {
        if (shift == null) return 0f;
        long minutes = ChronoUnit.MINUTES.between(shift.getStartTime(), shift.getEndTime());
        float hours = minutes / 60.0f;
        return hours >= 5 ? hours - 0.5f : hours; // apply 30 min break if shift >= 5h
    }
}