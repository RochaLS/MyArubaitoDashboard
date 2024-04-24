package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.model.Job;
import com.rocha.MyArubaitoDash.model.Shift;
import com.rocha.MyArubaitoDash.repository.JobRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public BigDecimal calculateGrossPay(LocalDate fromDate, int workerId, int jobId) {
       List<Shift> shifts = shiftService.getShiftsFrom(fromDate,workerId, jobId);

       if (shifts.size() == 0) {
           throw new EntityNotFoundException("No shifts found");
       }
       Job job = jobService.getJobById(1);
       BigDecimal jobHourlyRate = job.getHourlyRate();

       float totalHours = 0;
       BigDecimal grossPay;


       // Here we are considering that in shifts longer than 5 hours there's a 30min break.
        for (Shift shift : shifts) {
            float shiftDuration = ChronoUnit.HOURS.between(shift.getStartTime(), shift.getEndTime());
            if (shiftDuration >= 5) {
                shiftDuration -= 0.5; // - 30min break
            }

            totalHours += shiftDuration;
        }

        grossPay = new BigDecimal(totalHours).multiply(jobHourlyRate);


        System.out.println("Total hours: " + totalHours + " times " + jobHourlyRate + " = " + grossPay);

        return grossPay; //Will return a incomeDto later... to be implemented
    }
}
