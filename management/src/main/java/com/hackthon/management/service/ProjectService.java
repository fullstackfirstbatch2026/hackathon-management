package com.hackthon.management.service;

import com.hackthon.management.dto.AboveAverageProjectDTO;
import com.hackthon.management.dto.ProjectTeamParticipantDTO;
import com.hackthon.management.entity.Evaluation;
import com.hackthon.management.entity.Project;
import com.hackthon.management.entity.Team;
import com.hackthon.management.repository.EvaluationRepository;
import com.hackthon.management.repository.ProjectRepository;
import com.hackthon.management.repository.TeamRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final EvaluationRepository evaluationRepository;
    private final EntityManager entityManager;

    public ProjectService(
            ProjectRepository projectRepository,
            TeamRepository teamRepository,
            EvaluationRepository evaluationRepository,
            EntityManager entityManager) {

        this.projectRepository = projectRepository;
        this.teamRepository = teamRepository;
        this.evaluationRepository = evaluationRepository;
        this.entityManager = entityManager;
    }

    // =========================
    // CREATE PROJECT
    // =========================

    public Project createProject(Project project) {

        if (project.getTeam() == null ||
                project.getTeam().getId() == null) {

            throw new IllegalArgumentException(
                    "Team ID is required"
            );
        }

        Long teamId = project.getTeam().getId();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Team not found with ID: " + teamId
                        ));

        // One team can have only one project
        if (projectRepository.existsByTeamId(teamId)) {

            throw new IllegalStateException(
                    "This team already has a project"
            );
        }

        project.setTeam(team);

        // New projects start as DRAFT
        project.setStatus("DRAFT");

        return projectRepository.save(project);
    }

    // =========================
    // GET ALL PROJECTS
    // =========================

    public List<Project> getAllProjects() {

        return projectRepository.findAll();
    }

    // =========================
    // GET PROJECT BY ID
    // =========================

    public Project getProjectById(Long id) {

        return projectRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Project not found with ID: " + id
                        ));
    }

    // =========================
    // UPDATE PROJECT
    // =========================

    public Project updateProject(
            Long id,
            Project updatedProject) {

        Project existingProject =
                getProjectById(id);

        if (updatedProject.getProjectName() != null) {

            existingProject.setProjectName(
                    updatedProject.getProjectName()
            );
        }

        if (updatedProject.getDescription() != null) {

            existingProject.setDescription(
                    updatedProject.getDescription()
            );
        }

        if (updatedProject.getTechnology() != null) {

            existingProject.setTechnology(
                    updatedProject.getTechnology()
            );
        }

        if (updatedProject.getStatus() != null) {

            existingProject.setStatus(
                    updatedProject.getStatus()
            );
        }

        // =========================
        // UPDATE TEAM
        // =========================

        if (updatedProject.getTeam() != null &&
                updatedProject.getTeam().getId() != null) {

            Long newTeamId =
                    updatedProject.getTeam().getId();

            Team newTeam = teamRepository.findById(newTeamId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Team not found with ID: "
                                            + newTeamId
                            ));

            /*
             * If changing to another team,
             * make sure the new team doesn't
             * already have a project.
             */
            if (!newTeamId.equals(
                    existingProject.getTeam().getId())
                    && projectRepository.existsByTeamId(newTeamId)) {

                throw new IllegalStateException(
                        "This team already has a project"
                );
            }

            existingProject.setTeam(newTeam);
        }

        return projectRepository.save(existingProject);
    }

    // =========================
    // DELETE PROJECT
    // =========================

    @Transactional
    public void deleteProject(Long id) {

        /*
         * STEP 1
         * Find the project.
         */
        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Project not found with ID: " + id
                        ));

        /*
         * STEP 2
         * Get the team associated with this project.
         */
        Team team = project.getTeam();

        /*
         * STEP 3
         * IMPORTANT:
         *
         * Team has a reference:
         *
         * Team.project -> Project
         *
         * Before deleting the project, remove that
         * in-memory relationship.
         *
         * Otherwise Hibernate sees:
         *
         * Team -> deleted Project
         *
         * and throws:
         *
         * TransientPropertyValueException
         */
        if (team != null) {
            team.setProject(null);
        }

        /*
         * STEP 4
         * Delete evaluations first.
         *
         * evaluations.project_id references projects.id
         */
        evaluationRepository.deleteByProjectId(id);

        /*
         * STEP 5
         * Execute evaluation DELETE immediately.
         */
        evaluationRepository.flush();

        /*
         * STEP 6
         * Delete the project.
         */
        projectRepository.delete(project);

        /*
         * STEP 7
         * Execute project DELETE.
         *
         * Team.project is already null,
         * so Hibernate will not complain about
         * the Team referencing the deleted Project.
         */
        projectRepository.flush();

        /*
         * STEP 8
         * Clear persistence context.
         */
        entityManager.clear();
    }

    // =========================
    // JOIN QUERY
    // =========================

    public List<ProjectTeamParticipantDTO>
    getProjectsWithTeamAndParticipants() {

        return projectRepository
                .findProjectsWithTeamAndParticipants();
    }

    // =========================
    // ABOVE AVERAGE QUERY
    // =========================

    public List<AboveAverageProjectDTO>
    getProjectsAboveAverage() {

        return projectRepository
                .findProjectsAboveAverage();
    }

    // =========================
    // SUBMIT PROJECT
    // =========================

    @Transactional
    public Project submitProject(Long projectId) {

        Project project =
                getProjectById(projectId);

        if (!"DRAFT".equalsIgnoreCase(
                project.getStatus())) {

            throw new IllegalStateException(
                    "Only DRAFT projects can be submitted"
            );
        }

        /*
         * Calls MySQL procedure:
         *
         * submit_project(projectId)
         */
        projectRepository.submitProject(projectId);

        /*
         * Refresh project so that the new
         * SUBMITTED status is returned.
         */
        entityManager.refresh(project);

        return project;
    }

    // =========================
    // PROJECT AVERAGE SCORE
    // =========================

    public Double getProjectAverageScore(
            Long projectId) {

        /*
         * Check that project exists.
         */
        getProjectById(projectId);

        /*
         * Calls MySQL function:
         *
         * calculate_project_average(projectId)
         */
        return projectRepository
                .calculateProjectAverage(projectId);
    }
}