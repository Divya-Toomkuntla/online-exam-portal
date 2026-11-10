package com.divya.examportal.dto;

import java.util.List;

/** What the student gets back right after submitting. The note is empty unless something needs explaining. */
public record ResultView(
        Long resultId,
        String examTitle,
        int score,
        int totalMarks,
        List<QuestionReview> review,
        String note) {
}
