package com.divya.examportal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExamRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title is too long")
        String title,

        @Size(max = 500, message = "Description is too long")
        String description,

        @Min(value = 1, message = "Duration must be at least 1 minute")
        @Max(value = 300, message = "Duration can be at most 300 minutes")
        int durationMinutes,

        // Optional. Empty means every student gets all the questions.
        @Min(value = 1, message = "Questions per student must be at least 1")
        Integer questionsPerAttempt) {
}
