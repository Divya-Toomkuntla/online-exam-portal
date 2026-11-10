package com.divya.examportal.service;

import com.divya.examportal.dto.ExamForStudent;
import com.divya.examportal.dto.ExamListItem;
import com.divya.examportal.dto.OptionView;
import com.divya.examportal.dto.QuestionForStudent;
import com.divya.examportal.dto.QuestionReview;
import com.divya.examportal.dto.ResultView;
import com.divya.examportal.model.Attempt;
import com.divya.examportal.model.Exam;
import com.divya.examportal.model.Question;
import com.divya.examportal.model.Result;
import com.divya.examportal.model.User;
import com.divya.examportal.repository.AttemptRepository;
import com.divya.examportal.repository.ExamRepository;
import com.divya.examportal.repository.QuestionRepository;
import com.divya.examportal.repository.ResultRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The rules for a student taking an exam:
 *  - one attempt per exam
 *  - each student gets a random set of questions, in random order, with shuffled options
 *  - the timer is checked on the server, not only in the browser
 */
@Service
public class ExamAttemptService {

    // A few seconds of allowance for slow internet when the timer runs out
    private static final long GRACE_SECONDS = 30;

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final AttemptRepository attemptRepository;
    private final ResultRepository resultRepository;
    private final ScoringService scoringService;

    public ExamAttemptService(ExamRepository examRepository,
                              QuestionRepository questionRepository,
                              AttemptRepository attemptRepository,
                              ResultRepository resultRepository,
                              ScoringService scoringService) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.resultRepository = resultRepository;
        this.scoringService = scoringService;
    }

    /** Exams this student can see, with their own status for each. */
    @Transactional(readOnly = true)
    public List<ExamListItem> listExams(User user) {
        List<ExamListItem> items = new ArrayList<>();

        for (Exam exam : examRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))) {
            long poolSize = questionRepository.countByExamId(exam.getId());
            if (poolSize == 0) {
                continue; // an exam with no questions cannot be taken yet
            }
            Attempt attempt = attemptRepository
                    .findByUserIdAndExamId(user.getId(), exam.getId()).orElse(null);

            items.add(new ExamListItem(
                    exam.getId(),
                    exam.getTitle(),
                    exam.getDescription(),
                    exam.getDurationMinutes(),
                    servedCount(exam, (int) poolSize),
                    attempt != null && attempt.isSubmitted(),
                    attempt != null && !attempt.isSubmitted()));
        }
        return items;
    }

    /**
     * Starts the student's attempt, or resumes it if they already started
     * (for example after refreshing the page). A finished attempt cannot be started again.
     */
    @Transactional
    public ExamForStudent start(Long examId, User user) {
        Exam exam = findExam(examId);

        Attempt attempt = attemptRepository.findByUserIdAndExamId(user.getId(), examId).orElse(null);

        if (attempt != null && attempt.isSubmitted()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already attempted this exam");
        }

        if (attempt == null) {
            List<Question> pool = new ArrayList<>(questionRepository.findByExamIdOrderByIdAsc(examId));
            if (pool.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This exam has no questions yet");
            }

            // Shuffle, then keep only as many as this exam gives to each student
            Collections.shuffle(pool);
            List<Question> chosen = pool.subList(0, servedCount(exam, pool.size()));

            attempt = new Attempt();
            attempt.setUser(user);
            attempt.setExam(exam);
            attempt.setStartedAt(LocalDateTime.now());
            attempt.setQuestionIdList(chosen.stream().map(Question::getId).toList());
            attempt = attemptRepository.save(attempt);
        }

        // "attempt" was reassigned above, so Java needs a final copy to use inside the lambda
        final Attempt current = attempt;
        List<QuestionForStudent> questions = loadServedQuestions(current).stream()
                .map(q -> new QuestionForStudent(
                        q.getId(), q.getQuestionText(), optionsFor(q, current), q.getMarks()))
                .toList();

        return new ExamForStudent(
                exam.getId(), exam.getTitle(), exam.getDescription(),
                exam.getDurationMinutes(),
                Math.max(0, secondsLeft(attempt, exam)),
                questions);
    }

    /** Marks the answers, saves the result, and closes the attempt. */
    @Transactional
    public ResultView submit(Long examId, User user, Map<Long, String> answers) {
        Exam exam = findExam(examId);

        Attempt attempt = attemptRepository.findByUserIdAndExamId(user.getId(), examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start the exam first"));

        if (attempt.isSubmitted()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already submitted this exam");
        }

        List<Question> questions = loadServedQuestions(attempt);

        // If the time (plus a small allowance) is over, the answers are not counted
        boolean late = secondsLeft(attempt, exam) < -GRACE_SECONDS;
        final Map<Long, String> counted = (late || answers == null) ? Map.of() : answers;

        int score = scoringService.score(questions, counted);
        int total = scoringService.totalMarks(questions);

        attempt.setSubmitted(true);

        Result result = new Result();
        result.setUser(user);
        result.setExam(exam);
        result.setScore(score);
        result.setTotalMarks(total);
        result = resultRepository.save(result);

        List<QuestionReview> review = questions.stream()
                .map(q -> {
                    String chosen = counted.get(q.getId());
                    return new QuestionReview(
                            q.getQuestionText(),
                            optionsFor(q, attempt),
                            chosen,
                            q.getCorrectOption(),
                            scoringService.isCorrect(q, chosen),
                            q.getMarks());
                })
                .toList();

        String note = late ? "Time was over before your answers arrived, so they were not counted." : null;
        return new ResultView(result.getId(), exam.getTitle(), score, total, review, note);
    }

    // ---------- helpers ----------

    private Exam findExam(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found"));
    }

    /** How many questions a student gets: the exam's limit, or all of them if there is no limit. */
    private int servedCount(Exam exam, int poolSize) {
        Integer limit = exam.getQuestionsPerAttempt();
        if (limit == null || limit <= 0 || limit >= poolSize) {
            return poolSize;
        }
        return limit;
    }

    /** The questions given to this attempt, in the order they were given. */
    private List<Question> loadServedQuestions(Attempt attempt) {
        List<Long> ids = attempt.questionIdList();
        Map<Long, Question> byId = questionRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));
        // If the admin deleted a question after the student started, it is simply skipped
        return ids.stream().map(byId::get).filter(Objects::nonNull).toList();
    }

    /**
     * The four options in a shuffled order. The shuffle is seeded with the attempt and question,
     * so the same student sees the same order again if they refresh the page.
     */
    private List<OptionView> optionsFor(Question q, Attempt attempt) {
        List<OptionView> options = new ArrayList<>(List.of(
                new OptionView("A", q.getOptionA()),
                new OptionView("B", q.getOptionB()),
                new OptionView("C", q.getOptionC()),
                new OptionView("D", q.getOptionD())));
        Collections.shuffle(options, new Random(attempt.getId() * 31 + q.getId()));
        return options;
    }

    private long secondsLeft(Attempt attempt, Exam exam) {
        long used = Duration.between(attempt.getStartedAt(), LocalDateTime.now()).getSeconds();
        return exam.getDurationMinutes() * 60L - used;
    }
}
