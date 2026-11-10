package com.divya.examportal.dto;

import java.util.List;

/** A question as a STUDENT sees it while taking the exam: no correct answer included. */
public record QuestionForStudent(
        Long id,
        String questionText,
        List<OptionView> options,
        int marks) {
}
