package com.divya.examportal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Records that a student started an exam: when, and which questions they were given.
 * The unique constraint means one student can have only ONE attempt per exam.
 */
@Entity
@Table(name = "attempts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "exam_id"}))
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id")
    private Exam exam;

    private LocalDateTime startedAt = LocalDateTime.now();

    private boolean submitted;

    // The ids of the questions given to this student, in the order shown, like "7,3,9"
    @Column(nullable = false, length = 4000)
    private String questionIds;

    public List<Long> questionIdList() {
        return Arrays.stream(questionIds.split(","))
                .filter(s -> !s.isBlank())
                .map(Long::parseLong)
                .toList();
    }

    public void setQuestionIdList(List<Long> ids) {
        this.questionIds = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public boolean isSubmitted() { return submitted; }
    public void setSubmitted(boolean submitted) { this.submitted = submitted; }
}
