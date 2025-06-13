package com.bookmaai.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 🤖 Pattern Learning Logger - مسجل تعلم الأنماط
 * 
 * ينتج ملفات CSV مفصلة لتدريب الـ AI على:
 * - تحليل أسباب نجاح/فشل الأنماط
 * - التنبؤ بالأنماط قبل تكوينها
 * - تحسين قواعد الأهداف والثقة
 * - التعلم من التاريخ والتحسين المستمر
 */
@Service
public class PatternLearningLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(PatternLearningLogger.class);
    
    private final Map<String, PatternTrackingData> activePatterns = new ConcurrentHashMap<>();
    private final AtomicLong patternIdCounter = new AtomicLong(1);
    
    private static final String CSV_HEADER = 
        "PatternId,Timestamp,Symbol,Direction,PatternType,OverallConfidence," +
        "Tier1Tools,Tier2Tools,Tier3Tools,Tier4Tools," +
        "CVD,Heatmap,VolumeDots,VWAP,VolumeProfile,Iceberg,VolumeBubbles," +
        "LargeLot,Imbalance,Absorption,StrengthLevel,StopRun," +
        "EntryPrice,StopLoss,TakeProfit,RiskRewardRatio," +
        "MathematicalSL,MathematicalTP,BookmapSL,BookmapTP," +
        "PositionSize,VolumeAtSignal,MarketCondition,TimeOfDay," +
        "PreviousPatternSuccess,PatternSequence,VolatilityLevel," +
        "Result,PipsGained,SuccessRate,FailureReason,LearningWeight," +
        "ShouldAdjustTargets,ShouldAdjustConfidence,ShouldAdjustPattern," +
        "AIRecommendation,FutureOptimization";

    /**
     * 📊 تسجيل كشف النمط (الطريقة الاحترافية الأساسية)
     */
    public String logPatternDetection(PatternDetectionData detectionData) {
        String patternId = "P" + patternIdCounter.getAndIncrement();
        
        PatternTrackingData tracking = new PatternTrackingData(
            patternId, detectionData, LocalDateTime.now()
        );
        
        activePatterns.put(patternId, tracking);
        
        logger.info("📊 Professional pattern {} logged: {} | {} | {:.1f}%", 
                   patternId, detectionData.getSymbol(), 
                   detectionData.getDirection(), detectionData.getOverallConfidence() * 100);
        
        return patternId;
    }
    
    /**
     * 📊 تسجيل كشف النمط (النسخة المطلوبة للـ AdvancedPatternEngine) - احترافية كاملة
     */
    public String logPatternDetection(com.bookmaai.core.AnalysisResult result, 
                                    RiskRewardCalculator.SmartRiskRewardResult riskReward,
                                    double volume, String source) {
        try {
            // تحويل AnalysisResult إلى PatternDetectionData مع الحفاظ على جميع البيانات الاحترافية
            PatternDetectionData detectionData = convertAnalysisResultToDetectionData(
                result, riskReward, volume, source);
            
            // تسجيل النمط باستخدام الطريقة الاحترافية الأساسية
            String patternId = logPatternDetection(detectionData);
            
            logger.info("📊 Professional pattern logged with ID: {} for symbol: {} | Pattern: {} | Confidence: {:.1f}%", 
                       patternId, result.getSymbol(), result.getPatternName(), result.getConfidence() * 100);
            
            return patternId;
            
        } catch (Exception e) {
            logger.error("❌ Error in professional pattern logging: {}", e.getMessage());
            return generatePatternId(); // استخدام في حالة الخطأ
        }
    }
    
    /**
     * 🎯 تسجيل كشف النمط (النسخة العامة للتوافق) - احترافية
     */
    public String logPatternDetection(Object analysisResult, Object riskReward, double volume, String source) {
        try {
            // Cast آمن للكائنات مع الحفاظ على الوظائف الاحترافية
            com.bookmaai.core.AnalysisResult result = (com.bookmaai.core.AnalysisResult) analysisResult;
            RiskRewardCalculator.SmartRiskRewardResult reward = (RiskRewardCalculator.SmartRiskRewardResult) riskReward;
            
            // استخدام الطريقة الاحترافية المتخصصة
            return logPatternDetection(result, reward, volume, source);
            
        } catch (Exception e) {
            logger.error("❌ Error in professional general pattern logging: {}", e.getMessage());
            return "ERROR_" + System.currentTimeMillis();
        }
    }
    
    /**
     * تحويل AnalysisResult إلى PatternDetectionData
     */
    private PatternDetectionData convertAnalysisResultToDetectionData(
            com.bookmaai.core.AnalysisResult analysisResult, 
            RiskRewardCalculator.SmartRiskRewardResult riskReward, 
            double volume, String source) {
        
        // استخراج البيانات من AnalysisResult
        String symbol = analysisResult.getSymbol();
        String direction = analysisResult.getDirection();
        String patternType = analysisResult.getPatternName(); // تم تغيير getPatternType إلى getPatternName
        double confidence = analysisResult.getConfidence(); // تم تغيير getOverallConfidence إلى getConfidence
        Map<String, Double> toolResults = new HashMap<>(); // سيتم ملؤها لاحقاً
        
        // استخراج بيانات Risk/Reward
        double entryPrice = riskReward.getEntryPrice();
        double stopLoss = riskReward.getStopLoss();
        double takeProfit = riskReward.getTakeProfit();
        double riskRewardRatio = riskReward.getRiskRewardRatio();
        double mathSL = riskReward.getMathematicalSL();
        double mathTP = riskReward.getMathematicalTP();
        double bookmapSL = riskReward.getBookmapSL();
        double bookmapTP = riskReward.getBookmapTP();
        String positionSize = riskReward.getPositionSize();
        
        // تحديد معلومات إضافية
        String marketCondition = determineMarketCondition(volume);
        String timeOfDay = getCurrentTimeOfDay();
        String volatilityLevel = "MEDIUM"; // يمكن تحسينه
        double expectedPips = Math.abs(takeProfit - entryPrice) * 10000;
        double previousSuccess = 0.75; // محاكاة
        String patternSequence = "NEUTRAL";
        
        return new PatternDetectionData(
            symbol, direction, patternType, confidence, toolResults,
            entryPrice, stopLoss, takeProfit, riskRewardRatio,
            mathSL, mathTP, bookmapSL, bookmapTP, positionSize,
            volume, marketCondition, timeOfDay, previousSuccess,
            patternSequence, volatilityLevel, expectedPips
        );
    }
    
    /**
     * تحديد حالة السوق حسب الحجم
     */
    private String determineMarketCondition(double volume) {
        if (volume > 1500) return "HIGH_LIQUIDITY";
        if (volume > 800) return "MEDIUM_LIQUIDITY";
        return "LOW_LIQUIDITY";
    }
    
    /**
     * تحديد وقت اليوم التداولي
     */
    private String getCurrentTimeOfDay() {
        int hour = LocalDateTime.now().getHour();
        
        if (hour >= 8 && hour < 12) return "LONDON_OPEN";
        if (hour >= 13 && hour < 17) return "NY_OPEN";
        if (hour >= 17 && hour < 21) return "LONDON_NY_OVERLAP";
        if (hour >= 21 || hour < 2) return "NY_CLOSE";
        return "ASIAN_SESSION";
    }
    
    /**
     * تحديث نتيجة النمط
     */
    public void updatePatternResult(String patternId, double currentPrice, String result) {
        PatternTrackingData tracking = activePatterns.get(patternId);
        if (tracking == null) return;
        
        // حساب النقاط المحققة
        double pipsGained = calculatePipsGained(tracking, currentPrice, result);
        String failureReason = analyzeFailureReason(tracking, currentPrice, result);
        
        // تحديث البيانات
        tracking.setResult(result, pipsGained, failureReason);
        
        // تحليل الذكي للتحسينات
        AnalysisResult analysis = performIntelligentAnalysis(tracking);
        tracking.setAnalysisResult(analysis);
        
        // تسجيل النهائي في CSV
        appendToCSV(tracking, result, pipsGained, failureReason);
        
        // إزالة من المتابعة النشطة
        activePatterns.remove(patternId);
        
        logger.info("📊 Pattern {} completed: {} | {:.1f} pips | {}", 
                   patternId, result, pipsGained, failureReason);
    }
    
    /**
     * تحليل ذكي للنمط لاستخراج التوصيات
     */
    private AnalysisResult performIntelligentAnalysis(PatternTrackingData tracking) {
        PatternDetectionData data = tracking.getDetectionData();
        
        // تحليل قوة الأدوات
        double toolsStrength = calculateToolsEffectiveness(data);
        
        // تحليل توقيت السوق
        MarketCondition marketCondition = analyzeMarketCondition(data);
        
        // توصيات التحسين
        OptimizationRecommendations recommendations = generateOptimizationRecommendations(
            tracking, toolsStrength, marketCondition);
        
        return new AnalysisResult(recommendations);
    }
    
    /**
     * حساب فعالية الأدوات
     */
    private double calculateToolsEffectiveness(PatternDetectionData data) {
        Map<String, Double> tools = data.getToolResults();
        double totalWeight = 0.0;
        double weightedSum = 0.0;
        
        // أوزان الطبقات - متوافق مع جميع إصدارات Java
        Map<String, Double> tierWeights = new HashMap<>();
        tierWeights.put("cvd", 4.0);
        tierWeights.put("heatmap", 3.8);
        tierWeights.put("volume_dots", 3.6);
        tierWeights.put("vwap", 3.4);
        tierWeights.put("volume_profile", 3.0);
        tierWeights.put("iceberg_detector", 3.2);
        tierWeights.put("volume_bubbles", 2.8);
        tierWeights.put("large_lot_tracker", 2.4);
        tierWeights.put("imbalance_indicator", 2.2);
        tierWeights.put("absorption_indicator", 2.6);
        tierWeights.put("strength_level_indicator", 2.0);
        tierWeights.put("stop_run", 1.8);
        
        for (Map.Entry<String, Double> entry : tools.entrySet()) {
            double weight = tierWeights.getOrDefault(entry.getKey(), 1.0);
            weightedSum += entry.getValue() * weight;
            totalWeight += weight;
        }
        
        return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
    }
    
    /**
     * تحليل حالة السوق
     */
    private MarketCondition analyzeMarketCondition(PatternDetectionData data) {
        double volume = data.getVolumeAtSignal();
        String timeOfDay = data.getTimeOfDay();
        
        if (volume > 1000 && ("LONDON_OPEN".equals(timeOfDay) || "NY_OPEN".equals(timeOfDay))) {
            return MarketCondition.HIGH_LIQUIDITY;
        } else if (volume > 500) {
            return MarketCondition.MEDIUM_LIQUIDITY;
        } else {
            return MarketCondition.LOW_LIQUIDITY;
        }
    }
    
    /**
     * إنتاج توصيات التحسين
     */
    private OptimizationRecommendations generateOptimizationRecommendations(
            PatternTrackingData tracking, double toolsStrength, MarketCondition marketCondition) {
        
        boolean shouldAdjustTargets = false;
        boolean shouldAdjustConfidence = false;
        boolean shouldAdjustPattern = false;
        String aiRecommendation = "";
        String futureOptimization = "";
        
        // تحليل النتائج
        if ("SUCCESS".equals(tracking.getResult())) {
            if (tracking.getPipsGained() > tracking.getDetectionData().getExpectedPips() * 1.5) {
                shouldAdjustTargets = true;
                aiRecommendation = "INCREASE_TARGETS";
                futureOptimization = "Pattern shows higher potential - increase target ratios by 15%";
            }
            
            if (toolsStrength > 0.9) {
                shouldAdjustConfidence = true;
                aiRecommendation += "_BOOST_CONFIDENCE";
                futureOptimization += " | Boost confidence threshold for similar setups";
            }
        } else {
            if (toolsStrength < 0.6) {
                shouldAdjustPattern = true;
                aiRecommendation = "PATTERN_FILTER_STRENGTHEN";
                futureOptimization = "Add stronger filters - require minimum 60% tools strength";
            }
            
            if (marketCondition == MarketCondition.LOW_LIQUIDITY) {
                shouldAdjustTargets = true;
                aiRecommendation += "_REDUCE_TARGETS_LOW_LIQUIDITY";
                futureOptimization += " | Reduce targets during low liquidity periods";
            }
        }
        
        return new OptimizationRecommendations(
            shouldAdjustTargets, shouldAdjustConfidence, shouldAdjustPattern,
            aiRecommendation, futureOptimization
        );
    }
    
    /**
     * كتابة البيانات إلى CSV
     */
    private void appendToCSV(PatternTrackingData tracking, String result, double pipsGained, String failureReason) {
        try {
            String filename = "pattern_learning_data_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd")) + ".csv";
            
            boolean fileExists = new java.io.File(filename).exists();
            
            try (FileWriter writer = new FileWriter(filename, true)) {
                // كتابة الرأس إذا كان ملف جديد
                if (!fileExists) {
                    writer.append(CSV_HEADER).append("\n");
                }
                
                // كتابة البيانات
                writer.append(formatCSVRow(tracking, result, pipsGained, failureReason));
                writer.flush();
            }
            
        } catch (IOException e) {
            logger.error("❌ Failed to write CSV: {}", e.getMessage());
        }
    }
    
    /**
     * تنسيق صف CSV
     */
    private String formatCSVRow(PatternTrackingData tracking, String result, double pipsGained, String failureReason) {
        PatternDetectionData data = tracking.getDetectionData();
        Map<String, Double> tools = data.getToolResults();
        
        // تقسيم الأدوات حسب الطبقات
        String tier1Tools = String.format("%.3f,%.3f,%.3f,%.3f",
            tools.getOrDefault("cvd", 0.0), tools.getOrDefault("heatmap", 0.0),
            tools.getOrDefault("volume_dots", 0.0), tools.getOrDefault("vwap", 0.0));
            
        String tier2Tools = String.format("%.3f,%.3f,%.3f",
            tools.getOrDefault("volume_profile", 0.0), tools.getOrDefault("iceberg_detector", 0.0),
            tools.getOrDefault("volume_bubbles", 0.0));
            
        String tier3Tools = String.format("%.3f,%.3f,%.3f",
            tools.getOrDefault("large_lot_tracker", 0.0), tools.getOrDefault("imbalance_indicator", 0.0),
            tools.getOrDefault("absorption_indicator", 0.0));
            
        String tier4Tools = String.format("%.3f,%.3f",
            tools.getOrDefault("strength_level_indicator", 0.0), tools.getOrDefault("stop_run", 0.0));
        
        // بناء الصف
        StringBuilder row = new StringBuilder();
        row.append(tracking.getPatternId()).append(",");
        row.append(tracking.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append(",");
        row.append(data.getSymbol()).append(",");
        row.append(data.getDirection()).append(",");
        row.append(data.getPatternType()).append(",");
        row.append(String.format("%.3f", data.getOverallConfidence())).append(",");
        
        // الأدوات
        row.append(tier1Tools).append(",");
        row.append(tier2Tools).append(",");
        row.append(tier3Tools).append(",");
        row.append(tier4Tools).append(",");
        
        // الأسعار والأهداف
        row.append(String.format("%.5f", data.getEntryPrice())).append(",");
        row.append(String.format("%.5f", data.getStopLoss())).append(",");
        row.append(String.format("%.5f", data.getTakeProfit())).append(",");
        row.append(String.format("%.2f", data.getRiskRewardRatio())).append(",");
        row.append(String.format("%.5f", data.getMathematicalSL())).append(",");
        row.append(String.format("%.5f", data.getMathematicalTP())).append(",");
        row.append(String.format("%.5f", data.getBookmapSL())).append(",");
        row.append(String.format("%.5f", data.getBookmapTP())).append(",");
        
        // معلومات السوق
        row.append(data.getPositionSize()).append(",");
        row.append(String.format("%.1f", data.getVolumeAtSignal())).append(",");
        row.append(data.getMarketCondition()).append(",");
        row.append(data.getTimeOfDay()).append(",");
        row.append(String.format("%.3f", data.getPreviousPatternSuccess())).append(",");
        row.append(data.getPatternSequence()).append(",");
        row.append(data.getVolatilityLevel()).append(",");
        
        // النتائج
        row.append(result).append(",");
        row.append(String.format("%.2f", pipsGained)).append(",");
        row.append(String.format("%.3f", calculateSuccessRate(data))).append(",");
        row.append(failureReason != null ? failureReason : "").append(",");
        row.append(String.format("%.2f", calculateLearningWeight(tracking, result))).append(",");
        
        // التوصيات (إذا كان التحليل متوفر)
        if (tracking.getAnalysisResult() != null) {
            OptimizationRecommendations rec = tracking.getAnalysisResult().getRecommendations();
            row.append(rec.shouldAdjustTargets() ? "TRUE" : "FALSE").append(",");
            row.append(rec.shouldAdjustConfidence() ? "TRUE" : "FALSE").append(",");
            row.append(rec.shouldAdjustPattern() ? "TRUE" : "FALSE").append(",");
            row.append(rec.getAiRecommendation()).append(",");
            row.append(rec.getFutureOptimization());
        } else {
            row.append("FALSE,FALSE,FALSE,,");
        }
        
        row.append("\n");
        return row.toString();
    }
    
    /**
     * حساب النقاط المحققة
     */
    private double calculatePipsGained(PatternTrackingData tracking, double currentPrice, String result) {
        PatternDetectionData data = tracking.getDetectionData();
        double entryPrice = data.getEntryPrice();
        
        if ("SUCCESS".equals(result)) {
            if ("BULLISH".equals(data.getDirection())) {
                return (data.getTakeProfit() - entryPrice) * 10000; // للفوركس
            } else {
                return (entryPrice - data.getTakeProfit()) * 10000;
            }
        } else if ("STOPPED".equals(result)) {
            if ("BULLISH".equals(data.getDirection())) {
                return (data.getStopLoss() - entryPrice) * 10000; // سالب
            } else {
                return (entryPrice - data.getStopLoss()) * 10000; // سالب
            }
        } else {
            // جزئي
            if ("BULLISH".equals(data.getDirection())) {
                return (currentPrice - entryPrice) * 10000;
            } else {
                return (entryPrice - currentPrice) * 10000;
            }
        }
    }
    
    /**
     * تحليل سبب الفشل
     */
    private String analyzeFailureReason(PatternTrackingData tracking, double currentPrice, String result) {
        if ("SUCCESS".equals(result)) return "SUCCESS";
        
        PatternDetectionData data = tracking.getDetectionData();
        
        // تحليل الأسباب المحتملة
        if (data.getOverallConfidence() < 0.7) {
            return "LOW_CONFIDENCE";
        } else if (data.getVolumeAtSignal() < 300) {
            return "LOW_VOLUME";
        } else if ("LOW_LIQUIDITY".equals(data.getMarketCondition())) {
            return "POOR_MARKET_CONDITIONS";
        } else if (calculateToolsEffectiveness(data) < 0.6) {
            return "WEAK_TOOLS_SIGNAL";
        } else {
            return "MARKET_REVERSAL";
        }
    }
    
    /**
     * حساب معدل النجاح
     */
    private double calculateSuccessRate(PatternDetectionData data) {
        // محاكاة حساب معدل النجاح التاريخي للنمط
        return 0.75; // 75% كمثال
    }
    
    /**
     * حساب وزن التعلم
     */
    private double calculateLearningWeight(PatternTrackingData tracking, String result) {
        // وزن أعلى للأنماط التي لها تأثير تعليمي أكبر
        double weight = 1.0;
        
        PatternDetectionData data = tracking.getDetectionData();
        
        // أنماط نادرة لها وزن أعلى
        if ("PERFECT_STORM".equals(data.getPatternType()) || 
            "TRIPLE_CONFIRMATION".equals(data.getPatternType())) {
            weight += 0.5;
        }
        
        // نتائج مفاجئة لها وزن أعلى
        if (("SUCCESS".equals(result) && data.getOverallConfidence() < 0.6) ||
            ("STOPPED".equals(result) && data.getOverallConfidence() > 0.9)) {
            weight += 0.3;
        }
        
        return weight;
    }
    
    /**
     * 🔢 إنشاء ID فريد للنمط (طريقة احترافية)
     */
    private String generatePatternId() {
        return "PATTERN_" + System.currentTimeMillis() + "_" + 
               String.format("%04d", (int)(Math.random() * 10000));
    }
    
    // =================== Data Classes ===================
    
    public static class PatternDetectionData {
        private final String symbol;
        private final String direction;
        private final String patternType;
        private final double overallConfidence;
        private final Map<String, Double> toolResults;
        private final double entryPrice;
        private final double stopLoss;
        private final double takeProfit;
        private final double riskRewardRatio;
        private final double mathematicalSL;
        private final double mathematicalTP;
        private final double bookmapSL;
        private final double bookmapTP;
        private final String positionSize;
        private final double volumeAtSignal;
        private final String marketCondition;
        private final String timeOfDay;
        private final double previousPatternSuccess;
        private final String patternSequence;
        private final String volatilityLevel;
        private final double expectedPips;
        
        public PatternDetectionData(String symbol, String direction, String patternType,
                                  double overallConfidence, Map<String, Double> toolResults,
                                  double entryPrice, double stopLoss, double takeProfit,
                                  double riskRewardRatio, double mathematicalSL, double mathematicalTP,
                                  double bookmapSL, double bookmapTP, String positionSize,
                                  double volumeAtSignal, String marketCondition, String timeOfDay,
                                  double previousPatternSuccess, String patternSequence,
                                  String volatilityLevel, double expectedPips) {
            this.symbol = symbol;
            this.direction = direction;
            this.patternType = patternType;
            this.overallConfidence = overallConfidence;
            this.toolResults = toolResults;
            this.entryPrice = entryPrice;
            this.stopLoss = stopLoss;
            this.takeProfit = takeProfit;
            this.riskRewardRatio = riskRewardRatio;
            this.mathematicalSL = mathematicalSL;
            this.mathematicalTP = mathematicalTP;
            this.bookmapSL = bookmapSL;
            this.bookmapTP = bookmapTP;
            this.positionSize = positionSize;
            this.volumeAtSignal = volumeAtSignal;
            this.marketCondition = marketCondition;
            this.timeOfDay = timeOfDay;
            this.previousPatternSuccess = previousPatternSuccess;
            this.patternSequence = patternSequence;
            this.volatilityLevel = volatilityLevel;
            this.expectedPips = expectedPips;
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public String getDirection() { return direction; }
        public String getPatternType() { return patternType; }
        public double getOverallConfidence() { return overallConfidence; }
        public Map<String, Double> getToolResults() { return toolResults; }
        public double getEntryPrice() { return entryPrice; }
        public double getStopLoss() { return stopLoss; }
        public double getTakeProfit() { return takeProfit; }
        public double getRiskRewardRatio() { return riskRewardRatio; }
        public double getMathematicalSL() { return mathematicalSL; }
        public double getMathematicalTP() { return mathematicalTP; }
        public double getBookmapSL() { return bookmapSL; }
        public double getBookmapTP() { return bookmapTP; }
        public String getPositionSize() { return positionSize; }
        public double getVolumeAtSignal() { return volumeAtSignal; }
        public String getMarketCondition() { return marketCondition; }
        public String getTimeOfDay() { return timeOfDay; }
        public double getPreviousPatternSuccess() { return previousPatternSuccess; }
        public String getPatternSequence() { return patternSequence; }
        public String getVolatilityLevel() { return volatilityLevel; }
        public double getExpectedPips() { return expectedPips; }
    }
    
    private static class PatternTrackingData {
        private final String patternId;
        private final PatternDetectionData detectionData;
        private final LocalDateTime timestamp;
        private String result;
        private double pipsGained;
        private String failureReason;
        private AnalysisResult analysisResult;
        
        public PatternTrackingData(String patternId, PatternDetectionData detectionData, LocalDateTime timestamp) {
            this.patternId = patternId;
            this.detectionData = detectionData;
            this.timestamp = timestamp;
        }
        
        public void setResult(String result, double pipsGained, String failureReason) {
            this.result = result;
            this.pipsGained = pipsGained;
            this.failureReason = failureReason;
        }
        
        public void setAnalysisResult(AnalysisResult analysisResult) {
            this.analysisResult = analysisResult;
        }
        
        // Professional Getters - الطرق الاحترافية
        public String getPatternId() { return patternId; }
        public PatternDetectionData getDetectionData() { return detectionData; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getResult() { return result; }
        public double getPipsGained() { return pipsGained; }
        public String getFailureReason() { return failureReason; }
        public AnalysisResult getAnalysisResult() { return analysisResult; }
    }
    
    private static class AnalysisResult {
        private final OptimizationRecommendations recommendations;
        
        public AnalysisResult(OptimizationRecommendations recommendations) {
            this.recommendations = recommendations;
        }
        
        public OptimizationRecommendations getRecommendations() { return recommendations; }
    }
    
    private static class OptimizationRecommendations {
        private final boolean shouldAdjustTargets;
        private final boolean shouldAdjustConfidence;
        private final boolean shouldAdjustPattern;
        private final String aiRecommendation;
        private final String futureOptimization;
        
        public OptimizationRecommendations(boolean shouldAdjustTargets, boolean shouldAdjustConfidence,
                                         boolean shouldAdjustPattern, String aiRecommendation, String futureOptimization) {
            this.shouldAdjustTargets = shouldAdjustTargets;
            this.shouldAdjustConfidence = shouldAdjustConfidence;
            this.shouldAdjustPattern = shouldAdjustPattern;
            this.aiRecommendation = aiRecommendation;
            this.futureOptimization = futureOptimization;
        }
        
        public boolean shouldAdjustTargets() { return shouldAdjustTargets; }
        public boolean shouldAdjustConfidence() { return shouldAdjustConfidence; }
        public boolean shouldAdjustPattern() { return shouldAdjustPattern; }
        public String getAiRecommendation() { return aiRecommendation; }
        public String getFutureOptimization() { return futureOptimization; }
    }
    
    private enum MarketCondition {
        HIGH_LIQUIDITY, MEDIUM_LIQUIDITY, LOW_LIQUIDITY
    }
} 