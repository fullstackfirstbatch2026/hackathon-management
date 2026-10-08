package com.hackthon.management.dto;

public interface ProjectTeamParticipantDTO {

    Long getProjectId();

    String getProjectName();

    Long getTeamId();

    String getTeamName();

    Long getParticipantId();

    String getParticipantName();

    String getParticipantEmail();
}