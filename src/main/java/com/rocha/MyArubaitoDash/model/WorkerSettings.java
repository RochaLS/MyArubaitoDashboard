package com.rocha.MyArubaitoDash.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkerSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NonNull
    private BigDecimal payMultiplier = BigDecimal.valueOf(1.5);

    @OneToOne
    @JoinColumn(name = "worker_id")
    private Worker worker;
}
