package com.bookmaai.services;

import java.time.LocalDateTime;

/**
 * 📊 Pattern Detection Result - نتيجة كشف النمط
 * 
 * يمثل نتيجة الكشف داخل النافذة الزمنية:
 * - detected: إذا اكتمل النمط بالكامل ضمن النافذة
 * - partial: إذا لم يكتمل النمط لكن هناك carry state للنافذة التالية
 * - carryState: حالة carry الحالية (PatternCarryState)
 * - confidence: مستوى الثقة في الكشف (0.0 - 1.0)
 * - outcome: نتيجة النمط (ربح/خسارة) إذا اكتمل
 * - pipsGained: عدد النقاط المكتسبة/المخسورة
 * - timeToOutcomeMinutes: الوقت المستغرق لتحقق النتيجة
 * - detectionTimestamp: وقت الكشف
 */
public class PatternDetectionResult {
    private boolean detected;
    private boolean partial;
    private PatternCarryState carryState;
    private double confidence;
    private boolean outcome;
    private int pipsGained;
    private int timeToOutcomeMinutes;
    private LocalDateTime detectionTimestamp;
    private String patternName;
    private String alertLevel; // LOW, MEDIUM, HIGH, CRITICAL
    
    public PatternDetectionResult(boolean detected, boolean partial, PatternCarryState carryState,
                                  double confidence, boolean outcome, int pipsGained, 
                                  int timeToOutcomeMinutes) {
        this.detected = detected;
        this.partial = partial;
        this.carryState = carryState;
        this.confidence = confidence;
        this.outcome = outcome;
        this.pipsGained = pipsGained;
        this.timeToOutcomeMinutes = timeToOutcomeMinutes;
        this.detectionTimestamp = LocalDateTime.now();
        this.alertLevel = calculateAlertLevel();
    }
    
    public PatternDetectionResult(String patternName, boolean detected, double confidence) {
        this.patternName = patternName;
        this.detected = detected;
        this.confidence = confidence;
        this.partial = false;
        this.carryState = new PatternCarryState();
        this.outcome = false;
        this.pipsGained = 0;
        this.timeToOutcomeMinutes = 0;
        this.detectionTimestamp = LocalDateTime.now();
        this.alertLevel = calculateAlertLevel();
    }
    
    /**
     * حساب مستوى التنبيه بناءً على الثقة والنتيجة
     */
    private String calculateAlertLevel() {
        if (!detected) return "LOW";
        
        if (confidence >= 0.9) return "CRITICAL";
        else if (confidence >= 0.8) return "HIGH";
        else if (confidence >= 0.6) return "MEDIUM";
        else return "LOW";
    }
    
    // Getters
    public boolean isDetected() { return detected; }
    public boolean isPartial() { return partial; }
    public PatternCarryState getCarryState() { return carryState; }
    public double getConfidence() { return confidence; }
    public boolean getOutcome() { return outcome; }
    public int getPipsGained() { return pipsGained; }
    public int getTimeToOutcomeMinutes() { return timeToOutcomeMinutes; }
    public LocalDateTime getDetectionTimestamp() { return detectionTimestamp; }
    public String getPatternName() { return patternName; }
    public String getAlertLevel() { return alertLevel; }
    
    // Setters
    public void setPatternName(String patternName) { this.patternName = patternName; }
    public void setDetected(boolean detected) { 
        this.detected = detected;
        this.alertLevel = calculateAlertLevel();
    }
    public void setConfidence(double confidence) { 
        this.confidence = confidence;
        this.alertLevel = calculateAlertLevel();
    }
    public void setOutcome(boolean outcome) { this.outcome = outcome; }
    public void setPipsGained(int pipsGained) { this.pipsGained = pipsGained; }
    
    /**
     * تحويل النتيجة إلى CSV row
     */
    public String toCsvRow() {
        return String.format("%s,%s,%s,%s,%.3f,%s,%d,%d,%s",
            detectionTimestamp != null ? detectionTimestamp.toString() : "",
            patternName != null ? patternName : "",
            detected ? "YES" : "NO",
            partial ? "YES" : "NO",
            confidence,
            outcome ? "PROFIT" : "LOSS",
            pipsGained,
            timeToOutcomeMinutes,
            alertLevel
        );
    }
    
    /**
     * CSV header
     */
    public static String getCsvHeader() {
        return "Timestamp,PatternName,Detected,Partial,Confidence,Outcome,PipsGained,TimeToOutcome,AlertLevel";
    }
    
    /**
     * تقييم جودة الكشف
     */
    public String getQualityRating() {
        if (!detected) return "No Pattern";
        
        double score = confidence;
        if (outcome && pipsGained > 10) score += 0.1;
        if (timeToOutcomeMinutes > 0 && timeToOutcomeMinutes <= 30) score += 0.05;
        
        if (score >= 0.95) return "Excellent";
        else if (score >= 0.85) return "Very Good";
        else if (score >= 0.75) return "Good";
        else if (score >= 0.65) return "Fair";
        else return "Poor";
    }
    
    @Override
    public String toString() {
        return String.format("PatternDetectionResult{pattern='%s', detected=%s, confidence=%.2f, quality='%s'}",
                           patternName, detected, confidence, getQualityRating());
    }
} 