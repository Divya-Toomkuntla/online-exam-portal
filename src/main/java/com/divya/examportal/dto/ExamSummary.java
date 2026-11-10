package com.divya.examportal.dto;

import java.time.LocalDateTime;

/** One row in the admin exam list. */
public record ExamSummary(
        Long id,
        String title,
        String description,
        int durationMinutes,
        Integer questionsPerAttempt,
        long questionCount,
        LocalDateTime createdAt) {
}
