package com.bookmaai.core.enhanced;

/**
 * 🎯 Iceberg Detection Result
 * 
 * Main result class for Enhanced Iceberg Detection containing all
 * analysis results and detection confidence metrics
 */
public class IcebergDetectionResult {
    private final String symbol;
    private final double price;
    private final long timestamp;
    
    private boolean detected;
    private double confidence;
    private HiddenSizeAnalysis hiddenSizeAnalysis;
    private RefillPatternAnalysis refillPatternAnalysis;
    private TimeBasedAnalysis timeBasedAnalysis;
    private StatisticalSignificance statisticalSignificance;
    
    public IcebergDetectionResult(String symbol, double price, long timestamp) {
        this.symbol = symbol;
        this.price = price;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public long getTimestamp() { return timestamp; }
    
    public boolean isDetected() { return detected; }
    public void setDetected(boolean detected) { this.detected = detected; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    
    public HiddenSizeAnalysis getHiddenSizeAnalysis() { return hiddenSizeAnalysis; }
    public void setHiddenSizeAnalysis(HiddenSizeAnalysis hiddenSizeAnalysis) { 
        this.hiddenSizeAnalysis = hiddenSizeAnalysis; }
    
    public RefillPatternAnalysis getRefillPatternAnalysis() { return refillPatternAnalysis; }
    public void setRefillPatternAnalysis(RefillPatternAnalysis refillPatternAnalysis) { 
        this.refillPatternAnalysis = refillPatternAnalysis; }
    
    public TimeBasedAnalysis getTimeBasedAnalysis() { return timeBasedAnalysis; }
    public void setTimeBasedAnalysis(TimeBasedAnalysis timeBasedAnalysis) { 
        this.timeBasedAnalysis = timeBasedAnalysis; }
    
    public StatisticalSignificance getStatisticalSignificance() { return statisticalSignificance; }
    public void setStatisticalSignificance(StatisticalSignificance statisticalSignificance) { 
        this.statisticalSignificance = statisticalSignificance; }
    
    /**
     * Check if this is a high-confidence iceberg detection
     */
    public boolean isHighConfidenceDetection() {
        return detected && confidence >= 85.0;
    }
    
    @Override
    public String toString() {
        return String.format("IcebergDetectionResult{symbol='%s', price=%.4f, detected=%s, confidence=%.1f%%}", 
                           symbol, price, detected, confidence);
    }
}

// Supporting classes
class HiddenSizeAnalysis {
    private double estimatedTotalSize;
    private double hiddenRatio;
    private double executionEfficiency;
    private double sizeConfidence;
    
    public HiddenSizeAnalysis(double estimatedTotalSize, double hiddenRatio, double executionEfficiency, double sizeConfidence) {
        this.estimatedTotalSize = estimatedTotalSize;
        this.hiddenRatio = hiddenRatio;
        this.executionEfficiency = executionEfficiency;
        this.sizeConfidence = sizeConfidence;
    }
    
    public double getEstimatedTotalSize() { return estimatedTotalSize; }
    public double getHiddenRatio() { return hiddenRatio; }
    public double getExecutionEfficiency() { return executionEfficiency; }
    public double getSizeConfidence() { return sizeConfidence; }
}

class RefillPatternAnalysis {
    private double refillConsistency;
    private double averageRefillTime;
    private int refillCount;
    private double patternStrength;
    
    public RefillPatternAnalysis(double refillConsistency, double averageRefillTime, int refillCount, double patternStrength) {
        this.refillConsistency = refillConsistency;
        this.averageRefillTime = averageRefillTime;
        this.refillCount = refillCount;
        this.patternStrength = patternStrength;
    }
    
    public double getRefillConsistency() { return refillConsistency; }
    public double getAverageRefillTime() { return averageRefillTime; }
    public int getRefillCount() { return refillCount; }
    public double getPatternStrength() { return patternStrength; }
}

class TimeBasedAnalysis {
    private double executionTimeConsistency;
    private double averageExecutionInterval;
    private double timeBasedConfidence;
    
    public TimeBasedAnalysis(double executionTimeConsistency, double averageExecutionInterval, double timeBasedConfidence) {
        this.executionTimeConsistency = executionTimeConsistency;
        this.averageExecutionInterval = averageExecutionInterval;
        this.timeBasedConfidence = timeBasedConfidence;
    }
    
    public double getExecutionTimeConsistency() { return executionTimeConsistency; }
    public double getAverageExecutionInterval() { return averageExecutionInterval; }
    public double getTimeBasedConfidence() { return timeBasedConfidence; }
}

class StatisticalSignificance {
    private double pValue;
    private double zScore;
    private double confidenceInterval;
    private boolean isSignificant;
    
    public StatisticalSignificance(double pValue, double zScore, double confidenceInterval, boolean isSignificant) {
        this.pValue = pValue;
        this.zScore = zScore;
        this.confidenceInterval = confidenceInterval;
        this.isSignificant = isSignificant;
    }
    
    public double getPValue() { return pValue; }
    public double getZScore() { return zScore; }
    public double getConfidenceInterval() { return confidenceInterval; }
    public boolean isSignificant() { return isSignificant; }
}