package com.divya.examportal.controller;

import com.divya.examportal.dto.ExamDetail;
import com.divya.examportal.dto.ExamRequest;
import com.divya.examportal.dto.ExamSummary;
import com.divya.examportal.dto.ImportRequest;
import com.divya.examportal.dto.ImportResult;
import com.divya.examportal.dto.QuestionRequest;
import com.divya.examportal.dto.QuestionView;
import com.divya.examportal.model.Exam;
import com.divya.examportal.model.Question;
import com.divya.examportal.repository.AttemptRepository;
import com.divya.examportal.repository.ExamRepository;
import com.divya.examportal.repository.QuestionRepository;
import com.divya.examportal.repository.ResultRepository;
import com.divya.examportal.service.QuestionImportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Everything an admin does with exams and questions.
 * The whole /api/admin/** path is already limited to admins in SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/exams")
public class AdminExamController {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ResultRepository resultRepository;
    private final AttemptRepository attemptRepository;
    private final QuestionImportService importService;

    public AdminExamController(ExamRepository examRepository,
                               QuestionRepository questionRepository,
                               ResultRepository resultRepository,
                               AttemptRepository attemptRepository,
                               QuestionImportService importService) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.resultRepository = resultRepository;
        this.attemptRepository = attemptRepository;
        this.importService = importService;
    }

    /** List all exams, newest first. */
    @GetMapping
    public List<ExamSummary> list() {
        return examRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::toSummary)
                .toList();
    }

    /** Create a new exam (without questions for now). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExamSummary create(@Valid @RequestBody ExamRequest request) {
        Exam exam = new Exam();
        exam.setTitle(request.title().trim());
        exam.setDescription(request.description() == null ? null : request.description().trim());
        exam.setDurationMinutes(request.durationMinutes());
        exam.setQuestionsPerAttempt(request.questionsPerAttempt());
        return toSummary(examRepository.save(exam));
    }

    /** One exam with all its questions and their correct answers. */
    @GetMapping("/{id}")
    public ExamDetail get(@PathVariable Long id) {
        Exam exam = findExam(id);
        List<QuestionView> questions = questionRepository.findByExamIdOrderByIdAsc(id)
                .stream()
                .map(this::toView)
                .toList();
        return new ExamDetail(exam.getId(), exam.getTitle(), exam.getDescription(),
                exam.getDurationMinutes(), questions);
    }

    /** Delete an exam together with its questions, attempts and saved results. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable Long id) {
        Exam exam = findExam(id);
        resultRepository.deleteByExamId(id);
        attemptRepository.deleteByExamId(id);
        examRepository.delete(exam); // questions are removed too (cascade)
    }

    /**
     * Lets every student take the exam again: deletes all attempts and results of this exam.
     * Useful for testing and demos, since each student normally gets only one attempt.
     */
    @DeleteMapping("/{id}/attempts")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void resetAttempts(@PathVariable Long id) {
        findExam(id);
        resultRepository.deleteByExamId(id);
        attemptRepository.deleteByExamId(id);
    }

    /** Add one multiple choice question to an exam. */
    @PostMapping("/{examId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionView addQuestion(@PathVariable Long examId,
                                    @Valid @RequestBody QuestionRequest request) {
        Exam exam = findExam(examId);

        Question question = new Question(
                request.questionText().trim(),
                request.optionA().trim(),
                request.optionB().trim(),
                request.optionC().trim(),
                request.optionD().trim(),
                request.correctOption(),
                request.marks());
        question.setExam(exam);

        return toView(questionRepository.save(question));
    }

    /**
     * Add many questions at once from pasted text (one question per line).
     * If any line has a problem, NOTHING is added and the problems are listed,
     * so the admin can fix them and try again without creating duplicates.
     */
    @PostMapping("/{examId}/questions/import")
    @Transactional
    public ImportResult importQuestions(@PathVariable Long examId,
                                        @RequestBody ImportRequest request) {
        Exam exam = findExam(examId);

        QuestionImportService.ParseResult parsed =
                importService.parse(request == null ? null : request.text());
        if (!parsed.errors().isEmpty()) {
            return new ImportResult(0, parsed.errors());
        }

        for (Question question : parsed.questions()) {
            question.setExam(exam);
        }
        questionRepository.saveAll(parsed.questions());
        return new ImportResult(parsed.questions().size(), List.of());
    }

    /** Delete one question from an exam. */
    @DeleteMapping("/{examId}/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deleteQuestion(@PathVariable Long examId, @PathVariable Long questionId) {
        Question question = questionRepository.findById(questionId)
                .filter(q -> q.getExam().getId().equals(examId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        questionRepository.delete(question);
    }

    // ---------- helpers ----------

    private Exam findExam(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found"));
    }

    private ExamSummary toSummary(Exam exam) {
        return new ExamSummary(
                exam.getId(),
                exam.getTitle(),
                exam.getDescription(),
                exam.getDurationMinutes(),
                exam.getQuestionsPerAttempt(),
                questionRepository.countByExamId(exam.getId()),
                exam.getCreatedAt());
    }

    private QuestionView toView(Question q) {
        return new QuestionView(
                q.getId(),
                q.getQuestionText(),
                q.getOptionA(),
                q.getOptionB(),
                q.getOptionC(),
                q.getOptionD(),
                q.getCorrectOption(),
                q.getMarks());
    }
}
