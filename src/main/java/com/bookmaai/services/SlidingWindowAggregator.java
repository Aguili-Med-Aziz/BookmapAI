package com.bookmaai.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 🔄 مجمع النافذة المتحركة - يجمع البيانات في نوافذ زمنية ثابتة
 */
public class SlidingWindowAggregator {
    
    private static final Logger logger = LoggerFactory.getLogger(SlidingWindowAggregator.class);
    
    private final int windowSizeMinutes;
    private final List<MarketSnapshot> currentWindow;
    private LocalDateTime windowStartTime;
    private boolean isWindowActive;
    
    // المؤشرات المجمعة
    private double totalAbsorption;
    private double peakCVD;
    private double totalIcebergVolume;
    private int snapshotCount;
    
    public SlidingWindowAggregator(int windowSizeMinutes) {
        this.windowSizeMinutes = windowSizeMinutes;
        this.currentWindow = new ArrayList<>();
        this.isWindowActive = false;
        resetAggregates();
    }
    
    /**
     * إضافة لقطة جديدة للنافذة
     */
    public void addSnapshot(MarketSnapshot snapshot) {
        if (!isWindowActive) {
            startNewWindow(snapshot.timestamp);
        }
        
        currentWindow.add(snapshot);
        updateAggregates(snapshot);
        snapshotCount++;
        
        logger.debug("📊 Added snapshot to window: {}", snapshot.timestamp);
    }
    
    /**
     * التحقق من جاهزية النافذة للإنهاء
     */
    public boolean isWindowReady() {
        if (!isWindowActive || windowStartTime == null) {
            return false;
        }
        
        LocalDateTime windowEndTime = windowStartTime.plusMinutes(windowSizeMinutes);
        return LocalDateTime.now().isAfter(windowEndTime);
    }
    
    /**
     * إنهاء النافذة وإنشاء الملخص
     */
    public WindowSummary finalizeWindow(String patternName) {
        if (!isWindowActive) {
            return null;
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        
        WindowSummary summary = new WindowSummary(
            windowStartTime,
            endTime,
            patternName,
            snapshotCount,
            totalAbsorption,
            peakCVD,
            totalIcebergVolume,
            calculateAverageVolume(),
            calculatePriceRange()
        );
        
        // إعادة تعيين النافذة
        resetWindow();
        
        logger.info("✅ Window finalized: {} to {} | Pattern: {} | Snapshots: {}", 
                   windowStartTime, endTime, patternName, snapshotCount);
        
        return summary;
    }
    
    /**
     * بدء نافذة جديدة
     */
    private void startNewWindow(LocalDateTime startTime) {
        this.windowStartTime = startTime;
        this.isWindowActive = true;
        resetAggregates();
        currentWindow.clear();
        
        logger.info("🔄 New window started at: {}", startTime);
    }
    
    /**
     * تحديث المؤشرات المجمعة
     */
    private void updateAggregates(MarketSnapshot snapshot) {
        totalAbsorption += snapshot.absorptionMeasure;
        
        // تحديث أعلى CVD
        if (Math.abs(snapshot.cvd) > Math.abs(peakCVD)) {
            peakCVD = snapshot.cvd;
        }
        
        totalIcebergVolume += snapshot.icebergVolume;
    }
    
    /**
     * إعادة تعيين المؤشرات المجمعة
     */
    private void resetAggregates() {
        totalAbsorption = 0.0;
        peakCVD = 0.0;
        totalIcebergVolume = 0.0;
        snapshotCount = 0;
    }
    
    /**
     * إعادة تعيين النافذة
     */
    private void resetWindow() {
        isWindowActive = false;
        windowStartTime = null;
        currentWindow.clear();
        resetAggregates();
    }
    
    /**
     * حساب متوسط الحجم
     */
    private double calculateAverageVolume() {
        if (currentWindow.isEmpty()) {
            return 0.0;
        }
        
        return currentWindow.stream()
                .mapToDouble(s -> s.volume)
                .average()
                .orElse(0.0);
    }
    
    /**
     * حساب نطاق السعر
     */
    private double calculatePriceRange() {
        if (currentWindow.isEmpty()) {
            return 0.0;
        }
        
        double maxPrice = currentWindow.stream()
                .mapToDouble(s -> Math.max(s.bidPrice, s.askPrice))
                .max()
                .orElse(0.0);
                
        double minPrice = currentWindow.stream()
                .mapToDouble(s -> Math.min(s.bidPrice, s.askPrice))
                .min()
                .orElse(0.0);
        
        return maxPrice - minPrice;
    }
    
    // Getters
    public int getWindowSizeMinutes() { return windowSizeMinutes; }
    public boolean isWindowActive() { return isWindowActive; }
    public int getCurrentSnapshotCount() { return snapshotCount; }
    public double getTotalAbsorption() { return totalAbsorption; }
    public double getPeakCVD() { return peakCVD; }
    public double getTotalIcebergVolume() { return totalIcebergVolume; }
    
    /**
     * الحصول على إجمالي الحجم في النافذة الحالية
     */
    public double getTotalVolume() {
        return currentWindow.stream()
                .mapToDouble(s -> s.volume)
                .sum();
    }
    
    /**
     * 📊 لقطة السوق - بيانات نقطة زمنية واحدة
     */
    public static class MarketSnapshot {
        public final LocalDateTime timestamp;
        public final double bidPrice;
        public final double askPrice;
        public final double volume;
        public final double absorptionMeasure;
        public final double cvd;
        public final double icebergVolume;
        
        public MarketSnapshot(LocalDateTime timestamp, double bidPrice, double askPrice, 
                            double volume, double absorptionMeasure, double cvd, double icebergVolume) {
            this.timestamp = timestamp;
            this.bidPrice = bidPrice;
            this.askPrice = askPrice;
            this.volume = volume;
            this.absorptionMeasure = absorptionMeasure;
            this.cvd = cvd;
            this.icebergVolume = icebergVolume;
        }
        
        @Override
        public String toString() {
            return String.format("MarketSnapshot[%s: bid=%.5f, ask=%.5f, vol=%.2f, abs=%.3f, cvd=%.1f, ice=%.1f]",
                    timestamp, bidPrice, askPrice, volume, absorptionMeasure, cvd, icebergVolume);
        }
    }
}
