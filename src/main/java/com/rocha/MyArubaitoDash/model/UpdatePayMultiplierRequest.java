package com.rocha.MyArubaitoDash.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePayMultiplierRequest {

    @NotNull(message = "Pay multiplier is required.")
    @DecimalMin(value = "1.0", message = "Pay multiplier must be greater than 0.")
    private BigDecimal payMultiplier;

    @NotNull(message = "Worker ID is required.")
    private Integer workerId;
}