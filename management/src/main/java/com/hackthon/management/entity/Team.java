package com.hackthon.management.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
public class Team {

    // =========================
    // ID
    // =========================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================
    // TEAM NAME
    // =========================

    @Column(nullable = false, unique = true)
    private String teamName;

    // =========================
    // DESCRIPTION
    // =========================

    private String description;

    // =========================
    // PARTICIPANTS
    // =========================

    /*
     * One team can have many participants.
     *
     * The team_id foreign key is stored
     * in the participants table.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "team")
    private List<Participant> participants = new ArrayList<>();

    // =========================
    // PROJECT
    // =========================

    /*
     * One team can have only ONE project.
     *
     * The actual relationship is controlled
     * by Project.team.
     *
     * Project:
     * @OneToOne
     * @JoinColumn(name = "team_id", unique = true)
     *
     * Team:
     * @OneToOne(mappedBy = "team")
     */
    @JsonIgnore
    @OneToOne(mappedBy = "team")
    private Project project;

    // =========================
    // CONSTRUCTORS
    // =========================

    public Team() {
    }

    public Team(
            Long id,
            String teamName,
            String description
    ) {
        this.id = id;
        this.teamName = teamName;
        this.description = description;
    }

    // =========================
    // GETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getDescription() {
        return description;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public Project getProject() {
        return project;
    }

    // =========================
    // SETTERS
    // =========================

    public void setId(Long id) {
        this.id = id;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setParticipants(
            List<Participant> participants) {

        this.participants = participants;
    }

    /*
     * IMPORTANT:
     *
     * This setter is required for the project
     * deletion fix.
     *
     * Before deleting a project:
     *
     * team.setProject(null);
     *
     * This removes the in-memory Team -> Project
     * reference before Hibernate flushes.
     */
    public void setProject(Project project) {
        this.project = project;
    }
}