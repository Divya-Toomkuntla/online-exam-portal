package com.divya.examportal.dto;

import java.util.Map;

/**
 * The answers a student sends when finishing an exam.
 * Key = question id, value = chosen option ("A", "B", "C" or "D").
 * Questions left unanswered are simply missing from the map.
 */
public record SubmitRequest(Map<Long, String> answers) {
}
