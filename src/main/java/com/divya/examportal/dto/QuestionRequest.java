package com.divya.examportal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record QuestionRequest(
        @NotBlank(message = "Question text is required")
        @Size(max = 500, message = "Question is too long")
        String questionText,

        @NotBlank(message = "Option A is required")
        @Size(max = 200, message = "Option A is too long")
        String optionA,

        @NotBlank(message = "Option B is required")
        @Size(max = 200, message = "Option B is too long")
        String optionB,

        @NotBlank(message = "Option C is required")
        @Size(max = 200, message = "Option C is too long")
        String optionC,

        @NotBlank(message = "Option D is required")
        @Size(max = 200, message = "Option D is too long")
        String optionD,

        @NotBlank(message = "Choose the correct option")
        @Pattern(regexp = "[A-D]", message = "Correct option must be A, B, C or D")
        String correctOption,

        @Min(value = 1, message = "Marks must be at least 1")
        @Max(value = 100, message = "Marks can be at most 100")
        int marks) {
}
