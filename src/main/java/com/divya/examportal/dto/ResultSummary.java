package com.divya.examportal.dto;

import java.time.LocalDateTime;

/** One row in "My results". */
public record ResultSummary(
        Long id,
        String examTitle,
        int score,
        int totalMarks,
        LocalDateTime attemptedAt) {
}
