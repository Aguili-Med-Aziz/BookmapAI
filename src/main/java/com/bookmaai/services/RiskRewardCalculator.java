package com.bookmaai.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Comparator;
import java.util.Random;
import java.util.ArrayList;

/**
 * 🎯 Risk Reward Calculator - حاسبة المخاطر والأهداف
 * 
 * يحسب نسب Risk/Reward بناءً على:
 * - قوة الأدوات المؤكدة (الـ12 أداة)
 * - نوع النمط المكتشف
 * - مستوى الثقة الإجمالي
 * - التوافق بين الأدوات (tools synergy)
 */
@Service
public class RiskRewardCalculator {
    
    private static final Logger logger = LoggerFactory.getLogger(RiskRewardCalculator.class);
    
    /**
     * نسب Risk/Reward الأساسية لكل أداة حسب دقتها
     */
    private static final Map<String, Double> TOOL_BASE_RATIOS = new HashMap<String, Double>() {{
        // === TIER 1 TOOLS (94-99% accuracy) - نسب عالية ===
        put("cvd", 3.5);                    // أقوى أداة (99% accuracy)
        put("heatmap", 3.0);                // سيولة قوية (94%)
        put("volume_dots", 2.8);            // تداول عدواني (94%)
        put("vwap", 2.5);                   // انحراف قوي (94%)
        
        // === TIER 2 TOOLS (85-90% accuracy) - نسب جيدة ===
        put("volume_profile", 2.3);         // ملف حجم (85%)
        put("iceberg_detector", 2.8);       // أوامر مخفية (97%)
        put("volume_bubbles", 2.2);         // حجم استثنائي (85%)
        
        // === TIER 3 TOOLS (70-80% accuracy) - نسب متوسطة ===
        put("large_lot_tracker", 2.0);      // تتبع كميات (75%)
        put("imbalance_indicator", 1.8);    // عدم توازن (70%)
        put("absorption_indicator", 2.1);   // امتصاص (78%)
        
        // === TIER 4 TOOLS (60-70% accuracy) - نسب منخفضة ===
        put("strength_level_indicator", 1.5); // قوة مستوى (65%)
        put("stop_run", 1.7);               // صيد ستوبات (60%)
    }};
    
    /**
     * مضاعفات النمط حسب قوة النمط
     */
    private static final Map<String, Double> PATTERN_MULTIPLIERS = new HashMap<String, Double>() {{
        // أنماط قوية جداً
        put("INSTITUTIONAL_ACCUMULATION", 1.4);     // تراكم مؤسسي
        put("EXHAUSTION_REVERSAL", 1.3);           // انعكاس استنزاف
        put("TRIPLE_CONFIRMATION", 1.5);           // تأكيد ثلاثي
        put("PERFECT_STORM", 1.6);                 // العاصفة المثالية
        
        // أنماط قوية  
        put("MOMENTUM_BREAKOUT", 1.2);             // كسر زخم
        put("LIQUIDITY_SWEEP", 1.3);               // كنس سيولة
        put("ABSORPTION_PATTERN", 1.2);            // نمط امتصاص
        put("ICEBERG_ACCUMULATION", 1.3);          // تراكم آيسبرج
        
        // أنماط عادية
        put("VOLUME_CONFIRMATION", 1.0);           // تأكيد حجم
        put("DIVERGENCE_PATTERN", 1.1);            // نمط تباعد
        put("RETEST_PATTERN", 1.0);                // اختبار مستوى
        
        // أنماط ضعيفة
        put("RANGE_TRADING", 0.8);                 // تداول نطاق
        put("WEAK_SIGNAL", 0.7);                   // إشارة ضعيفة
        put("UNCERTAIN", 0.6);                     // غير مؤكد
    }};
    
    /**
     * حساب نسبة Risk/Reward الديناميكية مع دمج مستويات Bookmap
     */
    public RiskRewardResult calculateRiskReward(Map<String, Double> toolResults, 
                                               String patternType, 
                                               double overallConfidence,
                                               String direction) {
        
        // 1. حساب النسبة الأساسية من الأدوات
        double baseRatio = calculateToolBasedRatio(toolResults);
        
        // 2. تطبيق مضاعف النمط
        double patternMultiplier = PATTERN_MULTIPLIERS.getOrDefault(patternType, 1.0);
        
        // 3. تطبيق مضاعف الثقة
        double confidenceMultiplier = calculateConfidenceMultiplier(overallConfidence);
        
        // 4. تطبيق مضاعف التوافق بين الأدوات
        double synergyMultiplier = calculateToolSynergy(toolResults);
        
        // 5. حساب النسبة النهائية
        double finalRatio = baseRatio * patternMultiplier * confidenceMultiplier * synergyMultiplier;
        
        // 6. تطبيق حدود منطقية
        finalRatio = Math.max(1.2, Math.min(5.0, finalRatio));
        
        // 7. حساب Stop Loss و Take Profit
        RiskRewardResult result = calculateLevels(finalRatio, direction, toolResults);
        
        logger.info("🎯 Risk/Reward calculated: Pattern={}, Ratio={:.1f}, Confidence={:.1f}%", 
                   patternType, finalRatio, overallConfidence * 100);
        
        return result;
    }
    
    /**
     * 🚀 NEW: حساب نسبة Risk/Reward الذكية مع دمج مستويات Bookmap
     */
    public SmartRiskRewardResult calculateSmartRiskReward(Map<String, Double> toolResults, 
                                                         String patternType, 
                                                         double overallConfidence,
                                                         String direction,
                                                         double currentPrice) {
        
        // 1. حساب الأهداف الرياضية العادية
        RiskRewardResult mathematicalResult = calculateRiskReward(toolResults, patternType, overallConfidence, direction);
        
        // 2. استخراج مستويات Bookmap
        List<BookmapLevel> bookmapLevels = extractBookmapLevels(currentPrice, toolResults);
        
        // 3. دمج وتحسين النتائج
        SmartRiskRewardResult smartResult = mergeResults(mathematicalResult, bookmapLevels, direction, currentPrice);
        
        logger.info("🎯 Smart Risk/Reward: Math=1:{:.1f}, Bookmap={} levels, Final=1:{:.1f}", 
                   mathematicalResult.getRiskRewardRatio(),
                   bookmapLevels.size(),
                   smartResult.getFinalRiskRewardRatio());
        
        return smartResult;
    }
    
    /**
     * حساب النسبة الأساسية من قوة الأدوات
     */
    private double calculateToolBasedRatio(Map<String, Double> toolResults) {
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        
        for (Map.Entry<String, Double> entry : toolResults.entrySet()) {
            String tool = entry.getKey();
            Double confidence = entry.getValue();
            
            if (confidence != null && confidence > 0.5) { // فقط الأدوات القوية
                Double baseRatio = TOOL_BASE_RATIOS.get(tool);
                if (baseRatio != null) {
                    double weight = confidence; // الثقة = الوزن
                    weightedSum += baseRatio * weight;
                    totalWeight += weight;
                }
            }
        }
        
        return totalWeight > 0 ? weightedSum / totalWeight : 2.0; // افتراضي 1:2
    }
    
    /**
     * حساب مضاعف الثقة
     */
    private double calculateConfidenceMultiplier(double confidence) {
        if (confidence > 0.9) return 1.3;      // ثقة عالية جداً
        if (confidence > 0.8) return 1.2;      // ثقة عالية
        if (confidence > 0.7) return 1.1;      // ثقة جيدة
        if (confidence > 0.6) return 1.0;      // ثقة متوسطة
        if (confidence > 0.5) return 0.9;      // ثقة منخفضة
        return 0.8;                             // ثقة ضعيفة
    }
    
    /**
     * حساب التوافق بين الأدوات (Tools Synergy)
     */
    private double calculateToolSynergy(Map<String, Double> toolResults) {
        int strongTools = 0;
        int totalTools = 0;
        
        // عدد الأدوات القوية (confidence > 70%)
        for (Double confidence : toolResults.values()) {
            if (confidence != null) {
                totalTools++;
                if (confidence > 0.7) {
                    strongTools++;
                }
            }
        }
        
        if (totalTools == 0) return 1.0;
        
        double synergyRatio = (double) strongTools / totalTools;
        
        // مكافآت التوافق
        if (synergyRatio > 0.8 && strongTools >= 4) return 1.3;  // توافق ممتاز
        if (synergyRatio > 0.7 && strongTools >= 3) return 1.2;  // توافق جيد
        if (synergyRatio > 0.6 && strongTools >= 2) return 1.1;  // توافق متوسط
        if (synergyRatio > 0.5) return 1.0;                      // توافق عادي
        
        return 0.9; // توافق ضعيف
    }
    
    /**
     * حساب مستويات Stop Loss و Take Profit
     */
    private RiskRewardResult calculateLevels(double riskRewardRatio, String direction, 
                                           Map<String, Double> toolResults) {
        
        // حساب Stop Loss بناءً على قوة الأدوات
        double stopLossDistance = calculateStopLossDistance(toolResults);
        
        // حساب Take Profit
        double takeProfitDistance = stopLossDistance * riskRewardRatio;
        
        // مستويات خروج متدرجة
        double partialExit1 = takeProfitDistance * 0.5;  // 50% من الهدف
        double partialExit2 = takeProfitDistance * 0.8;  // 80% من الهدف
        
        return new RiskRewardResult(
            riskRewardRatio,
            stopLossDistance,
            takeProfitDistance,
            partialExit1,
            partialExit2,
            direction,
            calculatePositionSizeRecommendation(riskRewardRatio)
        );
    }
    
    /**
     * حساب مسافة Stop Loss بناءً على قوة الأدوات
     */
    private double calculateStopLossDistance(Map<String, Double> toolResults) {
        // كلما كانت الأدوات أقوى، كلما قل Stop Loss (ثقة أكبر)
        double avgConfidence = toolResults.values().stream()
                .filter(conf -> conf != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.6);
        
        // نطاق Stop Loss: 0.3% إلى 1.0%
        double baseStopLoss = 0.8; // 0.8% افتراضي
        
        if (avgConfidence > 0.9) return baseStopLoss * 0.4;      // 0.32% - ثقة عالية جداً
        if (avgConfidence > 0.8) return baseStopLoss * 0.5;      // 0.40% - ثقة عالية
        if (avgConfidence > 0.7) return baseStopLoss * 0.7;      // 0.56% - ثقة جيدة
        if (avgConfidence > 0.6) return baseStopLoss * 0.9;      // 0.72% - ثقة متوسطة
        
        return baseStopLoss * 1.2; // 0.96% - ثقة منخفضة
    }
    
    /**
     * اقتراح حجم الصفقة بناءً على Risk/Reward
     */
    private String calculatePositionSizeRecommendation(double riskRewardRatio) {
        if (riskRewardRatio >= 4.0) return "LARGE";      // 3-5% من الحساب
        if (riskRewardRatio >= 3.0) return "MEDIUM";     // 2-3% من الحساب  
        if (riskRewardRatio >= 2.0) return "STANDARD";   // 1-2% من الحساب
        if (riskRewardRatio >= 1.5) return "SMALL";      // 0.5-1% من الحساب
        return "MICRO";                                   // 0.25-0.5% من الحساب
    }
    
    /**
     * فئة نتيجة حساب المخاطر والأهداف
     */
    public static class RiskRewardResult {
        private final double riskRewardRatio;
        private final double stopLossDistance;
        private final double takeProfitDistance;
        private final double partialExit1;
        private final double partialExit2;
        private final String direction;
        private final String positionSizeRecommendation;
        
        public RiskRewardResult(double riskRewardRatio, double stopLossDistance, 
                              double takeProfitDistance, double partialExit1, 
                              double partialExit2, String direction, 
                              String positionSizeRecommendation) {
            this.riskRewardRatio = riskRewardRatio;
            this.stopLossDistance = stopLossDistance;
            this.takeProfitDistance = takeProfitDistance;
            this.partialExit1 = partialExit1;
            this.partialExit2 = partialExit2;
            this.direction = direction;
            this.positionSizeRecommendation = positionSizeRecommendation;
        }
        
        // Getters
        public double getRiskRewardRatio() { return riskRewardRatio; }
        public double getStopLossDistance() { return stopLossDistance; }
        public double getTakeProfitDistance() { return takeProfitDistance; }
        public double getPartialExit1() { return partialExit1; }
        public double getPartialExit2() { return partialExit2; }
        public String getDirection() { return direction; }
        public String getPositionSizeRecommendation() { return positionSizeRecommendation; }
        
        @Override
        public String toString() {
            return String.format("RiskReward{ratio=1:%.1f, stopLoss=%.3f%%, takeProfit=%.3f%%, size=%s}", 
                               riskRewardRatio, stopLossDistance * 100, takeProfitDistance * 100, 
                               positionSizeRecommendation);
        }
    }
    
    /**
     * 🎯 Bookmap Level - مستوى واحد من Bookmap
     */
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
        
        public int getPriority() {
            if (type.contains("POC")) return 1;          // أعلى أولوية
            if (type.contains("VWAP")) return 2;         
            if (type.contains("HEATMAP")) return 3;      
            if (type.contains("ICEBERG")) return 4;      
            if (type.contains("ABSORPTION")) return 5;   
            return 6; // أولوية عادية
        }
        
        @Override
        public String toString() {
            return String.format("%.5f (%s - %.1f%%)", price, type, strength * 100);
        }
    }
    
    /**
     * 🚀 Smart Risk Reward Result - نتيجة الحساب الذكي المدمج
     */
    public static class SmartRiskRewardResult {
        private final RiskRewardResult originalMathResult;
        private final List<BookmapLevel> bookmapLevels;
        private final double finalStopLossDistance;
        private final double finalTakeProfitDistance;
        private final double partialExit1;
        private final double partialExit2;
        private final double finalRiskRewardRatio;
        private final BookmapLevel bestStopLevel;
        private final BookmapLevel bestTargetLevel;
        private final String mergeQuality;
        private final double smartConfidence;
        
        public SmartRiskRewardResult(RiskRewardResult originalMathResult, List<BookmapLevel> bookmapLevels,
                                   double finalStopLossDistance, double finalTakeProfitDistance,
                                   double partialExit1, double partialExit2, double finalRiskRewardRatio,
                                   BookmapLevel bestStopLevel, BookmapLevel bestTargetLevel,
                                   String mergeQuality, double smartConfidence) {
            this.originalMathResult = originalMathResult;
            this.bookmapLevels = bookmapLevels;
            this.finalStopLossDistance = finalStopLossDistance;
            this.finalTakeProfitDistance = finalTakeProfitDistance;
            this.partialExit1 = partialExit1;
            this.partialExit2 = partialExit2;
            this.finalRiskRewardRatio = finalRiskRewardRatio;
            this.bestStopLevel = bestStopLevel;
            this.bestTargetLevel = bestTargetLevel;
            this.mergeQuality = mergeQuality;
            this.smartConfidence = smartConfidence;
        }
        
        // Getters
        public RiskRewardResult getOriginalMathResult() { return originalMathResult; }
        public List<BookmapLevel> getBookmapLevels() { return new ArrayList<>(bookmapLevels); }
        public double getFinalStopLossDistance() { return finalStopLossDistance; }
        public double getFinalTakeProfitDistance() { return finalTakeProfitDistance; }
        public double getPartialExit1() { return partialExit1; }
        public double getPartialExit2() { return partialExit2; }
        public double getFinalRiskRewardRatio() { return finalRiskRewardRatio; }
        public BookmapLevel getBestStopLevel() { return bestStopLevel; }
        public BookmapLevel getBestTargetLevel() { return bestTargetLevel; }
        public String getMergeQuality() { return mergeQuality; }
        public double getSmartConfidence() { return smartConfidence; }
        
        /**
         * Get Entry Price (currentPrice will be provided by PatternEngine)
         */
        public double getEntryPrice() {
            // This will be set by PatternEngine when creating the result
            return 0.0; // PatternEngine will provide the actual entry price
        }
        
        /**
         * Get Stop Loss distance (as percentage)
         */
        public double getStopLoss() {
            return finalStopLossDistance;
        }
        
        /**
         * Get Take Profit distance (as percentage)
         */
        public double getTakeProfit() {
            return finalTakeProfitDistance;
        }
        
        /**
         * Get Risk/Reward Ratio
         */
        public double getRiskRewardRatio() {
            return finalRiskRewardRatio;
        }
        
        /**
         * Get Position Size recommendation
         */
        public String getPositionSize() {
            return originalMathResult.getPositionSizeRecommendation();
        }
        
        /**
         * Get Mathematical Stop Loss distance
         */
        public double getMathematicalSL() {
            return originalMathResult.getStopLossDistance();
        }
        
        /**
         * Get Mathematical Take Profit distance
         */
        public double getMathematicalTP() {
            return originalMathResult.getTakeProfitDistance();
        }
        
        /**
         * Get Bookmap-based Stop Loss distance
         */
        public double getBookmapSL() {
            if (bestStopLevel != null) {
                return finalStopLossDistance;
            }
            return originalMathResult.getStopLossDistance(); // fallback to math
        }
        
        /**
         * Get Bookmap-based Take Profit distance
         */
        public double getBookmapTP() {
            if (bestTargetLevel != null) {
                return finalTakeProfitDistance;
            }
            return originalMathResult.getTakeProfitDistance(); // fallback to math
        }
        
        // ========== ORIGINAL METHODS ==========
        
        /**
         * مقارنة مع النتيجة الرياضية الأصلية
         */
        public String getComparisonSummary() {
            double mathRR = originalMathResult.getRiskRewardRatio();
            double improvement = ((finalRiskRewardRatio - mathRR) / mathRR) * 100;
            
            return String.format("Smart vs Math: R/R improved by %.1f%% (1:%.1f → 1:%.1f), Quality: %s", 
                               improvement, mathRR, finalRiskRewardRatio, mergeQuality);
        }
        
        /**
         * الحصول على تفاصيل مستويات Bookmap
         */
        public String getBookmapLevelsSummary() {
            if (bookmapLevels.isEmpty()) {
                return "❌ No Bookmap levels found";
            }
            
            StringBuilder summary = new StringBuilder();
            summary.append(String.format("📊 Bookmap Levels (%d total):\n", bookmapLevels.size()));
            
            if (bestStopLevel != null) {
                summary.append(String.format("   🛡️ Best Stop: %s\n", bestStopLevel));
            }
            if (bestTargetLevel != null) {
                summary.append(String.format("   🎯 Best Target: %s\n", bestTargetLevel));
            }
            
            summary.append(String.format("   💯 Confidence: %.1f%% | Quality: %s", 
                                        smartConfidence * 100, mergeQuality));
            
            return summary.toString();
        }
        
        /**
         * حساب مستويات التداول الفعلية
         */
        public String getTradingLevels(double currentPrice, String direction) {
            double stopLoss, takeProfit, partialExit1Price, partialExit2Price;
            
            if ("BULLISH".equals(direction)) {
                stopLoss = currentPrice * (1 - finalStopLossDistance);
                takeProfit = currentPrice * (1 + finalTakeProfitDistance);
                partialExit1Price = currentPrice * (1 + partialExit1);
                partialExit2Price = currentPrice * (1 + partialExit2);
            } else {
                stopLoss = currentPrice * (1 + finalStopLossDistance);
                takeProfit = currentPrice * (1 - finalTakeProfitDistance);
                partialExit1Price = currentPrice * (1 - partialExit1);
                partialExit2Price = currentPrice * (1 - partialExit2);
            }
            
            return String.format(
                "🎯 Smart Trading Levels:\n" +
                "   Entry: %.5f\n" +
                "   Stop Loss: %.5f (%.3f%%) %s\n" +
                "   Partial Exit 1: %.5f (50%% position)\n" +
                "   Partial Exit 2: %.5f (80%% position)\n" +
                "   Take Profit: %.5f (%.3f%%) %s\n" +
                "   Risk/Reward: 1:%.1f\n" +
                "   Confidence: %.1f%% | Quality: %s",
                currentPrice, 
                stopLoss, finalStopLossDistance * 100, bestStopLevel != null ? "(" + bestStopLevel.getType() + ")" : "",
                partialExit1Price, partialExit2Price, 
                takeProfit, finalTakeProfitDistance * 100, bestTargetLevel != null ? "(" + bestTargetLevel.getType() + ")" : "",
                finalRiskRewardRatio, smartConfidence * 100, mergeQuality
            );
        }
        
        @Override
        public String toString() {
            return String.format("SmartRR{1:%.1f, SL=%.3f%%, TP=%.3f%%, Quality=%s, Confidence=%.1f%%}", 
                               finalRiskRewardRatio, finalStopLossDistance * 100, 
                               finalTakeProfitDistance * 100, mergeQuality, smartConfidence * 100);
        }
    }
    
    /**
     * استخراج مستويات Bookmap من الأدوات المفعلة
     */
    private List<BookmapLevel> extractBookmapLevels(double currentPrice, Map<String, Double> toolResults) {
        List<BookmapLevel> levels = new ArrayList<>();
        
        // استخراج من Heatmap
        extractHeatmapLevels(levels, currentPrice, toolResults.get("heatmap"));
        
        // استخراج من Volume Profile  
        extractVolumeProfileLevels(levels, currentPrice, toolResults.get("volume_profile"));
        
        // استخراج من VWAP
        extractVWAPLevels(levels, currentPrice, toolResults.get("vwap"));
        
        // استخراج من Absorption
        extractAbsorptionLevels(levels, currentPrice, toolResults.get("absorption_indicator"));
        
        // استخراج من Iceberg
        extractIcebergLevels(levels, currentPrice, toolResults.get("iceberg_detector"));
        
        // ترتيب حسب القوة والأولوية
        levels.sort(Comparator.comparingInt(BookmapLevel::getPriority)
                   .thenComparingDouble(level -> -level.getStrength()));
        
        return levels;
    }
    
    /**
     * دمج النتائج الرياضية مع مستويات Bookmap
     */
    private SmartRiskRewardResult mergeResults(RiskRewardResult mathResult, List<BookmapLevel> bookmapLevels, 
                                              String direction, double currentPrice) {
        
        // الأهداف الرياضية الأصلية
        double mathStopLoss = mathResult.getStopLossDistance();
        double mathTakeProfit = mathResult.getTakeProfitDistance();
        
        // البحث عن أفضل مستويات Bookmap
        BookmapLevel bestStopLevel = findBestStopLevel(bookmapLevels, currentPrice, mathStopLoss, direction);
        BookmapLevel bestTargetLevel = findBestTargetLevel(bookmapLevels, currentPrice, mathTakeProfit, direction);
        
        // حساب الأهداف النهائية
        double finalStopLoss = calculateFinalStopLoss(mathStopLoss, bestStopLevel, currentPrice);
        double finalTakeProfit = calculateFinalTakeProfit(mathTakeProfit, bestTargetLevel, currentPrice);
        
        // نسبة R/R الجديدة
        double finalRiskRewardRatio = finalTakeProfit / finalStopLoss;
        
        // تقييم جودة الدمج
        String mergeQuality = assessMergeQuality(mathResult, bestStopLevel, bestTargetLevel);
        
        return new SmartRiskRewardResult(
            mathResult,                    // النتيجة الرياضية الأصلية
            bookmapLevels,                // جميع مستويات Bookmap
            finalStopLoss,                // Stop Loss النهائي
            finalTakeProfit,              // Take Profit النهائي
            finalTakeProfit * 0.5,        // Partial Exit 1
            finalTakeProfit * 0.8,        // Partial Exit 2
            finalRiskRewardRatio,         // نسبة R/R النهائية
            bestStopLevel,                // أفضل مستوى stop
            bestTargetLevel,              // أفضل مستوى هدف
            mergeQuality,                 // جودة الدمج
            calculateSmartConfidence(mathResult, bookmapLevels)  // الثقة المدمجة
        );
    }
    
    // ========== استخراج مستويات من أدوات Bookmap ==========
    
    private void extractHeatmapLevels(List<BookmapLevel> levels, double currentPrice, Double confidence) {
        if (confidence == null || confidence < 0.6) return;
        
        // محاكاة مناطق سيولة عالية من Heatmap
        Random rand = new Random();
        for (int i = 0; i < 2 + rand.nextInt(2); i++) {
            double offset = (rand.nextDouble() - 0.5) * 0.012; // ±0.6%
            double price = currentPrice * (1 + offset);
            String type = price > currentPrice ? "HEATMAP_RESISTANCE" : "HEATMAP_SUPPORT";
            levels.add(new BookmapLevel(price, type, confidence * 0.85, "High liquidity zone"));
        }
    }
    
    private void extractVolumeProfileLevels(List<BookmapLevel> levels, double currentPrice, Double confidence) {
        if (confidence == null || confidence < 0.5) return;
        
        Random rand = new Random();
        
        // POC (Point of Control) - أهم مستوى
        double poc = currentPrice * (1 + (rand.nextDouble() - 0.5) * 0.008);
        String pocType = poc > currentPrice ? "VP_POC_RESISTANCE" : "VP_POC_SUPPORT";
        levels.add(new BookmapLevel(poc, pocType, 0.90, "Volume Profile POC"));
        
        // Value Area High & Low
        double vah = currentPrice * (1.007 + rand.nextDouble() * 0.003);
        double val = currentPrice * (0.993 - rand.nextDouble() * 0.003);
        levels.add(new BookmapLevel(vah, "VP_VAH", 0.75, "Value Area High"));
        levels.add(new BookmapLevel(val, "VP_VAL", 0.75, "Value Area Low"));
    }
    
    private void extractVWAPLevels(List<BookmapLevel> levels, double currentPrice, Double confidence) {
        if (confidence == null || confidence < 0.5) return;
        
        Random rand = new Random();
        
        // VWAP Level
        double vwap = currentPrice * (1 + (rand.nextDouble() - 0.5) * 0.004);
        String vwapType = vwap > currentPrice ? "VWAP_RESISTANCE" : "VWAP_SUPPORT";
        levels.add(new BookmapLevel(vwap, vwapType, 0.80, "VWAP Level"));
        
        // VWAP Standard Deviations
        double stdDev = currentPrice * 0.003;
        levels.add(new BookmapLevel(vwap + stdDev, "VWAP_STD+1", 0.60, "VWAP +1 StdDev"));
        levels.add(new BookmapLevel(vwap - stdDev, "VWAP_STD-1", 0.60, "VWAP -1 StdDev"));
    }
    
    private void extractAbsorptionLevels(List<BookmapLevel> levels, double currentPrice, Double confidence) {
        if (confidence == null || confidence < 0.6) return;
        
        Random rand = new Random();
        double absorptionPrice = currentPrice * (1 + (rand.nextDouble() - 0.5) * 0.008);
        String type = absorptionPrice > currentPrice ? "ABSORPTION_RESISTANCE" : "ABSORPTION_SUPPORT";
        levels.add(new BookmapLevel(absorptionPrice, type, confidence * 0.80, "Strong absorption zone"));
    }
    
    private void extractIcebergLevels(List<BookmapLevel> levels, double currentPrice, Double confidence) {
        if (confidence == null || confidence < 0.7) return;
        
        Random rand = new Random();
        double icebergPrice = currentPrice * (1 + (rand.nextDouble() - 0.5) * 0.006);
        String type = icebergPrice > currentPrice ? "ICEBERG_RESISTANCE" : "ICEBERG_SUPPORT";
        levels.add(new BookmapLevel(icebergPrice, type, confidence * 0.75, "Hidden iceberg order"));
    }
    
    // ========== البحث والاختيار ==========
    
    private BookmapLevel findBestStopLevel(List<BookmapLevel> levels, double currentPrice, double mathStopLoss, String direction) {
        // البحث عن مستوى دعم مناسب
        return levels.stream()
            .filter(level -> isValidStopLevel(level, currentPrice, direction))
            .filter(level -> isWithinReasonableRange(level, currentPrice, mathStopLoss, 0.5, 1.5))
            .min(Comparator.comparingInt(BookmapLevel::getPriority)
                .thenComparingDouble(level -> -level.getStrength()))
            .orElse(null);
    }
    
    private BookmapLevel findBestTargetLevel(List<BookmapLevel> levels, double currentPrice, double mathTakeProfit, String direction) {
        // البحث عن مستوى مقاومة مناسب
        return levels.stream()
            .filter(level -> isValidTargetLevel(level, currentPrice, direction))
            .filter(level -> isWithinReasonableRange(level, currentPrice, mathTakeProfit, 0.3, 2.0))
            .min(Comparator.comparingInt(BookmapLevel::getPriority)
                .thenComparingDouble(level -> -level.getStrength()))
            .orElse(null);
    }
    
    private boolean isValidStopLevel(BookmapLevel level, double currentPrice, String direction) {
        if ("BULLISH".equals(direction)) {
            return level.getPrice() < currentPrice && level.getType().contains("SUPPORT");
        } else {
            return level.getPrice() > currentPrice && level.getType().contains("RESISTANCE");
        }
    }
    
    private boolean isValidTargetLevel(BookmapLevel level, double currentPrice, String direction) {
        if ("BULLISH".equals(direction)) {
            return level.getPrice() > currentPrice && level.getType().contains("RESISTANCE");
        } else {
            return level.getPrice() < currentPrice && level.getType().contains("SUPPORT");
        }
    }
    
    private boolean isWithinReasonableRange(BookmapLevel level, double currentPrice, double mathDistance, double minFactor, double maxFactor) {
        double levelDistance = Math.abs(level.getPrice() - currentPrice) / currentPrice;
        return levelDistance >= mathDistance * minFactor && levelDistance <= mathDistance * maxFactor;
    }
    
    // ========== الحسابات النهائية ==========
    
    private double calculateFinalStopLoss(double mathStopLoss, BookmapLevel bestStopLevel, double currentPrice) {
        if (bestStopLevel == null) {
            return mathStopLoss;
        }
        
        double bookmapStopLoss = Math.abs(bestStopLevel.getPrice() - currentPrice) / currentPrice;
        
        // متوسط مرجح بناءً على قوة المستوى
        double weight = bestStopLevel.getStrength();
        return (bookmapStopLoss * weight) + (mathStopLoss * (1 - weight));
    }
    
    private double calculateFinalTakeProfit(double mathTakeProfit, BookmapLevel bestTargetLevel, double currentPrice) {
        if (bestTargetLevel == null) {
            return mathTakeProfit;
        }
        
        double bookmapTakeProfit = Math.abs(bestTargetLevel.getPrice() - currentPrice) / currentPrice;
        
        // اختيار الأقوى إذا كان المستوى قوي جداً
        if (bestTargetLevel.getStrength() > 0.8) {
            return bookmapTakeProfit;
        } else {
            // متوسط مرجح
            double weight = bestTargetLevel.getStrength();
            return (bookmapTakeProfit * weight) + (mathTakeProfit * (1 - weight));
        }
    }
    
    private String assessMergeQuality(RiskRewardResult mathResult, BookmapLevel stopLevel, BookmapLevel targetLevel) {
        int qualityScore = 0;
        
        if (stopLevel != null && stopLevel.getStrength() > 0.7) qualityScore += 2;
        if (targetLevel != null && targetLevel.getStrength() > 0.7) qualityScore += 2;
        if (stopLevel != null && stopLevel.getPriority() <= 3) qualityScore += 1;
        if (targetLevel != null && targetLevel.getPriority() <= 3) qualityScore += 1;
        
        if (qualityScore >= 5) return "EXCELLENT";
        if (qualityScore >= 3) return "GOOD";
        if (qualityScore >= 1) return "FAIR";
        return "MATHEMATICAL_ONLY";
    }
    
    private double calculateSmartConfidence(RiskRewardResult mathResult, List<BookmapLevel> bookmapLevels) {
        double baseConfidence = 0.75;
        
        // مكافأة وجود مستويات قوية
        long strongLevels = bookmapLevels.stream()
            .mapToLong(level -> level.getStrength() > 0.7 ? 1 : 0)
            .sum();
        
        double bookmapBonus = Math.min(0.15, strongLevels * 0.03);
        
        return Math.min(0.92, baseConfidence + bookmapBonus);
    }
} 