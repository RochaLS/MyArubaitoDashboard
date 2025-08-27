package com.rocha.MyArubaitoDash.util;

import com.rocha.MyArubaitoDash.dto.ShiftDTO;
import com.rocha.MyArubaitoDash.model.Job;
import com.rocha.MyArubaitoDash.model.Shift;
import com.rocha.MyArubaitoDash.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ShiftHelper {

    private final JobService jobService;

    @Autowired
    public ShiftHelper(JobService jobService) {
        this.jobService = jobService;
    }

    // ————— Create shift DTOs —————
    public List<ShiftDTO> createShiftDTOs(List<Shift> shifts, int workerId, BigDecimal holidayMultiplier, Map<Integer, Job> jobMap) {
        List<ShiftDTO> shiftDTOs = new ArrayList<>();
        for (Shift shift : shifts) {
            BigDecimal duration = BigDecimal.valueOf(calculateShiftDuration(shift));
            Job job = jobMap.get(shift.getJob().getId());
            BigDecimal hourlyRate = job.getHourlyRate();
            BigDecimal multiplier = shift.getIsHoliday() ? holidayMultiplier : BigDecimal.ONE;
            BigDecimal moneyValue = duration.multiply(hourlyRate).multiply(multiplier);

            shiftDTOs.add(new ShiftDTO(
                    workerId,
                    job.getId(),
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

    // ————— Create job map from shifts —————
    public Map<Integer, Job> getJobMapForShifts(List<Shift> shifts) {
        Set<Integer> jobIds = shifts.stream()
                .map(shift -> shift.getJob().getId())
                .collect(Collectors.toSet());

        List<Job> jobs = jobService.getJobsByIds(jobIds);
        return jobs.stream().collect(Collectors.toMap(Job::getId, Function.identity()));
    }

    // ————— Helper —————
    public float calculateShiftDuration(Shift shift) {
        long minutes = ChronoUnit.MINUTES.between(shift.getStartTime(), shift.getEndTime());
        float hours = minutes / 60.0f;
        return hours >= 5 ? hours - 0.5f : hours; // 30 min break rule
    }
}