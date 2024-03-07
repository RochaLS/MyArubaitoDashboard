package com.rocha.MyArubaitoDash.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobDTO {
    private int id;
    private String title;
    private BigDecimal hourlyRate;
    private int workerId;
}
