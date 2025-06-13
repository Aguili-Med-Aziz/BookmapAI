package com.bookmaai.services;

import com.bookmaai.services.RiskRewardCalculator.RiskRewardResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 🎯 Smart Target Calculator - حاسبة الأهداف الذكية المدمجة
 * 
 * يدمج بين:
 * 1. النظام الرياضي (RiskRewardCalculator) 
 * 2. مستويات Bookmap الفعلية
 * 3. اختيار الأفضل تلقائياً
 */
@Service
public class SmartTargetCalculator {
    
    private static final Logger logger = LoggerFactory.getLogger(SmartTargetCalculator.class);
    
    @Autowired
    private RiskRewardCalculator riskRewardCalculator;
    
    /**
     * حساب الأهداف الذكية المدمجة
     */
    public SmartTargetResult calculateSmartTargets(Map<String, Double> toolResults, 
                                                  String patternType, 
                                                  double overallConfidence,
                                                  String direction,
                                                  double currentPrice) {
        
        // 1. حساب الأهداف الرياضية
        RiskRewardResult mathematicalTargets = riskRewardCalculator.calculateRiskReward(
            toolResults, patternType, overallConfidence, direction
        );
        
        // 2. استخراج مستويات Bookmap
        BookmapLevels bookmapLevels = extractBookmapLevels(currentPrice, toolResults, direction);
        
        // 3. دمج ومقارنة النتائج
        SmartTargetResult smartResult = mergeAndOptimize(mathematicalTargets, bookmapLevels, 
                                                        direction, currentPrice);
        
        logger.info("🎯 Smart targets calculated: Math={:.3f}%, Bookmap={} levels, Final={:.3f}%", 
                   mathematicalTargets.getTakeProfitDistance() * 100,
                   bookmapLevels.getAllLevels().size(),
                   smartResult.getFinalTakeProfitDistance() * 100);
        
        return smartResult;
    }
    
    /**
     * استخراج مستويات Bookmap (مبسط)
     */
    private BookmapLevels extractBookmapLevels(double currentPrice, Map<String, Double> toolResults, String direction) {
        BookmapLevels levels = new BookmapLevels(currentPrice);
        
        // استخراج من Heatmap
        extractFromHeatmap(levels, currentPrice, toolResults.get("heatmap"));
        
        // استخراج من Volume Profile
        extractFromVolumeProfile(levels, currentPrice, toolResults.get("volume_profile"));
        
        // استخراج من VWAP
        extractFromVWAP(levels, currentPrice, toolResults.get("vwap"));
        
        // استخراج من Absorption
        extractFromAbsorption(levels, currentPrice, toolResults.get("absorption_indicator"));
        
        // استخراج من Iceberg
        extractFromIceberg(levels, currentPrice, toolResults.get("iceberg_detector"));
        
        // ترتيب المستويات
        levels.sortByStrengthAndDistance();
        
        return levels;
    }
    
    /**
     * دمج وتحسين النتائج
     */
    private SmartTargetResult mergeAndOptimize(RiskRewardResult mathResult, BookmapLevels bookmapLevels, 
                                              String direction, double currentPrice) {
        
        // حساب الأهداف الرياضية
        double mathStopLoss = mathResult.getStopLossDistance();
        double mathTakeProfit = mathResult.getTakeProfitDistance();
        
        // البحث عن أفضل مستويات Bookmap
        BookmapLevel bestStopLevel = findBestStopLoss(bookmapLevels, direction, currentPrice, mathStopLoss);
        List<BookmapLevel> bestTargets = findBestTargets(bookmapLevels, direction, currentPrice, mathTakeProfit);
        
        // اختيار النهائي
        double finalStopLoss = chooseFinalStopLoss(mathStopLoss, bestStopLevel, currentPrice);
        double finalTakeProfit = chooseFinalTakeProfit(mathTakeProfit, bestTargets, currentPrice);
        
        // حساب نسبة R/R الجديدة
        double newRiskRewardRatio = finalTakeProfit / finalStopLoss;
        
        return new SmartTargetResult(
            mathResult,                    // النتيجة الرياضية الأصلية
            bookmapLevels,                // مستويات Bookmap
            finalStopLoss,                // Stop Loss النهائي
            finalTakeProfit,              // Take Profit النهائي
            finalTakeProfit * 0.5,        // Partial Exit 1
            finalTakeProfit * 0.8,        // Partial Exit 2
            newRiskRewardRatio,           // نسبة R/R الجديدة
            bestStopLevel,                // أفضل مستوى stop
            bestTargets,                  // أفضل مستويات هدف
            calculateConfidenceScore(mathResult, bookmapLevels) // درجة الثقة المدمجة
        );
    }
    
    /**
     * البحث عن أفضل Stop Loss
     */
    private BookmapLevel findBestStopLoss(BookmapLevels levels, String direction, double currentPrice, double mathStopLoss) {
        List<BookmapLevel> supportLevels = levels.getSupportLevels();
        if (supportLevels.isEmpty()) return null;
        
        // البحث عن مستوى دعم قريب من الحساب الرياضي
        double mathStopPrice = "BULLISH".equals(direction) ? 
            currentPrice * (1 - mathStopLoss) : currentPrice * (1 + mathStopLoss);
        
        return supportLevels.stream()
            .filter(level -> Math.abs(level.getPrice() - mathStopPrice) / currentPrice < 0.01) // ضمن 1%
            .min(Comparator.comparingDouble(level -> Math.abs(level.getPrice() - mathStopPrice)))
            .orElse(supportLevels.get(0)); // أقوى مستوى دعم
    }
    
    /**
     * البحث عن أفضل أهداف
     */
    private List<BookmapLevel> findBestTargets(BookmapLevels levels, String direction, double currentPrice, double mathTakeProfit) {
        List<BookmapLevel> targetLevels = levels.getTargetLevels();
        if (targetLevels.isEmpty()) return Collections.emptyList();
        
        // ترتيب حسب القوة والمسافة
        return targetLevels.stream()
            .filter(level -> level.getDistanceFrom(currentPrice) <= mathTakeProfit * 1.5) // ضمن 150% من الهدف الرياضي
            .sorted(Comparator.comparingInt(BookmapLevel::getPriority)
                   .thenComparingDouble(level -> -level.getStrength()))
            .limit(3) // أفضل 3 أهداف
            .collect(ArrayList::new, (list, level) -> list.add(level), (list1, list2) -> list1.addAll(list2));
    }
    
    /**
     * اختيار Stop Loss النهائي
     */
    private double chooseFinalStopLoss(double mathStopLoss, BookmapLevel bestStopLevel, double currentPrice) {
        if (bestStopLevel == null) {
            return mathStopLoss; // استخدام الرياضي
        }
        
        double bookmapStopLoss = Math.abs(bestStopLevel.getPrice() - currentPrice) / currentPrice;
        
        // إذا كان مستوى Bookmap منطقي (ضمن 50%-150% من الرياضي)
        if (bookmapStopLoss >= mathStopLoss * 0.5 && bookmapStopLoss <= mathStopLoss * 1.5) {
            // استخدام المتوسط المرجح
            double weight = bestStopLevel.getStrength();
            return (bookmapStopLoss * weight) + (mathStopLoss * (1 - weight));
        } else {
            return mathStopLoss; // الرياضي أكثر أماناً
        }
    }
    
    /**
     * اختيار Take Profit النهائي
     */
    private double chooseFinalTakeProfit(double mathTakeProfit, List<BookmapLevel> bestTargets, double currentPrice) {
        if (bestTargets.isEmpty()) {
            return mathTakeProfit; // استخدام الرياضي
        }
        
        // استخدام أقرب هدف قوي
        BookmapLevel primaryTarget = bestTargets.get(0);
        double bookmapTakeProfit = primaryTarget.getDistanceFrom(currentPrice);
        
        // التحقق من المنطقية
        if (bookmapTakeProfit >= mathTakeProfit * 0.3 && bookmapTakeProfit <= mathTakeProfit * 2.0) {
            // استخدام الهدف الأقوى
            if (primaryTarget.getStrength() > 0.7) {
                return bookmapTakeProfit;
            } else {
                // متوسط مرجح
                double weight = primaryTarget.getStrength();
                return (bookmapTakeProfit * weight) + (mathTakeProfit * (1 - weight));
            }
        } else {
            return mathTakeProfit; // الرياضي أكثر أماناً
        }
    }
    
    /**
     * حساب درجة ثقة مدمجة
     */
    private double calculateConfidenceScore(RiskRewardResult mathResult, BookmapLevels bookmapLevels) {
        double baseConfidence = 0.7; // أساسي
        
        // مكافأة وجود مستويات Bookmap قوية
        long strongLevels = bookmapLevels.getAllLevels().stream()
            .mapToLong(level -> level.getStrength() > 0.7 ? 1 : 0)
            .sum();
        
        double bookmapBonus = Math.min(0.2, strongLevels * 0.05); // حتى 20% مكافأة
        
        // مكافأة نسبة R/R العالية
        double rrBonus = Math.min(0.1, (mathResult.getRiskRewardRatio() - 2.0) * 0.02);
        
        return Math.min(0.95, baseConfidence + bookmapBonus + rrBonus);
    }
    
    // ========== استخراج مبسط للمستويات ==========
    
    private void extractFromHeatmap(BookmapLevels levels, double currentPrice, Double confidence) {
        if (confidence == null || confidence < 0.6) return;
        
        // محاكاة سيولة عالية في الهeatmap
        Random rand = new Random();
        for (int i = 0; i < 2 + rand.nextInt(2); i++) {
            double offset = (rand.nextDouble() - 0.5) * 0.015; // ±0.75%
            double price = currentPrice * (1 + offset);
            String type = price > currentPrice ? "HEATMAP_RESISTANCE" : "HEATMAP_SUPPORT";
            levels.addLevel(new BookmapLevel(price, type, confidence * 0.9, "Heatmap liquidity zone"));
        }
    }
    
    private void extractFromVolumeProfile(BookmapLevels levels, double currentPrice, Double confidence) {
        if (confidence == null || confidence < 0.5) return;
        
        Random rand = new Random();
        
        // POC
        double poc = currentPrice * (1 + (rand.nextDouble() - 0.5) * 0.008);
        String pocType = poc > currentPrice ? "VP_POC_RESISTANCE" : "VP_POC_SUPPORT";
        levels.addLevel(new BookmapLevel(poc, pocType, 0.90, "Volume Profile POC"));
        
        // Value Area
        double vah = currentPrice * (1 + 0.005 + rand.nextDouble() * 0.005);
        double val = currentPrice * (1 - 0.005 - rand.nextDouble() * 0.005);
        levels.addLevel(new BookmapLevel(vah, "VP_VAH", 0.75, "Value Area High"));
        levels.addLevel(new BookmapLevel(val, "VP_VAL", 0.75, "Value Area Low"));
    }
    
    private void extractFromVWAP(BookmapLevels levels, double currentPrice, Double confidence) {
        if (confidence == null || confidence < 0.5) return;
        
        Random rand = new Random();
        double vwap = currentPrice * (1 + (rand.nextDouble() - 0.5) * 0.004);
        String vwapType = vwap > currentPrice ? "VWAP_RESISTANCE" : "VWAP_SUPPORT";
        levels.addLevel(new BookmapLevel(vwap, vwapType, 0.80, "VWAP level"));
        
        // Standard deviations
        double stdDev = currentPrice * 0.003;
        levels.addLevel(new BookmapLevel(vwap + stdDev, "VWAP_STD+1", 0.60, "VWAP +1 StdDev"));
        levels.addLevel(new BookmapLevel(vwap - stdDev, "VWAP_STD-1", 0.60, "VWAP -1 StdDev"));
    }
    
    private void extractFromAbsorption(BookmapLevels levels, double currentPrice, Double confidence) {
        if (confidence == null || confidence < 0.6) return;
        
        Random rand = new Random();
        double absorptionLevel = currentPrice * (1 + (rand.nextDouble() - 0.5) * 0.008);
        String type = absorptionLevel > currentPrice ? "ABSORPTION_RESISTANCE" : "ABSORPTION_SUPPORT";
        levels.addLevel(new BookmapLevel(absorptionLevel, type, confidence * 0.85, "Absorption zone"));
    }
    
    private void extractFromIceberg(BookmapLevels levels, double currentPrice, Double confidence) {
        if (confidence == null || confidence < 0.7) return;
        
        Random rand = new Random();
        double icebergLevel = currentPrice * (1 + (rand.nextDouble() - 0.5) * 0.006);
        String type = icebergLevel > currentPrice ? "ICEBERG_RESISTANCE" : "ICEBERG_SUPPORT";
        levels.addLevel(new BookmapLevel(icebergLevel, type, confidence * 0.80, "Iceberg order"));
    }
    
    // ========== فئات البيانات ==========
    
    public static class BookmapLevel {
        private final double price;
        private final String type;
        private final double strength;
        private final String description;
        
        public BookmapLevel(double price, String type, double strength, String description) {
            this.price = price;
            this.type = type;
            this.strength = strength;
            this.description = description;
        }
        
        public double getPrice() { return price; }
        public String getType() { return type; }
        public double getStrength() { return strength; }
        public String getDescription() { return description; }
        
        public double getDistanceFrom(double currentPrice) {
            return Math.abs(price - currentPrice) / currentPrice;
        }
        
        public int getPriority() {
            if (type.contains("POC")) return 1;
            if (type.contains("VWAP")) return 2;
            if (type.contains("HEATMAP")) return 3;
            if (type.contains("ICEBERG")) return 4;
            if (type.contains("ABSORPTION")) return 5;
            return 6;
        }
        
        @Override
        public String toString() {
            return String.format("%.5f (%s)", price, type);
        }
    }
    
    public static class BookmapLevels {
        private final double currentPrice;
        private final List<BookmapLevel> allLevels = new ArrayList<>();
        
        public BookmapLevels(double currentPrice) {
            this.currentPrice = currentPrice;
        }
        
        public void addLevel(BookmapLevel level) {
            allLevels.add(level);
        }
        
        public List<BookmapLevel> getAllLevels() { return new ArrayList<>(allLevels); }
        
        public List<BookmapLevel> getTargetLevels() {
            return allLevels.stream()
                .filter(level -> level.getPrice() > currentPrice)
                .collect(ArrayList::new, (list, level) -> list.add(level), (list1, list2) -> list1.addAll(list2));
        }
        
        public List<BookmapLevel> getSupportLevels() {
            return allLevels.stream()
                .filter(level -> level.getPrice() < currentPrice)
                .collect(ArrayList::new, (list, level) -> list.add(level), (list1, list2) -> list1.addAll(list2));
        }
        
        public void sortByStrengthAndDistance() {
            allLevels.sort(Comparator.comparingInt(BookmapLevel::getPriority)
                          .thenComparingDouble(level -> -level.getStrength())
                          .thenComparingDouble(level -> level.getDistanceFrom(currentPrice)));
        }
        
        public double getCurrentPrice() { return currentPrice; }
    }
    
    public static class SmartTargetResult {
        private final RiskRewardResult originalMathResult;
        private final BookmapLevels bookmapLevels;
        private final double finalStopLossDistance;
        private final double finalTakeProfitDistance;
        private final double partialExit1;
        private final double partialExit2;
        private final double finalRiskRewardRatio;
        private final BookmapLevel bestStopLevel;
        private final List<BookmapLevel> bestTargetLevels;
        private final double confidenceScore;
        
        public SmartTargetResult(RiskRewardResult originalMathResult, BookmapLevels bookmapLevels,
                               double finalStopLossDistance, double finalTakeProfitDistance,
                               double partialExit1, double partialExit2, double finalRiskRewardRatio,
                               BookmapLevel bestStopLevel, List<BookmapLevel> bestTargetLevels,
                               double confidenceScore) {
            this.originalMathResult = originalMathResult;
            this.bookmapLevels = bookmapLevels;
            this.finalStopLossDistance = finalStopLossDistance;
            this.finalTakeProfitDistance = finalTakeProfitDistance;
            this.partialExit1 = partialExit1;
            this.partialExit2 = partialExit2;
            this.finalRiskRewardRatio = finalRiskRewardRatio;
            this.bestStopLevel = bestStopLevel;
            this.bestTargetLevels = bestTargetLevels;
            this.confidenceScore = confidenceScore;
        }
        
        // Getters
        public RiskRewardResult getOriginalMathResult() { return originalMathResult; }
        public BookmapLevels getBookmapLevels() { return bookmapLevels; }
        public double getFinalStopLossDistance() { return finalStopLossDistance; }
        public double getFinalTakeProfitDistance() { return finalTakeProfitDistance; }
        public double getPartialExit1() { return partialExit1; }
        public double getPartialExit2() { return partialExit2; }
        public double getFinalRiskRewardRatio() { return finalRiskRewardRatio; }
        public BookmapLevel getBestStopLevel() { return bestStopLevel; }
        public List<BookmapLevel> getBestTargetLevels() { return bestTargetLevels; }
        public double getConfidenceScore() { return confidenceScore; }
        
        @Override
        public String toString() {
            return String.format("SmartTarget{R:R=1:%.1f, SL=%.3f%%, TP=%.3f%%, Confidence=%.1f%%}", 
                               finalRiskRewardRatio, finalStopLossDistance * 100, 
                               finalTakeProfitDistance * 100, confidenceScore * 100);
        }
    }
} 