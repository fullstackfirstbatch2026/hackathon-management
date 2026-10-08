package com.hackthon.management.repository;

import com.hackthon.management.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    @Modifying
    @Query("DELETE FROM Evaluation e WHERE e.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}