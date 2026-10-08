package com.hackthon.management.service;

import com.hackthon.management.entity.Judge;
import com.hackthon.management.repository.JudgeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JudgeService {

    private final JudgeRepository judgeRepository;

    public JudgeService(JudgeRepository judgeRepository) {
        this.judgeRepository = judgeRepository;
    }

    public Judge createJudge(Judge judge) {
        return judgeRepository.save(judge);
    }

    public List<Judge> getAllJudges() {
        return judgeRepository.findAll();
    }

    public Judge getJudgeById(Long id) {
        return judgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Judge not found"));
    }

    public Judge updateJudge(Long id, Judge judge) {

        Judge existing = getJudgeById(id);

        existing.setName(judge.getName());
        existing.setEmail(judge.getEmail());
        existing.setExpertise(judge.getExpertise());

        return judgeRepository.save(existing);
    }

    public void deleteJudge(Long id) {
        judgeRepository.deleteById(id);
    }
}