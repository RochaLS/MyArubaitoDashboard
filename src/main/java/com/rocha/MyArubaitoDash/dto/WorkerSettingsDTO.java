package com.rocha.MyArubaitoDash.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkerSettingsDTO {
    private int id;
    private BigDecimal payMultiplier;
    private int workerId;
}
