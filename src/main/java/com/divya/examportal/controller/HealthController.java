package com.divya.examportal.controller;

import com.divya.examportal.repository.ExamRepository;
import com.divya.examportal.repository.QuestionRepository;
import com.divya.examportal.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A tiny endpoint to check the app and database are working.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;

    public HealthController(UserRepository userRepository,
                            ExamRepository examRepository,
                            QuestionRepository questionRepository) {
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("status", "UP");
        info.put("users", userRepository.count());
        info.put("exams", examRepository.count());
        info.put("questions", questionRepository.count());
        return info;
    }
}
