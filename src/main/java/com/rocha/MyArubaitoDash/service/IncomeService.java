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
import java.util.List;

@Service
public class IncomeService {

    final private ShiftService shiftService;
    final private JobService jobService;

    public IncomeService(ShiftService shiftService, JobService jobService) {
        this.shiftService = shiftService;
        this.jobService = jobService;
    }

    public IncomeDTO geIncomeDataFor(LocalDate fromDate, int workerId, int jobId) {
       List<Shift> shifts = shiftService.getShiftsFrom(fromDate,workerId, jobId);

       if (shifts.size() == 0) {
           return null;
       }
       Job job = jobService.getJobById(jobId);
       BigDecimal jobHourlyRate = job.getHourlyRate();

       float totalHours = 0;
       BigDecimal grossPay;

       List<ShiftDTO> shiftDTOS = new ArrayList<>();

       // Here we are considering that in shifts longer than 5 hours there's a 30min break.
        for (Shift shift : shifts) {

            shiftDTOS.add(new ShiftDTO(
                    workerId,
                    jobId,
                    shift.getStartDate(),
                    shift.getStartTime(),
                    shift.getEndDate(),
                    shift.getEndTime(),
                    shift.getShiftType(),
                    shift.getId()
                    )

            );

            totalHours += calculateShiftDuration(shift);
            System.out.println("Start date: " + shift.getStartDate() + " start time: " + shift.getStartTime());

        }

        grossPay = new BigDecimal(totalHours).multiply(jobHourlyRate);
        System.out.println("Total hours: " + totalHours + " times " + jobHourlyRate + " = " + grossPay);

        float nextShiftDuration = calculateShiftDuration(shifts.get(0));

        return new IncomeDTO(grossPay, shiftDTOS, shiftDTOS.get(0), nextShiftDuration, new BigDecimal(nextShiftDuration).multiply(jobHourlyRate) , totalHours);
    }

    private float calculateShiftDuration(Shift shift) {
        float shiftDuration = ChronoUnit.HOURS.between(shift.getStartTime(), shift.getEndTime());
        if (shiftDuration >= 5) {
            shiftDuration -= 0.5; // - 30min break
        }

        return shiftDuration;
    }
}
