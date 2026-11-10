package com.divya.examportal.service;

import com.divya.examportal.model.Question;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns plain text into questions. One question per line, parts separated by the | sign:
 *
 *   Question | Option A | Option B | Option C | Option D | Correct letter | Marks (optional)
 *
 * Empty lines and lines starting with # are ignored.
 * Used by the admin "import many questions" feature and by the ready-made exams at startup.
 */
@Service
public class QuestionImportService {

    private static final int MAX_LINES = 500;
    private static final int MAX_ERRORS_SHOWN = 20;

    public record ParseResult(List<Question> questions, List<String> errors) {
    }

    public ParseResult parse(String text) {
        List<Question> questions = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        if (text == null || text.isBlank()) {
            errors.add("Nothing to import. Paste at least one question.");
            return new ParseResult(questions, errors);
        }

        String[] lines = text.split("\\R");
        if (lines.length > MAX_LINES) {
            errors.add("Too many lines. Import at most " + MAX_LINES + " lines at a time.");
            return new ParseResult(questions, errors);
        }

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String problem = parseLine(line, questions);
            if (problem != null) {
                errors.add("Line " + (i + 1) + ": " + problem);
            }
        }

        if (errors.isEmpty() && questions.isEmpty()) {
            errors.add("No questions found. Put one question on each line.");
        }
        if (errors.size() > MAX_ERRORS_SHOWN) {
            int more = errors.size() - MAX_ERRORS_SHOWN;
            errors = new ArrayList<>(errors.subList(0, MAX_ERRORS_SHOWN));
            errors.add("...and " + more + " more lines with problems.");
        }
        return new ParseResult(questions, errors);
    }

    /** Adds the question to the list if the line is fine. Returns a problem text, or null if OK. */
    private String parseLine(String line, List<Question> questions) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 6 || parts.length > 7) {
            return "expected 6 or 7 parts separated by | but found " + parts.length;
        }

        String text = parts[0].trim();
        String optionA = parts[1].trim();
        String optionB = parts[2].trim();
        String optionC = parts[3].trim();
        String optionD = parts[4].trim();
        String correct = parts[5].trim().toUpperCase();

        if (text.isEmpty()) return "the question is empty";
        if (text.length() > 500) return "the question is longer than 500 characters";
        if (optionA.isEmpty() || optionB.isEmpty() || optionC.isEmpty() || optionD.isEmpty()) {
            return "all four options must have text";
        }
        if (optionA.length() > 200 || optionB.length() > 200
                || optionC.length() > 200 || optionD.length() > 200) {
            return "an option is longer than 200 characters";
        }
        if (!correct.matches("[A-D]")) {
            return "the correct answer must be A, B, C or D (found \"" + parts[5].trim() + "\")";
        }

        int marks = 1;
        if (parts.length == 7 && !parts[6].isBlank()) {
            try {
                marks = Integer.parseInt(parts[6].trim());
            } catch (NumberFormatException e) {
                return "marks must be a whole number (found \"" + parts[6].trim() + "\")";
            }
            if (marks < 1 || marks > 100) {
                return "marks must be between 1 and 100";
            }
        }

        questions.add(new Question(text, optionA, optionB, optionC, optionD, correct, marks));
        return null;
    }
}
