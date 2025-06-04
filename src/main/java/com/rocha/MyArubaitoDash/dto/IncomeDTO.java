package com.rocha.MyArubaitoDash.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IncomeDTO {
    @ToString.Exclude
    private BigDecimal totalGrossPay;

    private List<ShiftDTO> shifts;

    private ShiftDTO nextShift;

    private float nextShiftTotalHours;
    @ToString.Exclude
    private BigDecimal nextShiftGrossPay;

    private float totalHours;
}
