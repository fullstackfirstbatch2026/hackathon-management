package com.hackthon.management.service;

import com.hackthon.management.entity.Participant;
import com.hackthon.management.entity.Team;
import com.hackthon.management.repository.ParticipantRepository;
import com.hackthon.management.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final TeamRepository teamRepository;

    public ParticipantService(
            ParticipantRepository participantRepository,
            TeamRepository teamRepository) {

        this.participantRepository = participantRepository;
        this.teamRepository = teamRepository;
    }

    public Participant createParticipant(Participant participant) {

        if (participant.getTeam() == null ||
                participant.getTeam().getId() == null) {
            throw new IllegalArgumentException("Team ID is required");
        }

        Team team = teamRepository.findById(
                participant.getTeam().getId()
        ).orElseThrow(() ->
                new RuntimeException("Team not found"));

        participant.setTeam(team);

        return participantRepository.save(participant);
    }

    public List<Participant> getAllParticipants() {
        return participantRepository.findAll();
    }

    public Participant getParticipantById(Long id) {
        return participantRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Participant not found"));
    }

    public Participant updateParticipant(Long id, Participant participant) {

        Participant existing = getParticipantById(id);

        existing.setName(participant.getName());
        existing.setEmail(participant.getEmail());
        existing.setCollege(participant.getCollege());
        existing.setDepartment(participant.getDepartment());

        if (participant.getTeam() != null) {

            if (participant.getTeam().getId() == null) {
                throw new IllegalArgumentException("Team ID is required");
            }

            Team team = teamRepository.findById(
                    participant.getTeam().getId()
            ).orElseThrow(() ->
                    new RuntimeException("Team not found"));

            existing.setTeam(team);
        }

        return participantRepository.save(existing);
    }

    public void deleteParticipant(Long id) {
        participantRepository.deleteById(id);
    }
}
