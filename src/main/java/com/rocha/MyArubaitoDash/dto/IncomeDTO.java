package com.rocha.MyArubaitoDash.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IncomeDTO {
    private BigDecimal totalGrossPay;
    private List<ShiftDTO> shifts;
    private ShiftDTO nextShift;
}
