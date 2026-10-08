package com.hackthon.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "participants")
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Column(nullable = false, unique = true)
    @NotBlank
    @Email
    private String email;

    private String college;

    private String department;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    public Participant() {
    }

    public Participant(String name, String email, String college, String department) {
        this.name = name;
        this.email = email;
        this.college = college;
        this.department = department;
    }

}
