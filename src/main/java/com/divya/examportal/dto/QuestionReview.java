package com.divya.examportal.dto;

import java.util.List;

/** One question after the exam is finished: what the student chose and what was correct. */
public record QuestionReview(
        String questionText,
        List<OptionView> options,
        String yourAnswer,
        String correctOption,
        boolean correct,
        int marks) {
}
