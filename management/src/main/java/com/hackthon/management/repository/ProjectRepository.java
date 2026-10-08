package com.hackthon.management.repository;

import com.hackthon.management.dto.AboveAverageProjectDTO;
import com.hackthon.management.dto.ProjectTeamParticipantDTO;
import com.hackthon.management.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.query.Procedure;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    /*
     * ONE TEAM = ONE PROJECT
     */
    boolean existsByTeamId(Long teamId);


    /*
     * ABOVE-AVERAGE PROJECTS
     *
     * Finds projects having an evaluation score
     * greater than the average of all evaluations.
     */
    @Query(value = """
        SELECT DISTINCT
            p.id AS projectId,
            p.project_name AS projectName
        FROM projects p
        JOIN evaluations e
            ON p.id = e.project_id
        WHERE e.score > (
            SELECT AVG(score)
            FROM evaluations
        )
        """,
            nativeQuery = true)
    List<AboveAverageProjectDTO> findProjectsAboveAverage();


    /*
     * STORED PROCEDURE
     *
     * Calls:
     * CALL submit_project(projectId)
     */
    @Procedure(
            procedureName = "submit_project"
    )
    void submitProject(
            @Param("p_project_id") Long projectId
    );


    /*
     * MYSQL FUNCTION
     *
     * Calls:
     * calculate_project_average(projectId)
     */
    @Query(
            value = """
                SELECT calculate_project_average(:projectId)
                """,
            nativeQuery = true
    )
    Double calculateProjectAverage(
            @Param("projectId") Long projectId
    );


    /*
     * JOIN
     *
     * Project → Team → Participants
     */
    @Query(value = """
        SELECT
            p.id AS projectId,
            p.project_name AS projectName,
            t.id AS teamId,
            t.team_name AS teamName,
            pt.id AS participantId,
            pt.name AS participantName,
            pt.email AS participantEmail
        FROM projects p
        JOIN teams t
            ON p.team_id = t.id
        JOIN participants pt
            ON t.id = pt.team_id
        """,
            nativeQuery = true)
    List<ProjectTeamParticipantDTO>
    findProjectsWithTeamAndParticipants();
}