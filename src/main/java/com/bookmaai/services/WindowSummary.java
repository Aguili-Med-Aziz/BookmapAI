package com.bookmaai.services;

import java.time.LocalDateTime;

/**
 * 📋 ملخص النافذة - يحتوي على البيانات المجمعة لنافذة زمنية مكتملة
 */
public class WindowSummary {
    
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String patternName;
    private final int totalSnapshots;
    private final double totalAbsorption;
    private final double peakCVD;
    private final double totalIcebergVolume;
    private final double averageVolume;
    private final double priceRange;
    
    // إحصائيات إضافية
    private boolean patternDetected;
    private double patternConfidence;
    private String outcome; // SUCCESS, STOPPED, ONGOING
    private double pipsGained;
    
    public WindowSummary(LocalDateTime startTime, LocalDateTime endTime, String patternName,
                        int totalSnapshots, double totalAbsorption, double peakCVD,
                        double totalIcebergVolume, double averageVolume, double priceRange) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.patternName = patternName;
        this.totalSnapshots = totalSnapshots;
        this.totalAbsorption = totalAbsorption;
        this.peakCVD = peakCVD;
        this.totalIcebergVolume = totalIcebergVolume;
        this.averageVolume = averageVolume;
        this.priceRange = priceRange;
        
        // القيم الافتراضية
        this.patternDetected = patternName != null && !patternName.equals("NONE");
        this.patternConfidence = 0.0;
        this.outcome = "ONGOING";
        this.pipsGained = 0.0;
    }
    
    /**
     * تحديث نتيجة النمط
     */
    public void updatePatternResult(String outcome, double pipsGained, double confidence) {
        this.outcome = outcome;
        this.pipsGained = pipsGained;
        this.patternConfidence = confidence;
    }
    
    /**
     * حساب مدة النافذة بالدقائق
     */
    public long getDurationMinutes() {
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }
    
    /**
     * تحديد ما إذا كانت النافذة ناجحة
     */
    public boolean isSuccessful() {
        return "SUCCESS".equals(outcome) && pipsGained > 0;
    }
    
    /**
     * حساب درجة النافذة الإجمالية
     */
    public double calculateOverallScore() {
        double score = 0.0;
        
        // درجة بناءً على كشف النمط
        if (patternDetected) {
            score += 25.0;
        }
        
        // درجة بناءً على الثقة
        score += patternConfidence * 25.0;
        
        // درجة بناءً على النتيجة
        if ("SUCCESS".equals(outcome)) {
            score += 30.0;
        } else if ("ONGOING".equals(outcome)) {
            score += 10.0;
        }
        
        // درجة بناءً على النقاط المكتسبة
        if (pipsGained > 0) {
            score += Math.min(20.0, pipsGained * 2);
        }
        
        return Math.min(100.0, score);
    }
    
    // Getters
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getPatternName() { return patternName; }
    public int getTotalSnapshots() { return totalSnapshots; }
    public double getTotalAbsorption() { return totalAbsorption; }
    public double getPeakCVD() { return peakCVD; }
    public double getTotalIcebergVolume() { return totalIcebergVolume; }
    public double getAverageVolume() { return averageVolume; }
    public double getPriceRange() { return priceRange; }
    public boolean isPatternDetected() { return patternDetected; }
    public double getPatternConfidence() { return patternConfidence; }
    public String getOutcome() { return outcome; }
    public double getPipsGained() { return pipsGained; }
    
    @Override
    public String toString() {
        return String.format("WindowSummary[%s to %s | Pattern: %s | Snapshots: %d | Score: %.1f]",
                startTime, endTime, patternName, totalSnapshots, calculateOverallScore());
    }
} 