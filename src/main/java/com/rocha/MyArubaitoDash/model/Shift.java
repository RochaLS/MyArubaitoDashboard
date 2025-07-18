package com.rocha.MyArubaitoDash.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private LocalDate startDate;
    private LocalTime startTime;
    private LocalDate endDate;
    private LocalTime endTime;
    private String shiftType;
    private Boolean isHoliday;

    @Transient
    @ToString.Exclude
    private BigDecimal moneyValue;

    @JsonIgnore
    private String encryptedMoneyValue;

    // If I were to implement eager loading in the future:

    //Specifying the sql join to get shifts based on worker and job.
    @ManyToOne
    @JoinColumn(name = "worker_id")
    @JsonIgnore
    private Worker worker;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

}
