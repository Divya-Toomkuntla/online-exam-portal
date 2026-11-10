package com.divya.examportal.service;

import com.divya.examportal.model.Question;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * The marking logic, kept separate from the web code so it is easy to test.
 */
@Service
public class ScoringService {

    /** Total marks available in an exam. */
    public int totalMarks(List<Question> questions) {
        return questions.stream().mapToInt(Question::getMarks).sum();
    }

    /** True only if the student chose the correct option (A to D, any letter case). */
    public boolean isCorrect(Question question, String chosen) {
        return chosen != null && chosen.trim().equalsIgnoreCase(question.getCorrectOption());
    }

    /**
     * Adds up the marks for every correct answer.
     * Wrong or missing answers give 0 marks (no negative marking).
     */
    public int score(List<Question> questions, Map<Long, String> answers) {
        if (answers == null) {
            return 0;
        }
        int score = 0;
        for (Question question : questions) {
            if (isCorrect(question, answers.get(question.getId()))) {
                score += question.getMarks();
            }
        }
        return score;
    }
}
