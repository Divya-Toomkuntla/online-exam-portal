package com.divya.examportal;

import com.divya.examportal.model.Question;
import com.divya.examportal.service.ScoringService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoringServiceTest {

    private final ScoringService scoringService = new ScoringService();

    private Question question(long id, String correctOption, int marks) {
        Question q = new Question("Question " + id, "a", "b", "c", "d", correctOption, marks);
        q.setId(id);
        return q;
    }

    private final List<Question> questions = List.of(
            question(1, "A", 1),
            question(2, "C", 2),
            question(3, "D", 3));

    @Test
    void totalMarksAddsUpAllQuestions() {
        assertEquals(6, scoringService.totalMarks(questions));
    }

    @Test
    void allCorrectGivesFullMarks() {
        Map<Long, String> answers = Map.of(1L, "A", 2L, "C", 3L, "D");
        assertEquals(6, scoringService.score(questions, answers));
    }

    @Test
    void onlyCorrectAnswersGetMarks() {
        Map<Long, String> answers = Map.of(1L, "A", 2L, "B", 3L, "D");
        assertEquals(4, scoringService.score(questions, answers));
    }

    @Test
    void unansweredQuestionsGetZero() {
        Map<Long, String> answers = new HashMap<>();
        answers.put(1L, "A");
        assertEquals(1, scoringService.score(questions, answers));
    }

    @Test
    void noAnswersGivesZero() {
        assertEquals(0, scoringService.score(questions, Map.of()));
        assertEquals(0, scoringService.score(questions, null));
    }

    @Test
    void answerLetterCaseDoesNotMatter() {
        assertTrue(scoringService.isCorrect(question(1, "A", 1), "a"));
        assertFalse(scoringService.isCorrect(question(1, "A", 1), "b"));
        assertFalse(scoringService.isCorrect(question(1, "A", 1), null));
    }
}
