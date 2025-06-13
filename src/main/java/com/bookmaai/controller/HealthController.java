package com.bookmaai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "BookmapAI System");
        response.put("status", "RUNNING");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "🎯 BookmapAI System is operational and ready for trading analysis!");
        return response;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("services", Map.of(
            "PatternEngine", "READY",
            "RiskRewardCalculator", "READY",
            "BookmapLevelExtractor", "READY",
            "SmartTargetCalculator", "READY"
        ));
        return response;
    }
} 