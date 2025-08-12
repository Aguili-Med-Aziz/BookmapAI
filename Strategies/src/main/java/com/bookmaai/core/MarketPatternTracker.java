package com.bookmaai.core;

import java.util.List;
import java.util.ArrayList;

/**
 * Tracks patterns and predictions for a specific market/instrument
 */
public class MarketPatternTracker {
    private final String symbol;
    private final List<PatternDetection> detectedPatterns;
    private final List<MarketPrediction> predictions;
    private long lastPatternUpdate;
    private double lastPrice; // For price change calculation
    
    public MarketPatternTracker(String symbol) {
        this.symbol = symbol;
        this.detectedPatterns = new ArrayList<>();
        this.predictions = new ArrayList<>();
        this.lastPatternUpdate = System.currentTimeMillis();
        this.lastPrice = 0.0;
    }
    
    /**
     * Get the last recorded price for this symbol
     */
    public double getLastPrice() {
        return lastPrice;
    }
    
    /**
     * Update the last recorded price for this symbol
     */
    public void updateLastPrice(double price) {
        this.lastPrice = price;
    }
    
    public void addPattern(String patternType, double confidence) {
        PatternDetection pattern = new PatternDetection(
            patternType, confidence, System.currentTimeMillis()
        );
        detectedPatterns.add(pattern);
        
        // Keep only last 10 patterns
        if (detectedPatterns.size() > 10) {
            detectedPatterns.remove(0);
        }
        
        // Generate prediction based on pattern
        generatePrediction(pattern);
        
        lastPatternUpdate = System.currentTimeMillis();
        
        System.out.println("🎯 Pattern detected for " + symbol + ": " + patternType + 
                         " (confidence: " + String.format("%.1f%%", confidence * 100) + ")");
    }
    
    private void generatePrediction(PatternDetection pattern) {
        // Generate prediction based on pattern type
        String direction = "NEUTRAL";
        double probability = pattern.confidence * 0.8; // Conservative estimate
        String timeframe = "5-15 minutes";
        
        if (pattern.patternType.contains("BULLISH") || pattern.patternType.contains("BUY")) {
            direction = "UP";
            timeframe = "3-10 minutes";
        } else if (pattern.patternType.contains("BEARISH") || pattern.patternType.contains("SELL")) {
            direction = "DOWN";
            timeframe = "3-10 minutes";
        } else if (pattern.patternType.contains("BREAKOUT")) {
            direction = "STRONG MOVE";
            probability = Math.min(0.95, probability * 1.2);
            timeframe = "1-5 minutes";
        }
        
        MarketPrediction prediction = new MarketPrediction(
            direction, probability, timeframe, 
            "Based on " + pattern.patternType + " pattern",
            System.currentTimeMillis()
        );
        
        predictions.add(prediction);
        
        // Keep only last 5 predictions
        if (predictions.size() > 5) {
            predictions.remove(0);
        }
        
        System.out.println("🔮 Prediction for " + symbol + ": " + direction + 
                         " (" + String.format("%.1f%%", probability * 100) + " probability, " + timeframe + ")");
    }
    
    public String getSymbol() { return symbol; }
    public List<PatternDetection> getPatterns() { return new ArrayList<>(detectedPatterns); }
    public List<MarketPrediction> getPredictions() { return new ArrayList<>(predictions); }
    public long getLastUpdate() { return lastPatternUpdate; }
    
    /**
     * Get patterns and predictions as JSON for dashboard
     */
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"symbol\": \"").append(symbol).append("\",");
        json.append("\"last_update\": ").append(lastPatternUpdate).append(",");
        
        // Patterns
        json.append("\"patterns\": [");
        for (int i = 0; i < detectedPatterns.size(); i++) {
            if (i > 0) json.append(",");
            PatternDetection p = detectedPatterns.get(i);
            json.append("{");
            json.append("\"type\": \"").append(p.patternType).append("\",");
            json.append("\"confidence\": ").append(String.format("%.2f", p.confidence)).append(",");
            json.append("\"timestamp\": ").append(p.timestamp);
            json.append("}");
        }
        json.append("],");
        
        // Predictions
        json.append("\"predictions\": [");
        for (int i = 0; i < predictions.size(); i++) {
            if (i > 0) json.append(",");
            MarketPrediction p = predictions.get(i);
            json.append("{");
            json.append("\"direction\": \"").append(p.direction).append("\",");
            json.append("\"probability\": ").append(String.format("%.2f", p.probability)).append(",");
            json.append("\"timeframe\": \"").append(p.timeframe).append("\",");
            json.append("\"reason\": \"").append(p.reason).append("\",");
            json.append("\"timestamp\": ").append(p.timestamp);
            json.append("}");
        }
        json.append("]");
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Pattern detection data class
     */
    public static class PatternDetection {
        public final String patternType;
        public final double confidence;
        public final long timestamp;
        
        public PatternDetection(String patternType, double confidence, long timestamp) {
            this.patternType = patternType;
            this.confidence = confidence;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Market prediction data class
     */
    public static class MarketPrediction {
        public final String direction;
        public final double probability;
        public final String timeframe;
        public final String reason;
        public final long timestamp;
        
        public MarketPrediction(String direction, double probability, String timeframe, String reason, long timestamp) {
            this.direction = direction;
            this.probability = probability;
            this.timeframe = timeframe;
            this.reason = reason;
            this.timestamp = timestamp;
        }
    }
}