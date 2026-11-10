package com.divya.examportal.dto;

/** A question as the ADMIN sees it, including the correct answer. */
public record QuestionView(
        Long id,
        String questionText,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String correctOption,
        int marks) {
}
