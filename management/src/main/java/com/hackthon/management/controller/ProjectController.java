package com.hackthon.management.controller;

import com.hackthon.management.dto.AboveAverageProjectDTO;
import com.hackthon.management.dto.ProjectTeamParticipantDTO;
import com.hackthon.management.entity.Project;
import com.hackthon.management.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // CREATE
    @PostMapping
    public Project createProject(
            @Valid @RequestBody Project project) {

        return projectService.createProject(project);
    }

    // GET ALL
    @GetMapping
    public List<Project> getAllProjects() {

        return projectService.getAllProjects();
    }

    // JOIN
    @GetMapping("/details")
    public List<ProjectTeamParticipantDTO>
    getProjectsWithTeamAndParticipants() {

        return projectService
                .getProjectsWithTeamAndParticipants();
    }

    // ABOVE AVERAGE
    @GetMapping("/above-average")
    public List<AboveAverageProjectDTO>
    getProjectsAboveAverage() {

        return projectService.getProjectsAboveAverage();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public Project getProjectById(
            @PathVariable Long id) {

        return projectService.getProjectById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Project updateProject(
            @PathVariable Long id,
            @Valid @RequestBody Project project) {

        return projectService.updateProject(id, project);
    }

    // AVERAGE SCORE
    @GetMapping("/{id}/average-score")
    public Double getProjectAverageScore(
            @PathVariable Long id) {

        return projectService.getProjectAverageScore(id);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String deleteProject(
            @PathVariable Long id) {

        projectService.deleteProject(id);

        return "Project deleted successfully";
    }

    // SUBMIT
    @PostMapping("/{id}/submit")
    public Project submitProject(
            @PathVariable Long id) {

        return projectService.submitProject(id);
    }
}