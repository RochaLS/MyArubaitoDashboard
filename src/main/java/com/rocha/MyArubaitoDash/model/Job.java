package com.rocha.MyArubaitoDash.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Transient // Not mapped to db
    private String title = null;
    @JsonIgnore
    private String encryptedTitle;
    @Transient // Not mapped to db
    private BigDecimal hourlyRate = null;
    @JsonIgnore
    private String encryptedHourlyRate;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Shift> shifts;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    @JsonIgnore
    private Worker worker;

}


