package com.rocha.MyArubaitoDash.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Job {
    @Id
    private int id;
    private String title;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private ArrayList<Shift> shifts;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private Worker worker;

}


