package com.hackthon.management.service;

import com.hackthon.management.entity.Evaluation;
import com.hackthon.management.entity.Participant;
import com.hackthon.management.entity.Project;
import com.hackthon.management.entity.Team;
import com.hackthon.management.repository.EvaluationRepository;
import com.hackthon.management.repository.ParticipantRepository;
import com.hackthon.management.repository.ProjectRepository;
import com.hackthon.management.repository.TeamRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final ParticipantRepository participantRepository;
    private final ProjectRepository projectRepository;
    private final EvaluationRepository evaluationRepository;
    private final EntityManager entityManager;

    public TeamService(
            TeamRepository teamRepository,
            ParticipantRepository participantRepository,
            ProjectRepository projectRepository,
            EvaluationRepository evaluationRepository,
            EntityManager entityManager) {

        this.teamRepository = teamRepository;
        this.participantRepository = participantRepository;
        this.projectRepository = projectRepository;
        this.evaluationRepository = evaluationRepository;
        this.entityManager = entityManager;
    }

    // =========================
    // CREATE TEAM
    // =========================

    public Team createTeam(Team team) {

        if (team.getTeamName() == null ||
                team.getTeamName().isBlank()) {

            throw new IllegalArgumentException(
                    "Team name is required"
            );
        }

        return teamRepository.save(team);
    }

    // =========================
    // GET ALL TEAMS
    // =========================

    public List<Team> getAllTeams() {

        return teamRepository.findAll();
    }

    // =========================
    // GET TEAM BY ID
    // =========================

    public Team getTeamById(Long id) {

        return teamRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Team not found with ID: " + id
                        ));
    }

    // =========================
    // UPDATE TEAM
    // =========================

    public Team updateTeam(
            Long id,
            Team updatedTeam) {

        Team existingTeam =
                getTeamById(id);

        if (updatedTeam.getTeamName() != null &&
                !updatedTeam.getTeamName().isBlank()) {

            existingTeam.setTeamName(
                    updatedTeam.getTeamName()
            );
        }

        if (updatedTeam.getDescription() != null) {

            existingTeam.setDescription(
                    updatedTeam.getDescription()
            );
        }

        return teamRepository.save(existingTeam);
    }

    // =========================
    // DELETE TEAM
    // =========================

    @Transactional
    public void deleteTeam(Long id) {

        /*
         * STEP 1
         * Find the team.
         */
        Team team = teamRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Team not found with ID: " + id
                        ));

        /*
         * STEP 2
         * Check whether this team has a project.
         */
        Project project = team.getProject();

        /*
         * STEP 3
         * If a project exists, delete its evaluations
         * first.
         */
        if (project != null) {

            Long projectId = project.getId();

            /*
             * Delete evaluations belonging
             * to the project.
             */
            evaluationRepository.deleteByProjectId(
                    projectId
            );

            /*
             * Execute evaluation DELETE.
             */
            evaluationRepository.flush();

            /*
             * Disconnect Team -> Project.
             *
             * This prevents Hibernate from trying
             * to keep a deleted Project attached
             * to the Team.
             */
            team.setProject(null);

            /*
             * Delete the project.
             */
            projectRepository.delete(project);

            /*
             * Execute project DELETE.
             */
            projectRepository.flush();
        }

        /*
         * STEP 4
         * Delete all participants belonging
         * to this team.
         *
         * This is the important fix for your
         * current foreign-key error.
         */
        participantRepository.deleteByTeamId(id);

        /*
         * STEP 5
         * Execute participant DELETE first.
         */
        participantRepository.flush();

        /*
         * STEP 6
         * Clear Hibernate persistence context.
         */
        entityManager.clear();

        /*
         * STEP 7
         * Delete the team.
         */
        teamRepository.deleteById(id);

        /*
         * STEP 8
         * Execute team DELETE.
         */
        teamRepository.flush();
    }
}