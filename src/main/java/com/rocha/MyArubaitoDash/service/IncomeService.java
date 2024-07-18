package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.dto.IncomeDTO;
import com.rocha.MyArubaitoDash.dto.ShiftDTO;
import com.rocha.MyArubaitoDash.model.Job;
import com.rocha.MyArubaitoDash.model.Shift;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
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
        List<Shift> shifts;
        if (jobId == -1) {
            if (endDate == null) {
                shifts = shiftService.getAllShiftsByWorkerFrom(fromDate, workerId);
            } else {
                shifts = shiftService.getAllShiftsInRangeByWorker(workerId, fromDate, endDate);
            }

        } else {
            shifts = shiftService.getShiftsFrom(fromDate, workerId, jobId);
        }

        if (shifts.isEmpty()) {
            return null;
        }

        // Create a HashMap to store job hourly rates together with jobId to use later
        HashMap<Integer, BigDecimal> jobHourlyRateMap = new HashMap<>();

        float totalHours = 0;
        List<ShiftDTO> shiftDTOS = new ArrayList<>();

        for (Shift shift : shifts) {
            int currentShiftJobId = shift.getJob().getId();

            // Retrieve job hourly rate and store it in the HashMap if not there already
            BigDecimal jobHourlyRate = jobHourlyRateMap.computeIfAbsent(currentShiftJobId, id -> jobService.getJobById(id).getHourlyRate());

            shiftDTOS.add(new ShiftDTO(
                    workerId,
                    currentShiftJobId,
                    shift.getStartDate(),
                    shift.getStartTime(),
                    shift.getEndDate(),
                    shift.getEndTime(),
                    shift.getShiftType(),
                    shift.getId()
            ));

            totalHours += calculateShiftDuration(shift);
        }

        // Calculate gross pay
        BigDecimal grossPay = BigDecimal.ZERO;
        for (Shift shift : shifts) {
            BigDecimal jobHourlyRate = jobHourlyRateMap.get(shift.getJob().getId());
            System.out.println(jobHourlyRate);
            grossPay = grossPay.add(new BigDecimal(calculateShiftDuration(shift)).multiply(jobHourlyRate));
        }

        System.out.println("Total hours: " + totalHours + " times hourly rates = " + grossPay);

        float nextShiftDuration = calculateShiftDuration(shifts.get(0));

        return new IncomeDTO(grossPay, shiftDTOS, shiftDTOS.get(0), nextShiftDuration, new BigDecimal(nextShiftDuration).multiply(jobHourlyRateMap.get(shiftDTOS.get(0).getJobId())), totalHours);
    }



    private float calculateShiftDuration(Shift shift) {
//        float shiftDuration = ChronoUnit.HOURS.between(shift.getStartTime(), shift.getEndTime());

        // Need to calculate shift duration in minutes first and then convert to hours...
        // The code above only calculates whole hours
        long minutesDifference = ChronoUnit.MINUTES.between(shift.getStartTime(), shift.getEndTime());

        // Convert the duration from minutes to hours with fractions
        float shiftDuration = minutesDifference / 60.0f;
        if (shiftDuration >= 5) {
            shiftDuration -= 0.5; // - 30min break
        }


        return shiftDuration;
    }
}
