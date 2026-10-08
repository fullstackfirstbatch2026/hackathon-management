package com.hackthon.management.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String projectName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String technology;

    @Column(nullable = false)
    private String status;

    @OneToOne
    @JoinColumn(
            name = "team_id",
            nullable = false,
            unique = true
    )
    private Team team;

    /*
     * A project has many evaluations.
     *
     * We are NOT using cascade here because
     * Evaluation deletion is handled explicitly
     * in ProjectService.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private List<Evaluation> evaluations = new ArrayList<>();

    public Project() {
    }

    public Project(
            Long id,
            String projectName,
            String description,
            String technology,
            String status,
            Team team
    ) {
        this.id = id;
        this.projectName = projectName;
        this.description = description;
        this.technology = technology;
        this.status = status;
        this.team = team;
    }

    public Long getId() {
        return id;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getDescription() {
        return description;
    }

    public String getTechnology() {
        return technology;
    }

    public String getStatus() {
        return status;
    }

    public Team getTeam() {
        return team;
    }

    public List<Evaluation> getEvaluations() {
        return evaluations;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void setEvaluations(List<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }
}