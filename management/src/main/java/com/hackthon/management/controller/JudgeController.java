package com.hackthon.management.controller;

import com.hackthon.management.entity.Judge;
import com.hackthon.management.service.JudgeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/judges")
public class JudgeController {

    private final JudgeService judgeService;

    public JudgeController(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    @PostMapping
    public Judge createJudge(@Valid @RequestBody Judge judge) {
        return judgeService.createJudge(judge);
    }

    @GetMapping
    public List<Judge> getAllJudges() {
        return judgeService.getAllJudges();
    }

    @GetMapping("/{id}")
    public Judge getJudgeById(@PathVariable Long id) {
        return judgeService.getJudgeById(id);
    }

    @PutMapping("/{id}")
    public Judge updateJudge(
            @PathVariable Long id,
            @Valid @RequestBody Judge judge) {

        return judgeService.updateJudge(id, judge);
    }

    @DeleteMapping("/{id}")
    public String deleteJudge(@PathVariable Long id) {
        judgeService.deleteJudge(id);
        return "Judge deleted successfully";
    }
}