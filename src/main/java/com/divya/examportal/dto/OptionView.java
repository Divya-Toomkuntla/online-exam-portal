package com.divya.examportal.dto;

/**
 * One answer choice. The key ("A" to "D") is its original letter and is what the student sends back.
 * The text is what they see. The order of options is shuffled, so the letters are not shown.
 */
public record OptionView(String key, String text) {
}
