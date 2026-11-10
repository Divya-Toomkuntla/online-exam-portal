package com.divya.examportal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Only admins can open anything under /api/admin (set in SecurityConfig).
 * This small endpoint is just to test that. Stage 3 adds real admin features here.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("message", "Hello admin, you are allowed here");
    }
}
