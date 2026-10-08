package com.hackthon.management.repository;

import com.hackthon.management.entity.Judge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JudgeRepository extends JpaRepository<Judge, Long> {
}