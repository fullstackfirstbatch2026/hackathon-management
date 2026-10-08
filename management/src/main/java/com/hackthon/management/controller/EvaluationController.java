package com.hackthon.management.controller;

import com.hackthon.management.entity.Evaluation;
import com.hackthon.management.service.EvaluationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping
    public Evaluation createEvaluation(@Valid @RequestBody Evaluation evaluation) {
        return evaluationService.createEvaluation(evaluation);
    }

    @GetMapping
    public List<Evaluation> getAllEvaluations() {
        return evaluationService.getAllEvaluations();
    }

    @GetMapping("/{id}")
    public Evaluation getEvaluationById(@PathVariable Long id) {
        return evaluationService.getEvaluationById(id);
    }

    @PutMapping("/{id}")
    public Evaluation updateEvaluation(
            @PathVariable Long id,
            @Valid @RequestBody Evaluation evaluation) {

        return evaluationService.updateEvaluation(id, evaluation);
    }

    @DeleteMapping("/{id}")
    public String deleteEvaluation(@PathVariable Long id) {
        evaluationService.deleteEvaluation(id);
        return "Evaluation deleted successfully";
    }
}