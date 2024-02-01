package com.rocha.MyArubaitoDash.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Worker {
    @Id
    private int id;
    private String name;
    private BigDecimal hourlyRate;
    private String location;


    @OneToMany(mappedBy =  "worker", cascade = CascadeType.ALL)
    private List<Shift> shifts;

    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL)
    private List<Job> jobs;

}
