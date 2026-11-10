package com.divya.examportal.dto;

public record AuthResponse(String token, String name, String email, String role) {
}
