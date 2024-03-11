package com.rocha.MyArubaitoDash.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Worker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    @Transient
    private String location;
    @JsonIgnore
    private String encryptedLocation;


    @JsonIgnore
    @OneToMany(mappedBy =  "worker", cascade = CascadeType.ALL)
    private List<Shift> shifts;

    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Job> jobs;

}


