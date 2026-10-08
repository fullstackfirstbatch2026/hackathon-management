package com.hackthon.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "judges")
public class Judge {

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

    private String expertise;

    public Judge() {
    }

    public Judge(String name, String email, String expertise) {
        this.name = name;
        this.email = email;
        this.expertise = expertise;
    }

}
