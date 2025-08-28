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

    private int shiftCount;

    private int openingCount;
    private int midCount;
    private int closingCount;
    private int completedShifts;

    private ShiftDTO nextShift;

    private float nextShiftTotalHours;
    @ToString.Exclude
    private BigDecimal nextShiftGrossPay;

    private float totalHours;
}
