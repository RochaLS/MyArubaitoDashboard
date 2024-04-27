package com.rocha.MyArubaitoDash.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private String title;

    @JsonIgnore
    private String encryptedTitle;

    @Transient // Not mapped to db
    private BigDecimal hourlyRate;

    @JsonIgnore
    private String encryptedHourlyRate;

    //If I were to implement eager loading in the future:

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Shift> shifts;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    @JsonIgnore
    private Worker worker;

}


