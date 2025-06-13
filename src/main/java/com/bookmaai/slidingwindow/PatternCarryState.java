package com.bookmaai.slidingwindow;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * 📈 حالة النمط المنقولة بين النوافذ
 * تتبع تطور النمط من البداية حتى الاكتمال
 */
public class PatternCarryState {
    
    // مراحل دورة حياة النمط
    public enum PatternLifecycle {
        INITIATING("🌱 Initiating", 0, 20),           // بداية التكوين
        FORMING("📊 Forming", 20, 40),                // التكوين المبكر
        DEVELOPING("📈 Developing", 40, 60),          // التطور
        MATURING("⚡ Maturing", 60, 80),              // النضج
        CONFIRMING("✅ Confirming", 80, 95),          // التأكيد
        COMPLETED("🎯 Completed", 95, 100),           // مكتمل
        FAILED("❌ Failed", -1, -1),                  // فشل
        INVALIDATED("⛔ Invalidated", -1, -1),        // ألغي
        SUCCESSFUL("🏆 Successful", 100, 100);        // نجح
        
        private final String display;
        private final int minProgress;
        private final int maxProgress;
        
        PatternLifecycle(String display, int minProgress, int maxProgress) {
            this.display = display;
            this.minProgress = minProgress;
            this.maxProgress = maxProgress;
        }
        
        public static PatternLifecycle fromProgress(double progress) {
            if (progress < 0) return FAILED;
            if (progress >= 100) return SUCCESSFUL;
            if (progress < 20) return INITIATING;
            if (progress < 40) return FORMING;
            if (progress < 60) return DEVELOPING;
            if (progress < 80) return MATURING;
            if (progress < 95) return CONFIRMING;
            return COMPLETED;
        }
        
        public String getDisplay() { return display; }
    }
    
    private String patternId;
    private String patternName;
    private PatternLifecycle currentStage;
    private PatternLifecycle previousStage;
    private int activeToolsCount;
    private Map<String, Double> toolContributions;
    private LocalDateTime firstDetected;
    private LocalDateTime lastUpdated;
    private LocalDateTime stageChangedAt;
    private int windowsActive;
    private int failureAttempts;
    private double peakProgress;
    private String failureReason;
    private boolean isTradeReady;
    private double entryPrice;
    private double targetPrice;
    private double stopLoss;
    
    public PatternCarryState(String patternId, String patternName) {
        this.patternId = patternId;
        this.patternName = patternName;
        this.currentStage = PatternLifecycle.INITIATING;
        this.previousStage = null;
        this.activeToolsCount = 0;
        this.toolContributions = new HashMap<>();
        this.firstDetected = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.stageChangedAt = LocalDateTime.now();
        this.windowsActive = 1;
        this.failureAttempts = 0;
        this.peakProgress = 10;
        this.isTradeReady = false;
    }
    
    /**
     * تحديث حالة النمط بناءً على البيانات الجديدة
     */
    public void updateWithSnapshot(MarketSnapshot snapshot, Map<String, Double> toolResults) {
        // تحديث البيانات المجمعة
        cumulativeVolume += snapshot.getVolume();
        averagePrice = ((averagePrice * dataPoints) + snapshot.getPrice()) / (dataPoints + 1);
        dataPoints++;
        
        // تحديث المؤشرات
        peakCVD = Math.max(peakCVD, Math.abs(snapshot.getCvd()));
        totalAbsorption += snapshot.getAbsorptionMeasure();
        icebergVolume += snapshot.getLargeLotActivity() * snapshot.getVolume();
        
        // تحديث الأدوات المفعلة
        for (Map.Entry<String, Double> entry : toolResults.entrySet()) {
            if (entry.getValue() > 0.6) { // أداة قوية
                activatedTools.add(entry.getKey());
                toolScores.put(entry.getKey(), entry.getValue());
            }
        }
        
        // حساب التقدم الجديد
        calculateProgress();
        lastUpdateTime = LocalDateTime.now();
    }
    
    /**
     * حساب تقدم النمط بناءً على الأدوات المفعلة والمرحلة
     */
    private void calculateProgress() {
        int activeToolCount = activatedTools.size();
        
        // المرحلة 1: تجميع جزئي (2-3 أدوات = 30%)
        if (activeToolCount >= 2 && stage == 0) {
            stage = 1;
            currentProgress = 30;
        }
        // المرحلة 2: تلاعب (4-6 أدوات = 60%)
        else if (activeToolCount >= 4 && stage == 1) {
            stage = 2;
            currentProgress = 60;
        }
        // المرحلة 3: توزيع (7-9 أدوات = 85%)
        else if (activeToolCount >= 7 && stage == 2) {
            stage = 3;
            currentProgress = 85;
        }
        // المرحلة 4: اكتمال (10+ أدوات = 100%)
        else if (activeToolCount >= 10 && stage == 3) {
            stage = 4;
            currentProgress = 100;
        }
        
        updateStageDescription();
    }
    
    /**
     * تحديث وصف المرحلة
     */
    private void updateStageDescription() {
        switch (currentStage) {
            case INITIATING:
                stageDescription = "Initial Detection";
                break;
            case FORMING:
                stageDescription = "Partial Accumulation";
                break;
            case DEVELOPING:
                stageDescription = "Manipulation Phase";
                break;
            case MATURING:
                stageDescription = "Distribution Phase";
                break;
            case CONFIRMING:
                stageDescription = "Final Confirmation Phase";
                break;
            case COMPLETED:
                stageDescription = "Pattern Complete";
                break;
            case FAILED:
                stageDescription = "Pattern Failed: " + (failureReason != null ? failureReason : "Unknown");
                break;
            case INVALIDATED:
                stageDescription = "Pattern Invalidated: " + (failureReason != null ? failureReason : "Market conditions");
                break;
            case SUCCESSFUL:
                stageDescription = "Pattern Successful";
                break;
        }
    }
    
    /**
     * هل النمط مكتمل؟
     */
    public boolean isComplete() {
        return currentProgress >= 100;
    }
    
    /**
     * هل النمط منتهي الصلاحية؟ (أكثر من ساعة)
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(startTime.plusHours(1));
    }
    
    // Getters
    public String getPatternId() { return patternId; }
    public String getPatternName() { return patternName; }
    public PatternLifecycle getCurrentStage() { return currentStage; }
    public PatternLifecycle getPreviousStage() { return previousStage; }
    public int getActiveToolsCount() { return activeToolsCount; }
    public Map<String, Double> getToolContributions() { return new HashMap<>(toolContributions); }
    public LocalDateTime getFirstDetected() { return firstDetected; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public LocalDateTime getStageChangedAt() { return stageChangedAt; }
    public int getWindowsActive() { return windowsActive; }
    public double getPeakProgress() { return peakProgress; }
    public boolean isTradeReady() { return isTradeReady; }
    public double getEntryPrice() { return entryPrice; }
    public double getTargetPrice() { return targetPrice; }
    public double getStopLoss() { return stopLoss; }
    
    public void incrementWindowsActive() { this.windowsActive++; }
    
    @Override
    public String toString() {
        return String.format("%s: %.1f%% (%s) - %d tools active", 
                           patternName, currentProgress, stageDescription, activatedTools.size());
    }
} 