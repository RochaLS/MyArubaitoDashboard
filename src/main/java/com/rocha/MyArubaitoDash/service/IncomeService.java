package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.dto.IncomeDTO;
import com.rocha.MyArubaitoDash.dto.ShiftDTO;
import com.rocha.MyArubaitoDash.model.Job;
import com.rocha.MyArubaitoDash.model.Shift;
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

    final private ShiftService shiftService;
    final private JobService jobService;

    public IncomeService(ShiftService shiftService, JobService jobService) {
        this.shiftService = shiftService;
        this.jobService = jobService;
    }

    public IncomeDTO geIncomeDataFor(LocalDate fromDate, LocalDate endDate, int workerId, int jobId) {
        List<Shift> shifts = getShifts(fromDate, endDate, workerId, jobId);
        if (shifts.isEmpty()) {
            return null;
        }

        HashMap<Integer, BigDecimal> jobHourlyRateMap = createJobHourlyRateMap(shifts);
        List<ShiftDTO> shiftDTOs = createShiftDTOs(shifts, workerId);
        BigDecimal grossPay = calculateGrossPay(shifts, jobHourlyRateMap);

        ShiftDTO nextShiftDTO = getNextShiftDTO(shiftDTOs);
        float nextShiftDuration = calculateShiftDuration(getNextShift(shifts));

        // Calculate bonus such as holiday pay.
        BigDecimal bonusRate = nextShiftDTO.getIsHoliday() ? BigDecimal.valueOf(1.5) : BigDecimal.ONE;


        return new IncomeDTO(grossPay, shiftDTOs, nextShiftDTO, nextShiftDuration,
                new BigDecimal(nextShiftDuration).multiply(jobHourlyRateMap.get(shiftDTOs.get(0).getJobId()).multiply(bonusRate)),
                calculateTotalHours(shifts));
    }

    private List<Shift> getShifts(LocalDate fromDate, LocalDate endDate, int workerId, int jobId) {
        if (jobId == -1) {
            return endDate == null
                    ? shiftService.getAllShiftsByWorkerFrom(fromDate, workerId)
                    : shiftService.getAllShiftsInRangeByWorker(workerId, fromDate, endDate);
        } else {
            return shiftService.getShiftsFrom(fromDate, workerId, jobId);
        }
    }

    private HashMap<Integer, BigDecimal> createJobHourlyRateMap(List<Shift> shifts) {
        HashMap<Integer, BigDecimal> jobHourlyRateMap = new HashMap<>();
        for (Shift shift : shifts) {
            jobHourlyRateMap.computeIfAbsent(shift.getJob().getId(), id -> jobService.getJobById(id).getHourlyRate());
        }
        return jobHourlyRateMap;
    }

    private List<ShiftDTO> createShiftDTOs(List<Shift> shifts, int workerId) {
        List<ShiftDTO> shiftDTOs = new ArrayList<>();
        for (Shift shift : shifts) {
            shiftDTOs.add(new ShiftDTO(
                    workerId,
                    shift.getJob().getId(),
                    shift.getStartDate(),
                    shift.getStartTime(),
                    shift.getEndDate(),
                    shift.getEndTime(),
                    shift.getShiftType(),
                    shift.getIsHoliday(),
                    shift.getId()
            ));
        }
        return shiftDTOs;
    }

    private BigDecimal calculateGrossPay(List<Shift> shifts, HashMap<Integer, BigDecimal> jobHourlyRateMap) {
        BigDecimal grossPay = BigDecimal.ZERO;
        for (Shift shift : shifts) {
            BigDecimal bonusRate = shift.getIsHoliday() ? BigDecimal.valueOf(1.5) : BigDecimal.ONE;
            BigDecimal jobHourlyRate = jobHourlyRateMap.get(shift.getJob().getId());
            grossPay = grossPay.add(new BigDecimal(calculateShiftDuration(shift)).multiply(jobHourlyRate.multiply(bonusRate)));
        }
        return grossPay;
    }

    private float calculateTotalHours(List<Shift> shifts) {
        return (float) shifts.stream()
                .mapToDouble(this::calculateShiftDuration)
                .sum();
    }

    private Shift getNextShift(List<Shift> shifts) {
        return shifts.stream()
                .filter(shift -> LocalDateTime.of(shift.getStartDate(), shift.getStartTime()).isAfter(LocalDateTime.now()))
                .findFirst()
                .orElse(null);
    }

    private ShiftDTO getNextShiftDTO(List<ShiftDTO> shiftDTOs) {
        return shiftDTOs.stream()
                .filter(shift -> shift.getStartDate().isAfter(ChronoLocalDate.from(LocalDateTime.now())))
                .findFirst()
                .orElse(null);
    }

    private float calculateShiftDuration(Shift shift) {
        if (shift == null) {
            return 0;
        }
        long minutesDifference = ChronoUnit.MINUTES.between(shift.getStartTime(), shift.getEndTime());
        float shiftDuration = minutesDifference / 60.0f;
        return shiftDuration >= 5 ? shiftDuration - 0.5f : shiftDuration; // - 30min break
    }
}

