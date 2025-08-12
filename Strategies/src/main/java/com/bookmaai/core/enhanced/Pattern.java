package com.bookmaai.core.enhanced;

import java.time.LocalDateTime;

/**
 * Pattern class for GPT4AnalysisEngine compatibility
 */
public class Pattern {
    private String type;
    private double confidence;
    private String symbol;
    private LocalDateTime timestamp;
    
    public Pattern(String type, double confidence, String symbol) {
        this.type = type;
        this.confidence = confidence;
        this.symbol = symbol;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getType() { return type; }
    public double getConfidence() { return confidence; }
    public String getSymbol() { return symbol; }
    public LocalDateTime getTimestamp() { return timestamp; }
} 