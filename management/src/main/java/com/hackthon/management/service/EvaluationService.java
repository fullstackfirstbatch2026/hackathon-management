package com.hackthon.management.service;

import com.hackthon.management.entity.Evaluation;
import com.hackthon.management.entity.Judge;
import com.hackthon.management.entity.Project;
import com.hackthon.management.repository.EvaluationRepository;
import com.hackthon.management.repository.JudgeRepository;
import com.hackthon.management.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final ProjectRepository projectRepository;
    private final JudgeRepository judgeRepository;

    public EvaluationService(
            EvaluationRepository evaluationRepository,
            ProjectRepository projectRepository,
            JudgeRepository judgeRepository) {

        this.evaluationRepository = evaluationRepository;
        this.projectRepository = projectRepository;
        this.judgeRepository = judgeRepository;
    }

    /*
     * CREATE EVALUATION
     */
    public Evaluation createEvaluation(Evaluation evaluation) {

        if (evaluation.getProject() == null ||
                evaluation.getProject().getId() == null) {

            throw new IllegalArgumentException(
                    "Project ID is required"
            );
        }

        if (evaluation.getJudge() == null ||
                evaluation.getJudge().getId() == null) {

            throw new IllegalArgumentException(
                    "Judge ID is required"
            );
        }

        Project project = projectRepository.findById(
                evaluation.getProject().getId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Project not found"
                )
        );

        Judge judge = judgeRepository.findById(
                evaluation.getJudge().getId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Judge not found"
                )
        );

        evaluation.setProject(project);
        evaluation.setJudge(judge);

        return evaluationRepository.save(evaluation);
    }

    /*
     * GET ALL EVALUATIONS
     */
    public List<Evaluation> getAllEvaluations() {
        return evaluationRepository.findAll();
    }

    /*
     * GET EVALUATION BY ID
     */
    public Evaluation getEvaluationById(Long id) {

        return evaluationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Evaluation not found"
                        )
                );
    }

    /*
     * UPDATE EVALUATION
     */
    public Evaluation updateEvaluation(
            Long id,
            Evaluation evaluation) {

        Evaluation existing =
                getEvaluationById(id);

        if (evaluation.getProject() != null &&
                evaluation.getProject().getId() != null) {

            Project project =
                    projectRepository.findById(
                            evaluation.getProject().getId()
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "Project not found"
                            )
                    );

            existing.setProject(project);
        }

        if (evaluation.getJudge() != null &&
                evaluation.getJudge().getId() != null) {

            Judge judge =
                    judgeRepository.findById(
                            evaluation.getJudge().getId()
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "Judge not found"
                            )
                    );

            existing.setJudge(judge);
        }

        existing.setScore(
                evaluation.getScore()
        );

        existing.setComments(
                evaluation.getComments()
        );

        return evaluationRepository.save(existing);
    }

    /*
     * DELETE EVALUATION
     */
    public void deleteEvaluation(Long id) {

        if (!evaluationRepository.existsById(id)) {

            throw new RuntimeException(
                    "Evaluation not found"
            );
        }

        evaluationRepository.deleteById(id);
    }
}
