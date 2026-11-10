package com.divya.examportal.repository;

import com.divya.examportal.model.Result;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Long> {

    // The exam is loaded together with the result, so we can show the exam title
    @EntityGraph(attributePaths = {"exam"})
    List<Result> findByUserIdOrderByAttemptedAtDesc(Long userId);

    // Used when an exam is deleted, so its saved results do not block the delete
    void deleteByExamId(Long examId);
}
