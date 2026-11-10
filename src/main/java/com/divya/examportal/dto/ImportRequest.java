package com.divya.examportal.dto;

/** The text the admin pasted (or loaded from a file) to import many questions at once. */
public record ImportRequest(String text) {
}
