package com.rocha.MyArubaitoDash.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/*
DTO stands for Data Transfer Object.
It's a design pattern used to transfer data between software application subsystems or layers that otherwise would not be compatible.
In essence, DTOs are objects that carry data between processes in order to reduce the number of method calls or data size across a network.
So in the case of this app I'm using DTOS to easily pass data using the ids (shift worker id and job id) instead of passing the whole objects.
It's highly adaptable!
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShiftDTO {
    @JsonProperty("worker_id") // This explicitly helps with the mapping. Having issues without it :(
    private int workerId;

    @JsonProperty("job_id")
    @NotNull
    private Integer jobId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private LocalTime endTime;

    private String shiftType;

    private Boolean isHoliday;

    private int id;

    @ToString.Exclude
    private BigDecimal moneyValue;

}
