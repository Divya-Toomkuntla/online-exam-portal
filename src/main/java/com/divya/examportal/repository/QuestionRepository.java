package com.divya.examportal.repository;

import com.divya.examportal.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByExamId(Long examId);

    // Same questions, always in the order they were added
    List<Question> findByExamIdOrderByIdAsc(Long examId);

    long countByExamId(Long examId);
}
