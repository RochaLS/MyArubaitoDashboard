package com.rocha.MyArubaitoDash.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Shift {
    @Id
    private int id;
    private LocalDate startDate;
    private LocalTime startTime;
    private LocalDate endDate;
    private LocalTime endTime;
    private String shiftType;

    //Specifying the sql join to get shifts based on worker and job.
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "worker_id"),
            @JoinColumn(name = "job_id")
    })
    private Worker worker;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;
}
