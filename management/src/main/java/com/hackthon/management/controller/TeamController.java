package com.hackthon.management.controller;

import com.hackthon.management.entity.Team;
import com.hackthon.management.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public Team createTeam(@Valid @RequestBody Team team) {
        return teamService.createTeam(team);
    }

    @GetMapping
    public List<Team> getAllTeams() {
        return teamService.getAllTeams();
    }

    @GetMapping("/{id}")
    public Team getTeamById(@PathVariable Long id) {
        return teamService.getTeamById(id);
    }

    @PutMapping("/{id}")
    public Team updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody Team team) {

        return teamService.updateTeam(id, team);
    }

    @DeleteMapping("/{id}")
    public String deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return "Team deleted successfully";
    }
}