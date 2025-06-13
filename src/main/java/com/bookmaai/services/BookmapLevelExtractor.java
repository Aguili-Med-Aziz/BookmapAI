package com.bookmaai.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 🎯 Bookmap Level Extractor - مستخرج مستويات Bookmap الفعلية
 * 
 * يستخرج مستويات الأهداف الحقيقية من:
 * - Heatmap: مناطق السيولة العالية
 * - Volume Profile: POC, HVN, Value Area
 * - VWAP: مستويات مغناطيسية  
 * - Absorption: مناطق قوية
 * - Iceberg: سيولة مخفية
 */
@Service
public class BookmapLevelExtractor {
    
    private static final Logger logger = LoggerFactory.getLogger(BookmapLevelExtractor.class);
    
    /**
     * استخراج جميع المستويات المهمة
     */
    public BookmapLevels extractLevels(double currentPrice, Map<String, Double> toolResults, 
                                     String direction, MarketData marketData) {
        
        BookmapLevels levels = new BookmapLevels(currentPrice);
        
        // 1. استخراج مستويات من Heatmap
        extractHeatmapLevels(levels, marketData, toolResults.get("heatmap"));
        
        // 2. استخراج مستويات من Volume Profile  
        extractVolumeProfileLevels(levels, marketData, toolResults.get("volume_profile"));
        
        // 3. استخراج مستويات من VWAP
        extractVWAPLevels(levels, marketData, toolResults.get("vwap"));
        
        // 4. استخراج مستويات من Absorption
        extractAbsorptionLevels(levels, marketData, toolResults.get("absorption_indicator"));
        
        // 5. استخراج مستويات من Iceberg
        extractIcebergLevels(levels, marketData, toolResults.get("iceberg_detector"));
        
        // 6. تقييم وترتيب المستويات
        levels.evaluateAndRank(direction);
        
        logger.info("🎯 Extracted {} target levels and {} support levels", 
                   levels.getTargetLevels().size(), levels.getSupportLevels().size());
        
        return levels;
    }
    
    /**
     * استخراج مستويات من Heatmap - السيولة العالية
     */
    private void extractHeatmapLevels(BookmapLevels levels, MarketData marketData, Double confidence) {
        if (confidence == null || confidence < 0.6) return;
        
        // محاكاة استخراج مستويات السيولة من Heatmap
        double currentPrice = levels.getCurrentPrice();
        
        // مناطق السيولة العالية (Red zones في Heatmap)
        List<LiquidityZone> liquidityZones = simulateHeatmapData(currentPrice, confidence);
        
        for (LiquidityZone zone : liquidityZones) {
            if (zone.getPrice() > currentPrice) {
                // مستوى مقاومة/هدف
                levels.addTargetLevel(new BookmapLevel(
                    zone.getPrice(), 
                    "HEATMAP_RESISTANCE", 
                    zone.getStrength(),
                    "High liquidity zone in heatmap"
                ));
            } else {
                // مستوى دعم
                levels.addSupportLevel(new BookmapLevel(
                    zone.getPrice(),
                    "HEATMAP_SUPPORT",
                    zone.getStrength(),
                    "Strong support from heatmap liquidity"
                ));
            }
        }
    }
    
    /**
     * استخراج مستويات من Volume Profile
     */
    private void extractVolumeProfileLevels(BookmapLevels levels, MarketData marketData, Double confidence) {
        if (confidence == null || confidence < 0.5) return;
        
        double currentPrice = levels.getCurrentPrice();
        
        // محاكاة بيانات Volume Profile
        VolumeProfileData vpData = simulateVolumeProfileData(currentPrice, confidence);
        
        // POC (Point of Control) - أهم مستوى
        if (vpData.getPOC() != currentPrice) {
            String type = vpData.getPOC() > currentPrice ? "VP_POC_RESISTANCE" : "VP_POC_SUPPORT";
            levels.addLevel(vpData.getPOC(), new BookmapLevel(
                vpData.getPOC(),
                type,
                0.90, // POC دائماً قوي
                "Volume Profile Point of Control"
            ));
        }
        
        // Value Area High & Low
        if (vpData.getValueAreaHigh() > currentPrice) {
            levels.addTargetLevel(new BookmapLevel(
                vpData.getValueAreaHigh(),
                "VP_VAH",
                0.75,
                "Volume Profile Value Area High"
            ));
        }
        
        if (vpData.getValueAreaLow() < currentPrice) {
            levels.addSupportLevel(new BookmapLevel(
                vpData.getValueAreaLow(),
                "VP_VAL", 
                0.75,
                "Volume Profile Value Area Low"
            ));
        }
        
        // HVN (High Volume Nodes)
        for (Double hvn : vpData.getHVNs()) {
            String type = hvn > currentPrice ? "VP_HVN_RESISTANCE" : "VP_HVN_SUPPORT";
            levels.addLevel(hvn, new BookmapLevel(
                hvn,
                type,
                0.70,
                "High Volume Node"
            ));
        }
    }
    
    /**
     * استخراج مستويات من VWAP
     */
    private void extractVWAPLevels(BookmapLevels levels, MarketData marketData, Double confidence) {
        if (confidence == null || confidence < 0.5) return;
        
        double currentPrice = levels.getCurrentPrice();
        
        // محاكاة بيانات VWAP
        VWAPData vwapData = simulateVWAPData(currentPrice, confidence);
        
        // VWAP نفسه
        if (Math.abs(vwapData.getVwap() - currentPrice) > 0.0005) {
            String type = vwapData.getVwap() > currentPrice ? "VWAP_RESISTANCE" : "VWAP_SUPPORT";
            levels.addLevel(vwapData.getVwap(), new BookmapLevel(
                vwapData.getVwap(),
                type,
                0.80,
                "Volume Weighted Average Price"
            ));
        }
        
        // VWAP Standard Deviations
        for (int i = 1; i <= 2; i++) {
            double upperBand = vwapData.getVwap() + (vwapData.getStdDev() * i);
            double lowerBand = vwapData.getVwap() - (vwapData.getStdDev() * i);
            
            if (upperBand > currentPrice) {
                levels.addTargetLevel(new BookmapLevel(
                    upperBand,
                    "VWAP_STD+" + i,
                    Math.max(0.60 - (i * 0.1), 0.4),
                    "VWAP +" + i + " Standard Deviation"
                ));
            }
            
            if (lowerBand < currentPrice) {
                levels.addSupportLevel(new BookmapLevel(
                    lowerBand,
                    "VWAP_STD-" + i,
                    Math.max(0.60 - (i * 0.1), 0.4),
                    "VWAP -" + i + " Standard Deviation"
                ));
            }
        }
    }
    
    /**
     * استخراج مستويات من Absorption Indicator
     */
    private void extractAbsorptionLevels(BookmapLevels levels, MarketData marketData, Double confidence) {
        if (confidence == null || confidence < 0.6) return;
        
        // محاكاة مناطق الامتصاص القوية
        List<Double> absorptionLevels = simulateAbsorptionLevels(levels.getCurrentPrice(), confidence);
        
        for (Double level : absorptionLevels) {
            String type = level > levels.getCurrentPrice() ? "ABSORPTION_RESISTANCE" : "ABSORPTION_SUPPORT";
            levels.addLevel(level, new BookmapLevel(
                level,
                type,
                confidence * 0.9, // قوة الامتصاص مرتبطة بالثقة
                "Strong absorption zone"
            ));
        }
    }
    
    /**
     * استخراج مستويات من Iceberg Detector
     */
    private void extractIcebergLevels(BookmapLevels levels, MarketData marketData, Double confidence) {
        if (confidence == null || confidence < 0.7) return;
        
        // محاكاة مستويات الأوامر المخفية
        List<Double> icebergLevels = simulateIcebergLevels(levels.getCurrentPrice(), confidence);
        
        for (Double level : icebergLevels) {
            String type = level > levels.getCurrentPrice() ? "ICEBERG_RESISTANCE" : "ICEBERG_SUPPORT";
            levels.addLevel(level, new BookmapLevel(
                level,
                type,
                confidence * 0.85, // أوامر مخفية قوية
                "Hidden iceberg order level"
            ));
        }
    }
    
    // ========== محاكاة البيانات ==========
    
    private List<LiquidityZone> simulateHeatmapData(double currentPrice, double confidence) {
        List<LiquidityZone> zones = new ArrayList<>();
        Random rand = new Random();
        
        // إنشاء 3-5 مناطق سيولة
        int numZones = 3 + rand.nextInt(3);
        for (int i = 0; i < numZones; i++) {
            double offset = (rand.nextDouble() - 0.5) * 0.02; // ±1%
            double price = currentPrice * (1 + offset);
            double strength = 0.6 + (rand.nextDouble() * 0.3); // 60-90%
            zones.add(new LiquidityZone(price, strength));
        }
        
        return zones;
    }
    
    private VolumeProfileData simulateVolumeProfileData(double currentPrice, double confidence) {
        Random rand = new Random();
        
        // POC قريب من السعر الحالي
        double poc = currentPrice * (1 + (rand.nextDouble() - 0.5) * 0.01);
        
        // Value Area (70% من الحجم)
        double valueAreaRange = currentPrice * 0.008; // 0.8%
        double vah = poc + valueAreaRange;
        double val = poc - valueAreaRange;
        
        // HVNs
        List<Double> hvns = new ArrayList<>();
        for (int i = 0; i < 2 + rand.nextInt(3); i++) {
            double hvn = currentPrice * (1 + (rand.nextDouble() - 0.5) * 0.015);
            hvns.add(hvn);
        }
        
        return new VolumeProfileData(poc, vah, val, hvns);
    }
    
    private VWAPData simulateVWAPData(double currentPrice, double confidence) {
        Random rand = new Random();
        
        // VWAP قريب من السعر
        double vwap = currentPrice * (1 + (rand.nextDouble() - 0.5) * 0.005);
        
        // Standard Deviation
        double stdDev = currentPrice * (0.002 + rand.nextDouble() * 0.003); // 0.2%-0.5%
        
        return new VWAPData(vwap, stdDev);
    }
    
    private List<Double> simulateAbsorptionLevels(double currentPrice, double confidence) {
        List<Double> levels = new ArrayList<>();
        Random rand = new Random();
        
        // 1-3 مستويات امتصاص
        int numLevels = 1 + rand.nextInt(3);
        for (int i = 0; i < numLevels; i++) {
            double offset = (rand.nextDouble() - 0.5) * 0.01; // ±0.5%
            levels.add(currentPrice * (1 + offset));
        }
        
        return levels;
    }
    
    private List<Double> simulateIcebergLevels(double currentPrice, double confidence) {
        List<Double> levels = new ArrayList<>();
        Random rand = new Random();
        
        // 1-2 مستويات آيسبرج
        int numLevels = 1 + rand.nextInt(2);
        for (int i = 0; i < numLevels; i++) {
            double offset = (rand.nextDouble() - 0.5) * 0.008; // ±0.4%
            levels.add(currentPrice * (1 + offset));
        }
        
        return levels;
    }
    
    // ========== فئات البيانات المساعدة ==========
    
    public static class MarketData {
        // يمكن إضافة بيانات السوق الحقيقية هنا لاحقاً
    }
    
    private static class LiquidityZone {
        private final double price;
        private final double strength;
        
        public LiquidityZone(double price, double strength) {
            this.price = price;
            this.strength = strength;
        }
        
        public double getPrice() { return price; }
        public double getStrength() { return strength; }
    }
    
    private static class VolumeProfileData {
        private final double poc;
        private final double valueAreaHigh;
        private final double valueAreaLow;
        private final List<Double> hvns;
        
        public VolumeProfileData(double poc, double valueAreaHigh, double valueAreaLow, List<Double> hvns) {
            this.poc = poc;
            this.valueAreaHigh = valueAreaHigh;
            this.valueAreaLow = valueAreaLow;
            this.hvns = hvns;
        }
        
        public double getPOC() { return poc; }
        public double getValueAreaHigh() { return valueAreaHigh; }
        public double getValueAreaLow() { return valueAreaLow; }
        public List<Double> getHVNs() { return hvns; }
    }
    
    private static class VWAPData {
        private final double vwap;
        private final double stdDev;
        
        public VWAPData(double vwap, double stdDev) {
            this.vwap = vwap;
            this.stdDev = stdDev;
        }
        
        public double getVwap() { return vwap; }
        public double getStdDev() { return stdDev; }
    }
} 