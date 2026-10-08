package com.hackthon.management.controller;

import com.hackthon.management.entity.Participant;
import com.hackthon.management.service.ParticipantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @PostMapping
    public Participant createParticipant(@Valid @RequestBody Participant participant) {
        return participantService.createParticipant(participant);
    }

    @GetMapping
    public List<Participant> getAllParticipants() {
        return participantService.getAllParticipants();
    }

    @GetMapping("/{id}")
    public Participant getParticipantById(@PathVariable Long id) {
        return participantService.getParticipantById(id);
    }

    @PutMapping("/{id}")
    public Participant updateParticipant(
            @PathVariable Long id,
            @Valid @RequestBody Participant participant) {

        return participantService.updateParticipant(id, participant);
    }

    @DeleteMapping("/{id}")
    public String deleteParticipant(@PathVariable Long id) {
        participantService.deleteParticipant(id);
        return "Participant deleted successfully";
    }
}