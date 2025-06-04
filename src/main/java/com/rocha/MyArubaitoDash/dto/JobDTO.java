package com.rocha.MyArubaitoDash.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobDTO {
    private int id;

    @NotNull
    @NotBlank
    @ToString.Exclude
    private String title;

    @NotNull
    @ToString.Exclude
    private BigDecimal hourlyRate;

    @JsonProperty("worker_id")
    @NotNull
    private Integer workerId; // was int previously had to use the wrapper to apply validation
}
