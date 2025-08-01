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

        HashMap<Integer, BigDecimal> jobHourlyRateMap = createJobHourlyRateMap(shifts);
        List<ShiftDTO> shiftDTOs = createShiftDTOs(shifts, workerId);
        logger.info(shiftDTOs.toString());

        BigDecimal grossPay = calculateGrossPay(shifts, jobHourlyRateMap, holidayMultiplier);

        // Get next shift
        Shift nextShift = shiftService.getNextShiftForWorker(workerId);
        ShiftDTO nextShiftDTO = null;
        float nextShiftDuration = 0;
        BigDecimal nextShiftPay = BigDecimal.ZERO;

        if (nextShift != null) {
            ownershipVerifier.checkShiftOwnership(nextShift); // 🔐 Check shift belongs to current user

            List<Shift> nextShiftList = List.of(nextShift);
            List<ShiftDTO> nextShiftDTOs = createShiftDTOs(nextShiftList, workerId);
            nextShiftDTO = !nextShiftDTOs.isEmpty() ? nextShiftDTOs.get(0) : null;

            if (nextShiftDTO != null) {
                nextShiftDuration = calculateShiftDuration(nextShift);

                int nextJobId = nextShift.getJob().getId();
                BigDecimal hourlyRate = jobHourlyRateMap.containsKey(nextJobId)
                        ? jobHourlyRateMap.get(nextJobId)
                        : jobService.getJobById(nextJobId).getHourlyRate();

                jobHourlyRateMap.putIfAbsent(nextJobId, hourlyRate);

                BigDecimal bonusRate = nextShiftDTO.getIsHoliday() ? holidayMultiplier : BigDecimal.ONE;
                nextShiftPay = new BigDecimal(nextShiftDuration).multiply(hourlyRate.multiply(bonusRate));
            }
        }

        return new IncomeDTO(grossPay, shiftDTOs, nextShiftDTO, nextShiftDuration,
                nextShiftPay, calculateTotalHours(shifts));
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
        HashMap<Integer, BigDecimal> jobHourlyRateMap = new HashMap<>();
        for (Shift shift : shifts) {
            jobHourlyRateMap.computeIfAbsent(
                    shift.getJob().getId(),
                    id -> jobService.getJobById(id).getHourlyRate()
            );
        }
        return jobHourlyRateMap;
    }

    private List<ShiftDTO> createShiftDTOs(List<Shift> shifts, int workerId) {
        List<ShiftDTO> shiftDTOs = new ArrayList<>();
        BigDecimal holidayMultiplier = workerSettingsService.getSettingsByWorkerId(workerId).getPayMultiplier();

        for (Shift shift : shifts) {
            BigDecimal duration = BigDecimal.valueOf(calculateShiftDuration(shift));
            BigDecimal hourlyRate = jobService.getJobById(shift.getJob().getId()).getHourlyRate();
            BigDecimal multiplier = shift.getIsHoliday() ? holidayMultiplier : BigDecimal.ONE;
            BigDecimal moneyValue = duration.multiply(hourlyRate).multiply(multiplier);

            shiftDTOs.add(new ShiftDTO(
                    workerId,
                    shift.getJob().getId(),
                    shift.getStartDate(),
                    shift.getStartTime(),
                    shift.getEndDate(),
                    shift.getEndTime(),
                    shift.getShiftType(),
                    shift.getIsHoliday(),
                    shift.getId(),
                    moneyValue
            ));
        }

        return shiftDTOs;
    }

    private BigDecimal calculateGrossPay(List<Shift> shifts, HashMap<Integer, BigDecimal> jobHourlyRateMap, BigDecimal holidayMultiplier) {
        BigDecimal grossPay = BigDecimal.ZERO;
        for (Shift shift : shifts) {
            BigDecimal bonusRate = shift.getIsHoliday() ? holidayMultiplier : BigDecimal.ONE;
            BigDecimal jobHourlyRate = jobHourlyRateMap.get(shift.getJob().getId());
            grossPay = grossPay.add(
                    new BigDecimal(calculateShiftDuration(shift)).multiply(jobHourlyRate.multiply(bonusRate))
            );
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
        return hours >= 5 ? hours - 0.5f : hours; //30 min break
    }
}
