package com.rocha.MyArubaitoDash.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobDTO {
    private int id;

    @NotNull
    @NotBlank
    private String title;

    @NotNull
    private BigDecimal hourlyRate;

    @NotNull
    private Integer workerId; // was int previously had to use the wrapper to apply validation
}
