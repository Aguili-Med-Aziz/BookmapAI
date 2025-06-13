package com.bookmaai.models;

import java.time.LocalDateTime;

/**
 * Safe Trading Analysis - نتيجة التحليل الآمن
 * 
 * يحتوي على التوصية الآمنة ومستوى المخاطر
 */
public class SafeTradingAnalysis {
    
    private final String safeRecommendation;
    private final RiskLevel riskLevel;
    private final double confidence;
    private final FinalAction finalAction;
    private final boolean safeToTrade;
    private final String safetyMessage;
    private final double safePositionSize;
    private final LocalDateTime timestamp;
    
    public SafeTradingAnalysis(String safeRecommendation, RiskLevel riskLevel, double confidence,
                             FinalAction finalAction, boolean safeToTrade, String safetyMessage,
                             double safePositionSize) {
        this.safeRecommendation = safeRecommendation;
        this.riskLevel = riskLevel;
        this.confidence = confidence;
        this.finalAction = finalAction;
        this.safeToTrade = safeToTrade;
        this.safetyMessage = safetyMessage;
        this.safePositionSize = safePositionSize;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public String getSafeRecommendation() { return safeRecommendation; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public double getConfidence() { return confidence; }
    public FinalAction getFinalAction() { return finalAction; }
    public boolean isSafeToTrade() { return safeToTrade; }
    public String getSafetyMessage() { return safetyMessage; }
    public double getSafePositionSize() { return safePositionSize; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    // Enums
    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public enum FinalAction {
        BUY, SELL, WAIT, AVOID
    }
    
    @Override
    public String toString() {
        return String.format("SafeTradingAnalysis{recommendation='%s', risk=%s, confidence=%.1f%%, safe=%s}", 
                           safeRecommendation, riskLevel, confidence * 100, safeToTrade);
    }
} 