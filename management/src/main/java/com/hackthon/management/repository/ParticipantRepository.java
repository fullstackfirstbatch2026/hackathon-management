package com.hackthon.management.repository;

import com.hackthon.management.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipantRepository
        extends JpaRepository<Participant, Long> {

    @Modifying
    @Query("DELETE FROM Participant p WHERE p.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);
}