package com.divya.examportal.dto;

/**
 * One exam card on the student's dashboard.
 * questionCount is how many questions THIS student will get.
 */
public record ExamListItem(
        Long id,
        String title,
        String description,
        int durationMinutes,
        int questionCount,
        boolean attempted,
        boolean inProgress) {
}
