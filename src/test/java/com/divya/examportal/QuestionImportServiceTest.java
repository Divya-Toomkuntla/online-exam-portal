package com.divya.examportal;

import com.divya.examportal.model.Question;
import com.divya.examportal.service.QuestionImportService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionImportServiceTest {

    private final QuestionImportService service = new QuestionImportService();

    @Test
    void parsesAValidLineWithDefaultMarks() {
        var result = service.parse("What is 2 + 2? | 3 | 4 | 5 | 6 | b");

        assertTrue(result.errors().isEmpty());
        assertEquals(1, result.questions().size());

        Question q = result.questions().get(0);
        assertEquals("What is 2 + 2?", q.getQuestionText());
        assertEquals("4", q.getOptionB());
        assertEquals("B", q.getCorrectOption());
        assertEquals(1, q.getMarks());
    }

    @Test
    void readsOptionalMarks() {
        var result = service.parse("Q | a | b | c | d | A | 5");
        assertEquals(5, result.questions().get(0).getMarks());
    }

    @Test
    void ignoresBlankLinesAndComments() {
        var result = service.parse("# my questions\n\nQ1 | a | b | c | d | A\n\nQ2 | a | b | c | d | D\n");
        assertTrue(result.errors().isEmpty());
        assertEquals(2, result.questions().size());
    }

    @Test
    void rejectsWrongNumberOfParts() {
        var result = service.parse("Only a question | and one option");
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().get(0).startsWith("Line 1"));
    }

    @Test
    void rejectsCorrectLetterOutsideAToD() {
        var result = service.parse("Q | a | b | c | d | E");
        assertFalse(result.errors().isEmpty());
    }

    @Test
    void reportsTheLineNumberOfTheProblem() {
        var result = service.parse("Q1 | a | b | c | d | A\nQ2 | a | b | c | d | Z");
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).startsWith("Line 2"));
    }

    @Test
    void rejectsEmptyText() {
        assertFalse(service.parse("   ").errors().isEmpty());
        assertFalse(service.parse(null).errors().isEmpty());
    }
}
