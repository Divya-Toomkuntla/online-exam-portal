package com.divya.examportal.controller;

import com.divya.examportal.dto.ExamForStudent;
import com.divya.examportal.dto.ExamListItem;
import com.divya.examportal.dto.ResultSummary;
import com.divya.examportal.dto.ResultView;
import com.divya.examportal.dto.SubmitRequest;
import com.divya.examportal.model.User;
import com.divya.examportal.repository.ResultRepository;
import com.divya.examportal.repository.UserRepository;
import com.divya.examportal.service.ExamAttemptService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * What a logged-in student does: see exams, start one, submit it, see past results.
 * The rules themselves live in ExamAttemptService.
 */
@RestController
@RequestMapping("/api")
public class StudentExamController {

    private final ExamAttemptService attemptService;
    private final ResultRepository resultRepository;
    private final UserRepository userRepository;

    public StudentExamController(ExamAttemptService attemptService,
                                 ResultRepository resultRepository,
                                 UserRepository userRepository) {
        this.attemptService = attemptService;
        this.resultRepository = resultRepository;
        this.userRepository = userRepository;
    }

    /** Exams with questions, and this student's status for each. */
    @GetMapping("/exams")
    public List<ExamListItem> listExams(Authentication authentication) {
        return attemptService.listExams(currentUser(authentication));
    }

    /** Starts (or resumes) the student's one attempt and returns the shuffled paper. */
    @PostMapping("/exams/{id}/start")
    public ExamForStudent start(@PathVariable Long id, Authentication authentication) {
        return attemptService.start(id, currentUser(authentication));
    }

    /** Marks the answers, saves the result, and returns the score with a review. */
    @PostMapping("/exams/{id}/submit")
    public ResultView submit(@PathVariable Long id,
                             @RequestBody SubmitRequest request,
                             Authentication authentication) {
        return attemptService.submit(id, currentUser(authentication),
                request == null ? null : request.answers());
    }

    /** All past attempts of the logged-in student, newest first. */
    @GetMapping("/results")
    public List<ResultSummary> myResults(Authentication authentication) {
        User user = currentUser(authentication);
        return resultRepository.findByUserIdOrderByAttemptedAtDesc(user.getId())
                .stream()
                .map(r -> new ResultSummary(
                        r.getId(),
                        r.getExam().getTitle(),
                        r.getScore(),
                        r.getTotalMarks(),
                        r.getAttemptedAt()))
                .toList();
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
