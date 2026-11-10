package com.divya.examportal.dto;

import java.util.List;

/** The exam paper for one student, with the seconds they still have left. */
public record ExamForStudent(
        Long id,
        String title,
        String description,
        int durationMinutes,
        long remainingSeconds,
        List<QuestionForStudent> questions) {
}
