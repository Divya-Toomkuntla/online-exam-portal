package com.divya.examportal.dto;

import java.util.List;

/** One exam with all its questions (admin view). */
public record ExamDetail(
        Long id,
        String title,
        String description,
        int durationMinutes,
        List<QuestionView> questions) {
}
