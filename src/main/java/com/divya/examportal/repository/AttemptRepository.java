package com.divya.examportal.repository;

import com.divya.examportal.model.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    Optional<Attempt> findByUserIdAndExamId(Long userId, Long examId);

    void deleteByExamId(Long examId);
}
