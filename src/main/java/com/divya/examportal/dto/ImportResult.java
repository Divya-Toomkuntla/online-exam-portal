package com.divya.examportal.dto;

import java.util.List;

/**
 * Result of a bulk import. If there is any error, nothing is added (added = 0)
 * and the errors say which lines to fix.
 */
public record ImportResult(int added, List<String> errors) {
}
