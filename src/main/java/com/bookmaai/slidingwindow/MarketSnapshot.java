package com.bookmaai.slidingwindow;

import java.time.LocalDateTime;

/**
 * 📊 لقطة السوق في لحظة معينة
 * تحتوي على جميع البيانات المطلوبة للتحليل
 */
public class MarketSnapshot {
    
    private final String symbol;
    private final double price;
    private final double volume;
    private final double vwap;
    private final LocalDateTime timestamp;
    
    // مؤشرات إضافية
    private double bidVolume;
    private double askVolume;
    private double cvd; // Cumulative Volume Delta
    private double heatmapLevel;
    private double volumeProfile;
    
    // معلومات النمط
    private String patternName;
    private double patternProgress;
    
    public MarketSnapshot(String symbol, double price, double volume, double vwap) {
        this.symbol = symbol;
        this.price = price;
        this.volume = volume;
        this.vwap = vwap;
        this.timestamp = LocalDateTime.now();
    }
    
    // حساب CVD
    public void updateCVD(double bidVol, double askVol) {
        this.bidVolume = bidVol;
        this.askVolume = askVol;
        this.cvd = askVol - bidVol;
    }
    
    // Getters
    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public double getVolume() { return volume; }
    public double getVwap() { return vwap; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getCvd() { return cvd; }
    public double getHeatmapLevel() { return heatmapLevel; }
    public void setHeatmapLevel(double level) { this.heatmapLevel = level; }
    public double getVolumeProfile() { return volumeProfile; }
    public void setVolumeProfile(double profile) { this.volumeProfile = profile; }
    public String getPatternName() { return patternName; }
    public void setPatternName(String name) { this.patternName = name; }
    public double getPatternProgress() { return patternProgress; }
    public void setPatternProgress(double progress) { this.patternProgress = progress; }
    
    // مقاييس إضافية
    public double getAbsorptionMeasure() {
        // قياس الامتصاص: حجم كبير مع تغير سعر صغير
        double priceChange = Math.abs(price - vwap) / vwap;
        return volume > 1000 && priceChange < 0.001 ? volume / 1000.0 : 0.0;
    }
    
    public double getLargeLotActivity() {
        // نشاط الصفقات الكبيرة
        return volume > 2000 ? 1.0 : volume / 2000.0;
    }
} 