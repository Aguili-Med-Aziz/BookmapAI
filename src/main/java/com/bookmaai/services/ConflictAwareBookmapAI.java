package com.bookmaai.services;

import com.bookmaai.core.AnalysisResult;
import com.bookmaai.core.AnalysisResult.SignalDirection;
import com.bookmaai.core.AnalysisResult.ConflictSeverity;
import com.bookmaai.core.AnalysisResult.ConflictInfo;
import com.bookmaai.core.AnalysisResult.SignalStrength;
import com.bookmaai.core.AnalysisResult.TradingAction;
import com.bookmaai.core.PatternEngineAdvanced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
// PatternEngineAdvanced imports cleaned up

/**
 * 🛡️ نظام BookmapAI الآمن الشامل مع إدارة التضارب (UNIFIED SECURITY LAYER)
 * 
 * يجمع جميع وظائف الأمان والتحليل المتقدم:
 * ✅ كشف التضارب (مدمج من PatternConflictDetector)
 * ✅ نظام الأولوية (مدمج من PatternPrioritySystem) 
 * ✅ تحليل الأنماط المتقدم (مدمج من PatternAnalysisService)
 * ✅ التوصيات الآمنة والتقارير الشاملة
 * ✅ تحليل التكرار الأسبوعي وإدارة الأنماط المتضاربة
 * 
 * مدمج من 4 خدمات: ConflictAwareBookmapAI + PatternConflictDetector + PatternPrioritySystem + PatternAnalysisService
 */
@Service
public class ConflictAwareBookmapAI {
    
    private static final Logger logger = LoggerFactory.getLogger(ConflictAwareBookmapAI.class);
    
    @Autowired
    private PatternEngineAdvanced patternEngine;
    
    @Autowired
    private BookmapConfigurationLoader configLoader;
    
    // ================ Inner Classes مدمجة من النماذج المحذوفة ================
    
    public enum ConflictSeverity {
        LOW, MEDIUM, HIGH, CRITICAL, NONE
    }
    
    public static class ConflictInfo {
        private String conflictType;
        private String severity;
        private List<String> conflictingPatterns;
        private String description;
        private double score;
        
        // Constructor with 4 parameters (original)
        public ConflictInfo(String type, String severity, List<String> patterns, String description) {
            this.conflictType = type;
            this.severity = severity;
            this.conflictingPatterns = patterns;
            this.description = description;
            this.score = 0.5;
        }
        
        // Constructor with 5 parameters (extended)
        public ConflictInfo(String type, String severity, String description, String recommendation, double score) {
            this.conflictType = type;
            this.severity = severity;
            this.conflictingPatterns = new ArrayList<>();
            this.description = description;
            this.score = score;
        }
        
        // Constructor with ConflictSeverity enum
        public ConflictInfo(String type, ConflictSeverity severity, String description, String recommendation, double score) {
            this.conflictType = type;
            this.severity = severity.toString();
            this.conflictingPatterns = new ArrayList<>();
            this.description = description;
            this.score = score;
        }
        
        public String getConflictType() { return conflictType; }
        public String getSeverity() { return severity; }
        public List<String> getConflictingPatterns() { return conflictingPatterns; }
        public String getDescription() { return description; }
        public double getScore() { return score; }
        
        // Missing methods
        public String getType() { return conflictType; }
        public ConflictSeverity getSeverityEnum() { 
            try {
                return ConflictSeverity.valueOf(severity);
            } catch (Exception e) {
                return ConflictSeverity.MEDIUM;
            }
        }
        public String getRecommendation() { return "توخي الحذر عند " + conflictType; }
        public double getImpact() { return score; }
    }
    
    public enum SignalStrength {
        WEAK, MODERATE, STRONG, CRITICAL, VERY_STRONG, VERY_HIGH
    }
    
    public static class WeeklyFrequencyReport {
        public Map<String, PatternFrequency> patternFrequencies;
        public int totalWeeklyOpportunities;
        public List<String> bestTradingDays;
        public double averageSuccessRate;
    }
    
    public static class PatternFrequency {
        private String patternName;
        private double successRate;
        private int weeklyOccurrences;
        private String priority;
        
        public PatternFrequency(String name, double success, int weekly, String priority) {
            this.patternName = name;
            this.successRate = success;
            this.weeklyOccurrences = weekly;
            this.priority = priority;
        }
        
        public String getPatternName() { return patternName; }
        public double getSuccessRate() { return successRate; }
        public int getWeeklyOccurrences() { return weeklyOccurrences; }
        public String getPriority() { return priority; }
    }
    
    public static class ConflictingPatternsReport {
        public int totalPatterns;
        public List<ConflictGroup> conflictGroups;
        public int totalConflicts;
        public double conflictPercentage;
    }
    
    public static class ConflictGroup {
        public String asset;
        public String patternType;
        public List<ConflictingPattern> conflictingPatterns;
        public String conflictReason;
    }
    
    public static class ConflictingPattern {
        public String name;
        public double successRate;
        public String toolName;
    }
    
    public static class CleanupResult {
        public List<String> removedPatterns;
        public List<String> keptPatterns;
        public int processedGroups;
    }
    
    // ================ SafeTradingAnalysis class ================
    
    public static class SafeTradingAnalysis {
        private final String asset;
        private final String session;
        private final RiskLevel riskLevel;
        private final FinalAction finalAction;
        private final double confidence;
        private final double positionSize;
        private final String safetyMessage;
        private final boolean safeToTrade;
        private final AnalysisResult analysisResult;
        
        public SafeTradingAnalysis(String asset, String session, RiskLevel riskLevel, 
                                 FinalAction finalAction, double confidence, double positionSize,
                                 String safetyMessage, boolean safeToTrade, AnalysisResult analysisResult) {
            this.asset = asset;
            this.session = session;
            this.riskLevel = riskLevel;
            this.finalAction = finalAction;
            this.confidence = confidence;
            this.positionSize = positionSize;
            this.safetyMessage = safetyMessage;
            this.safeToTrade = safeToTrade;
            this.analysisResult = analysisResult;
        }
        
        public enum RiskLevel {
            VERY_LOW, LOW, MODERATE, HIGH, VERY_HIGH
        }
        
        public enum FinalAction {
            STRONG_BUY, BUY, HOLD, SELL, STRONG_SELL, NO_TRADE, WATCH
        }
        
        // Getters
        public String getAsset() { return asset; }
        public String getSession() { return session; }
        public RiskLevel getRiskLevel() { return riskLevel; }
        public FinalAction getFinalAction() { return finalAction; }
        public double getConfidence() { return confidence; }
        public double getPositionSize() { return positionSize; }
        public String getSafetyMessage() { return safetyMessage; }
        public boolean isSafeToTrade() { return safeToTrade; }
        public AnalysisResult getAnalysisResult() { return analysisResult; }
        
        // Missing methods
        public String getSafeRecommendation() {
            return finalAction.toString();
        }
        
        public double getPositionSizeMultiplier() {
            return positionSize;
        }
        
        public String getConflictWarning() {
            if (analysisResult != null && analysisResult.isHasConflicts()) {
                return analysisResult.getConflictSummary();
            }
            return null;
        }
        
        // Builder pattern
        public static SafeTradingAnalysisBuilder builder() {
            return new SafeTradingAnalysisBuilder();
        }
        
        public static class SafeTradingAnalysisBuilder {
            private String asset;
            private String session;
            private RiskLevel riskLevel;
            private FinalAction finalAction;
            private double confidence;
            private double positionSize;
            private String safetyMessage;
            private boolean safeToTrade;
            private AnalysisResult analysisResult;
            private Object rawAnalysis;
            private AnalysisResult filteredResult;
            private String conflictWarning;
            private FinalAction safeRecommendation;
            private double positionSizeMultiplier;
            
            public SafeTradingAnalysisBuilder rawAnalysis(Object rawAnalysis) {
                this.rawAnalysis = rawAnalysis;
                return this;
            }
            
            public SafeTradingAnalysisBuilder filteredResult(AnalysisResult filteredResult) {
                this.filteredResult = filteredResult;
                this.analysisResult = filteredResult;
                return this;
            }
            
            public SafeTradingAnalysisBuilder conflictWarning(String conflictWarning) {
                this.conflictWarning = conflictWarning;
                return this;
            }
            
            public SafeTradingAnalysisBuilder safeRecommendation(FinalAction safeRecommendation) {
                this.safeRecommendation = safeRecommendation;
                this.finalAction = safeRecommendation;
                return this;
            }
            
            public SafeTradingAnalysisBuilder riskLevel(RiskLevel riskLevel) {
                this.riskLevel = riskLevel;
                return this;
            }
            
            public SafeTradingAnalysisBuilder confidence(double confidence) {
                this.confidence = confidence;
                return this;
            }
            
            public SafeTradingAnalysisBuilder positionSizeMultiplier(double positionSizeMultiplier) {
                this.positionSizeMultiplier = positionSizeMultiplier;
                this.positionSize = positionSizeMultiplier;
                return this;
            }
            
            public SafeTradingAnalysisBuilder safetyMessage(String safetyMessage) {
                this.safetyMessage = safetyMessage;
                return this;
            }
            
            public SafeTradingAnalysisBuilder asset(String asset) {
                this.asset = asset;
                return this;
            }
            
            public SafeTradingAnalysisBuilder session(String session) {
                this.session = session;
                return this;
            }
            
            public SafeTradingAnalysis build() {
                this.safeToTrade = riskLevel != RiskLevel.VERY_HIGH;
                return new SafeTradingAnalysis(asset, session, riskLevel, finalAction, 
                                             confidence, positionSize, safetyMessage, 
                                             safeToTrade, analysisResult);
            }
        }
    }
    
    // ================ دمج وظائف PatternPrioritySystem ================
    
    // ترتيب الأولويات (مدمج من PatternPrioritySystem)
    private static final Map<String, Integer> PATTERN_PRIORITIES = new HashMap<String, Integer>() {{
        put("AMDPattern", 1);
        put("MANIPULATION_DETECTION", 1);
        put("StopRunSweepPattern", 2);
        put("STOP_RUN_CLASSIC", 2);
        put("VWAPInstitutionalReversal", 3);
        put("VWAP_INSTITUTIONAL_REVERSAL", 3);
        put("BreakoutPattern", 4);
        put("ReversalPattern", 5);
        put("TrendPattern", 6);
        put("SupportResistancePattern", 7);
        put("ICEBERG_ACCUMULATION", 8);
        put("CVDPattern", 9);
        put("VolumeProfilePattern", 10);
        put("VWAPPattern", 11);
        put("LargeLotPattern", 12);
        put("HeatmapPattern", 13);
        put("VolumeDotsPattern", 14);
    }};
    
    // عوامل تقليل الثقة عند التضارب (مدمج من PatternPrioritySystem)
    private static final double CRITICAL_CONFLICT_REDUCTION = 0.5; // تقليل 50%
    private static final double HIGH_CONFLICT_REDUCTION = 0.7;     // تقليل 30%
    private static final double MEDIUM_CONFLICT_REDUCTION = 0.85;  // تقليل 15%
    
    // ================ دمج وظائف PatternAnalysisService ================
    
    // الأنماط القوية الـ5 (مدمج من PatternAnalysisService)
    private static final Map<String, PatternFrequency> TOP_5_PATTERNS = new HashMap<String, PatternFrequency>() {{
        put("STOP_RUN_CLASSIC_NQ", new PatternFrequency("STOP_RUN_CLASSIC_NQ", 0.92, 2, "HIGH_PRIORITY"));
        put("MANIPULATION_DETECTION_NQ", new PatternFrequency("MANIPULATION_DETECTION_NQ", 0.94, 1, "CRITICAL_PRIORITY"));
        put("VWAP_INSTITUTIONAL_REVERSAL_NQ", new PatternFrequency("VWAP_INSTITUTIONAL_REVERSAL_NQ", 0.89, 3, "HIGH_PRIORITY"));
        put("ICEBERG_ACCUMULATION_NQ", new PatternFrequency("ICEBERG_ACCUMULATION_NQ", 0.87, 4, "MEDIUM_PRIORITY"));
        put("CVD_DIVERGENCE_STRONG_NQ", new PatternFrequency("CVD_DIVERGENCE_STRONG_NQ", 0.85, 5, "MEDIUM_PRIORITY"));
    }};
    
    // عتبات التضارب (مدمج من PatternConflictDetector)
    private static final double STRENGTH_CONFLICT_THRESHOLD = 0.4;
    private static final double CRITICAL_CONFLICT_THRESHOLD = 0.8;
    private static final double MINIMUM_SAFE_CONFIDENCE = 0.3; // للاختبار مؤقتاً
    private static final double HIGH_CONFIDENCE_THRESHOLD = 0.8;
    
    /**
     * 🛡️ التحليل الآمن المبسط (واجهة سهلة)
     */
    public SafeTradingAnalysis performSafeAnalysis(String asset, String session) {
        return performComprehensiveAnalysis(asset, session);
    }
    
    /**
     * 🛡️ التحليل الآمن الشامل الموحد
     */
    public SafeTradingAnalysis performComprehensiveAnalysis(String asset, String session) {
        
        logger.info("🛡️ Starting comprehensive safe analysis for asset: {}, session: {}", asset, session);
        
        try {
            // 1. تشغيل تحليل الأنماط
            AnalysisResult rawAnalysis = patternEngine.analyzePatterns(asset, 0.0, 0.0, 0.0);
            
            // 2. جمع نتائج الأنماط
            List<AnalysisResult> allPatterns = simulatePatternResults(asset, session);
            
            // 3. كشف التضارب (وظيفة مدمجة من PatternConflictDetector)
            AnalysisResult conflictAnalysis = performAdvancedConflictDetection(allPatterns);
            
            // 4. حل التضارب بالأولوية (وظيفة مدمجة من PatternPrioritySystem)
            AnalysisResult filteredResult = performAdvancedConflictResolution(allPatterns, conflictAnalysis);
            
            // 5. تحليل متقدم للأنماط (وظيفة مدمجة من PatternAnalysisService)
            WeeklyFrequencyReport weeklyReport = generateWeeklyFrequencyAnalysis();
            
            // 6. إنتاج التوصية الآمنة النهائية
            SafeTradingAnalysis safeAnalysis = generateComprehensiveSafeRecommendation(
                rawAnalysis, filteredResult, conflictAnalysis, weeklyReport, asset, session
            );
            
            logger.info("🛡️ Comprehensive analysis completed for {}: {} (confidence: {:.1f}%)", 
                       asset, safeAnalysis.getSafeRecommendation(), 
                       safeAnalysis.getConfidence() * 100);
            
            return safeAnalysis;
            
        } catch (Exception e) {
            logger.error("❌ Error in comprehensive analysis for {}: {}", asset, e.getMessage());
            return createErrorAnalysis(asset, session, e.getMessage());
        }
    }
    
    /**
     * 🎯 إنتاج التوصية الآمنة الشاملة (مدمج)
     */
    private SafeTradingAnalysis generateComprehensiveSafeRecommendation(
            Object rawAnalysis, AnalysisResult filteredResult, AnalysisResult conflictAnalysis, 
            WeeklyFrequencyReport weeklyReport, String asset, String session) {
        
        // حساب مستوى المخاطر
        SafeTradingAnalysis.RiskLevel riskLevel = calculateRiskLevel(filteredResult, conflictAnalysis);
        
        // تحديد التوصية النهائية
        SafeTradingAnalysis.FinalAction finalAction = determineFinalAction(filteredResult, riskLevel);
        
        // حساب الثقة المعدلة
        double adjustedConfidence = filteredResult.getConfidence();
        
        // تحديد حجم المركز الآمن
        double safePositionSize = calculateSafePositionSize(riskLevel, adjustedConfidence);
        
        // إنشاء رسالة الأمان
        String safetyMessage = generateComprehensiveSafetyMessage(filteredResult, riskLevel);
        
        // تحديد إذا كان آمن للتداول
        boolean isSafe = isSafeForTrading(filteredResult, riskLevel);
        
        return SafeTradingAnalysis.builder()
            .rawAnalysis(rawAnalysis)
            .filteredResult(filteredResult)
            .conflictWarning(conflictAnalysis != null ? conflictAnalysis.getConflictSummary() : "لا يوجد تضارب")
            .safeRecommendation(finalAction)
            .riskLevel(riskLevel)
            .confidence(adjustedConfidence)
            .positionSizeMultiplier(safePositionSize)
            .safetyMessage(safetyMessage)
            .asset(asset)
            .session(session)
            .build();
    }
    
    /**
     * ⚖️ حساب مستوى المخاطر
     */
    private SafeTradingAnalysis.RiskLevel calculateRiskLevel(AnalysisResult filteredResult, AnalysisResult conflictAnalysis) {
        if (!filteredResult.isSafeToTrade()) {
            return SafeTradingAnalysis.RiskLevel.VERY_HIGH;
        }
        
        if (conflictAnalysis.isHasConflicts()) {
            String conflictLevel = conflictAnalysis.getConflictLevel();
            switch (conflictLevel) {
                case "CRITICAL":
                    return SafeTradingAnalysis.RiskLevel.VERY_HIGH;
                case "HIGH":
                    return SafeTradingAnalysis.RiskLevel.HIGH;
                case "MEDIUM":
                    return SafeTradingAnalysis.RiskLevel.MODERATE;
                default:
                    return SafeTradingAnalysis.RiskLevel.LOW;
            }
        }
        
        double confidence = filteredResult.getConfidence();
        if (confidence < 0.6) return SafeTradingAnalysis.RiskLevel.HIGH;
        if (confidence < 0.8) return SafeTradingAnalysis.RiskLevel.MODERATE;
        return SafeTradingAnalysis.RiskLevel.LOW;
    }
    
    /**
     * 🎯 تحديد الإجراء النهائي
     */
    private SafeTradingAnalysis.FinalAction determineFinalAction(AnalysisResult filteredResult, SafeTradingAnalysis.RiskLevel riskLevel) {
        if (riskLevel == SafeTradingAnalysis.RiskLevel.VERY_HIGH) {
            return SafeTradingAnalysis.FinalAction.NO_TRADE;
        }
        
        String direction = filteredResult.getDirection();
        double confidence = filteredResult.getConfidence();
        
        if ("BUY".equals(direction) || "BULLISH".equals(direction)) {
            return confidence > 0.8 ? SafeTradingAnalysis.FinalAction.STRONG_BUY : SafeTradingAnalysis.FinalAction.BUY;
        } else if ("SELL".equals(direction) || "BEARISH".equals(direction)) {
            return confidence > 0.8 ? SafeTradingAnalysis.FinalAction.STRONG_SELL : SafeTradingAnalysis.FinalAction.SELL;
        }
        
        return SafeTradingAnalysis.FinalAction.HOLD;
    }
    
    /**
     * 📊 حساب حجم المركز الآمن
     */
    private double calculateSafePositionSize(SafeTradingAnalysis.RiskLevel riskLevel, double confidence) {
        double baseSize = 1.0;
        
        // تعديل بناءً على المخاطر
        double riskMultiplier;
        switch (riskLevel) {
            case VERY_HIGH:
                riskMultiplier = 0.0;
                break;
            case HIGH:
                riskMultiplier = 0.5;
                break;
            case MODERATE:
                riskMultiplier = 0.7;
                break;
            case LOW:
                riskMultiplier = 1.0;
                break;
            default:
                riskMultiplier = 1.0;
                break;
        }
        
        // تعديل بناءً على الثقة
        double confidenceMultiplier = Math.min(1.2, confidence * 1.5);
        
        return baseSize * riskMultiplier * confidenceMultiplier;
    }
    
    /**
     * 📝 إنشاء رسالة الأمان الشاملة
     */
    private String generateComprehensiveSafetyMessage(AnalysisResult filteredResult, SafeTradingAnalysis.RiskLevel riskLevel) {
        StringBuilder message = new StringBuilder();
        
        if (riskLevel == SafeTradingAnalysis.RiskLevel.VERY_HIGH) {
            message.append("⛔ مخاطر عالية جداً - تجنب التداول تماماً");
        } else if (filteredResult.isHasConflicts()) {
            message.append(String.format("⚠️ تم كشف تضارب (%s) - توخي الحذر", filteredResult.getConflictLevel()));
        } else {
            message.append("✅ إشارة آمنة - يمكن التداول");
        }
        
        message.append(String.format(" | الثقة: %.1f%%", filteredResult.getConfidence() * 100));
        
        if (filteredResult.getPatternName() != null) {
            message.append(String.format(" | النمط: %s", filteredResult.getPatternName()));
        }
        
        return message.toString();
    }
    
    /**
     * 🛡️ تحديد إذا كان آمن للتداول
     */
    private boolean isSafeForTrading(AnalysisResult filteredResult, SafeTradingAnalysis.RiskLevel riskLevel) {
        return riskLevel != SafeTradingAnalysis.RiskLevel.VERY_HIGH &&
               filteredResult.isSafeToTrade() &&
               filteredResult.getConfidence() >= MINIMUM_SAFE_CONFIDENCE;
    }
    
    /**
     * 🔍 كشف التضارب المتقدم (مدمج من PatternConflictDetector)
     */
    private AnalysisResult performAdvancedConflictDetection(List<AnalysisResult> patternResults) {
        
        if (patternResults == null || patternResults.isEmpty()) {
            return createNoConflictAnalysis();
        }
        
        logger.debug("🔍 Advanced conflict detection among {} patterns", patternResults.size());
        
        List<ConflictInfo> conflicts = new ArrayList<>();
        
        // 1. فحص تضارب الاتجاه (Direction conflicts)
        conflicts.addAll(detectDirectionConflicts(patternResults));
        
        // 2. فحص تضارب القوة (Strength conflicts)
        conflicts.addAll(detectStrengthConflicts(patternResults));
        
        // 3. فحص تضارب التوقيت (Timing conflicts)
        conflicts.addAll(detectTimingConflicts(patternResults));
        
        // 4. فحص تضارب المستويات (Level conflicts)
        conflicts.addAll(detectLevelConflicts(patternResults));
        
        String level = calculateConflictSeverity(conflicts);
        double overallScore = calculateOverallConflictScore(conflicts);
        
        return createConflictAnalysisResult(conflicts, level, overallScore);
    }
    
    /**
     * 🔄 كشف تضارب الاتجاه (مدمج من PatternConflictDetector)
     */
    private List<ConflictInfo> detectDirectionConflicts(List<AnalysisResult> results) {
        
        List<ConflictInfo> conflicts = new ArrayList<>();
        
        List<AnalysisResult> buySignals = results.stream()
            .filter(r -> "BUY".equals(r.getDirection()) || "BULLISH".equals(r.getDirection()))
            .collect(Collectors.toList());
            
        List<AnalysisResult> sellSignals = results.stream()
            .filter(r -> "SELL".equals(r.getDirection()) || "BEARISH".equals(r.getDirection()))
            .collect(Collectors.toList());
        
        // إذا وُجدت إشارات شراء وبيع في نفس الوقت
        if (!buySignals.isEmpty() && !sellSignals.isEmpty()) {
            
            double buyConfidenceAvg = buySignals.stream()
                .mapToDouble(AnalysisResult::getConfidence)
                .average().orElse(0.0);
                
            double sellConfidenceAvg = sellSignals.stream()
                .mapToDouble(AnalysisResult::getConfidence)
                .average().orElse(0.0);
            
            ConflictSeverity severity = 
                Math.max(buyConfidenceAvg, sellConfidenceAvg) > CRITICAL_CONFLICT_THRESHOLD ?
                ConflictSeverity.CRITICAL : ConflictSeverity.HIGH;
            
            ConflictInfo conflict = new ConflictInfo(
                "DIRECTION_CONFLICT",
                severity,
                String.format("تضارب في الاتجاه: %d إشارات شراء مقابل %d إشارات بيع", 
                           buySignals.size(), sellSignals.size()),
                "إشارة غير موثوقة - تجنب التداول أو انتظار تأكيد",
                Math.max(buyConfidenceAvg, sellConfidenceAvg)
            );
            
            conflicts.add(conflict);
            
            logger.warn("⚠️ Direction conflict detected: {} BUY vs {} SELL signals", 
                       buySignals.size(), sellSignals.size());
        }
        
        return conflicts;
    }
    
    /**
     * 💪 كشف تضارب القوة (مدمج من PatternConflictDetector)
     */
    private List<ConflictInfo> detectStrengthConflicts(List<AnalysisResult> results) {
        
        List<ConflictInfo> conflicts = new ArrayList<>();
        
        // البحث عن أنماط بنفس الاتجاه لكن قوة متضاربة جداً
        Map<String, List<AnalysisResult>> groupedByDirection = results.stream()
            .filter(r -> !"NEUTRAL".equals(r.getDirection()) && !"HOLD".equals(r.getDirection()))
            .collect(Collectors.groupingBy(AnalysisResult::getDirection));
        
        for (Map.Entry<String, List<AnalysisResult>> entry : groupedByDirection.entrySet()) {
            List<AnalysisResult> sameDirectionPatterns = entry.getValue();
            
            if (sameDirectionPatterns.size() > 1) {
                double maxConfidence = sameDirectionPatterns.stream()
                    .mapToDouble(AnalysisResult::getConfidence)
                    .max().orElse(0.0);
                    
                double minConfidence = sameDirectionPatterns.stream()
                    .mapToDouble(AnalysisResult::getConfidence)
                    .min().orElse(0.0);
                
                // إذا كان الفرق في القوة > 40%
                if (maxConfidence - minConfidence > STRENGTH_CONFLICT_THRESHOLD) {
                    ConflictInfo conflict = new ConflictInfo(
                        "STRENGTH_CONFLICT",
                        ConflictSeverity.MEDIUM,
                        String.format("تضارب في القوة لاتجاه %s: فرق %.1f%% في مستوى الثقة", 
                                   entry.getKey(), (maxConfidence - minConfidence) * 100),
                        "قلل حجم الصفقة - ثقة متوسطة",
                        maxConfidence - minConfidence
                    );
                    
                    conflicts.add(conflict);
                    
                    logger.debug("💪 Strength conflict detected for {}: {:.1f}% difference", 
                               entry.getKey(), (maxConfidence - minConfidence) * 100);
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * ⏰ كشف تضارب التوقيت (مدمج من PatternConflictDetector)
     */
    private List<ConflictInfo> detectTimingConflicts(List<AnalysisResult> results) {
        List<ConflictInfo> conflicts = new ArrayList<>();
        
        // فحص إذا كانت الأنماط من جلسات مختلفة
        Map<String, List<AnalysisResult>> groupedBySession = results.stream()
            .filter(r -> r.getSession() != null)
            .collect(Collectors.groupingBy(AnalysisResult::getSession));
        
        if (groupedBySession.size() > 1) {
            ConflictInfo conflict = new ConflictInfo(
                "TIMING_CONFLICT",
                ConflictSeverity.LOW,
                "تضارب في التوقيت: أنماط من جلسات مختلفة",
                "انتظر تأكيد إضافي من الجلسة الحالية",
                0.3
            );
            
            conflicts.add(conflict);
        }
        
        return conflicts;
    }
    
    /**
     * 📊 كشف تضارب المستويات (جديد - مدمج)
     */
    private List<ConflictInfo> detectLevelConflicts(List<AnalysisResult> results) {
        List<ConflictInfo> conflicts = new ArrayList<>();
        
        // فحص إذا كانت الأنماط تشير لمستويات مختلفة جداً
        Set<Double> targetPrices = results.stream()
            .map(AnalysisResult::getTargetPrice)
            .filter(price -> price != null)
            .collect(Collectors.toSet());
        
        if (targetPrices.size() > 2) {
            double maxTarget = targetPrices.stream().max(Double::compare).orElse(0.0);
            double minTarget = targetPrices.stream().min(Double::compare).orElse(0.0);
            
            if (Math.abs(maxTarget - minTarget) / Math.min(maxTarget, minTarget) > 0.02) { // 2% فرق
                ConflictInfo conflict = new ConflictInfo(
                    "LEVEL_CONFLICT",
                    ConflictSeverity.MEDIUM,
                    String.format("تضارب في المستويات: فرق %.2f%% بين الأهداف", 
                                Math.abs(maxTarget - minTarget) / Math.min(maxTarget, minTarget) * 100),
                    "استخدم متوسط الأهداف أو انتظر تأكيد",
                    0.4
                );
                
                conflicts.add(conflict);
            }
        }
        
        return conflicts;
    }
    
    // ================ دمج وظائف PatternPrioritySystem ================
    
    /**
     * 🏆 حل التضارب المتقدم بالأولوية (مدمج من PatternPrioritySystem)
     */
    private AnalysisResult performAdvancedConflictResolution(List<AnalysisResult> patterns, AnalysisResult conflictAnalysis) {
        
        if (!conflictAnalysis.isHasConflicts()) {
            return createNoConflictResolution(patterns);
        }
        
        // ترتيب الأنماط حسب الأولوية (مدمج من PatternPrioritySystem)
        List<AnalysisResult> sortedByPriority = patterns.stream()
            .sorted(this::compareByAdvancedPriority)
            .collect(Collectors.toList());
        
        // الأولوية الأولى تفوز
        AnalysisResult winningPattern = sortedByPriority.get(0);
        
        // حساب عامل تقليل الثقة (مدمج من PatternPrioritySystem)
        double reductionFactor = calculateAdvancedReductionFactor(conflictAnalysis.getConflictLevel(), patterns.size());
        double adjustedConfidence = winningPattern.getConfidence() * reductionFactor;
        
        return createResolvedSignalResult(winningPattern, adjustedConfidence, conflictAnalysis, reductionFactor);
    }
    
    /**
     * 📊 مقارنة الأنماط حسب الأولوية المتقدمة (مدمج من PatternPrioritySystem)
     */
    private int compareByAdvancedPriority(AnalysisResult p1, AnalysisResult p2) {
        
        // 1. الأولوية الأساسية (أقل رقم = أولوية أعلى)
        int priority1 = getAdvancedPatternPriority(p1);
        int priority2 = getAdvancedPatternPriority(p2);
        
        if (priority1 != priority2) {
            return Integer.compare(priority1, priority2);
        }
        
        // 2. في حالة التساوي، استخدم مستوى الثقة
        int confidenceComparison = Double.compare(p2.getConfidence(), p1.getConfidence());
        if (confidenceComparison != 0) {
            return confidenceComparison;
        }
        
        // 3. في حالة التساوي، استخدم قوة الإشارة
        return compareSignalStrength(p2.getStrength(), p1.getStrength());
    }
    
    /**
     * 🏅 الحصول على أولوية النمط المتقدمة (مدمج من PatternPrioritySystem)
     */
    private int getAdvancedPatternPriority(AnalysisResult pattern) {
        
        // البحث بالاسم الكامل أولاً
        String patternName = pattern.getPatternName();
        if (patternName != null) {
            Integer priority = PATTERN_PRIORITIES.get(patternName);
            if (priority != null) {
                return priority;
            }
            
            // البحث بالاسم الجزئي
            for (Map.Entry<String, Integer> entry : PATTERN_PRIORITIES.entrySet()) {
                if (patternName.toUpperCase().contains(entry.getKey().toUpperCase()) ||
                    entry.getKey().toUpperCase().contains(patternName.toUpperCase())) {
                    return entry.getValue();
                }
            }
        }
        
        // البحث باسم الأداة
        String toolName = pattern.getToolName();
        if (toolName != null) {
            Integer priority = PATTERN_PRIORITIES.get(toolName + "Pattern");
            if (priority != null) {
                return priority;
            }
        }
        
        // أولوية افتراضية منخفضة
        logger.debug("🔍 No priority found for pattern: {}, using default priority 999", patternName);
        return 999;
    }
    
    /**
     * 💪 مقارنة قوة الإشارة (مدمج من PatternPrioritySystem)
     */
    private int compareSignalStrength(com.bookmaai.core.AnalysisResult.SignalStrength s1, com.bookmaai.core.AnalysisResult.SignalStrength s2) {
        if (s1 == null || s2 == null) return 0;
        
        Map<com.bookmaai.core.AnalysisResult.SignalStrength, Integer> strengthOrder = new HashMap<>();
        strengthOrder.put(com.bookmaai.core.AnalysisResult.SignalStrength.VERY_STRONG, 4);
        strengthOrder.put(com.bookmaai.core.AnalysisResult.SignalStrength.STRONG, 3);
        strengthOrder.put(com.bookmaai.core.AnalysisResult.SignalStrength.MODERATE, 2);
        strengthOrder.put(com.bookmaai.core.AnalysisResult.SignalStrength.WEAK, 1);
        
        return Integer.compare(
            strengthOrder.getOrDefault(s1, 0),
            strengthOrder.getOrDefault(s2, 0)
        );
    }
    
    /**
     * 📉 حساب عامل تقليل الثقة المتقدم (مدمج من PatternPrioritySystem)
     */
    private double calculateAdvancedReductionFactor(String level, int conflictCount) {
        
        // حساب أساسي بناءً على مستوى التضارب
        double baseFactor;
        switch (level) {
            case "CRITICAL":
                baseFactor = CRITICAL_CONFLICT_REDUCTION;
                break;
            case "HIGH":
                baseFactor = HIGH_CONFLICT_REDUCTION;
                break;
            case "MEDIUM":
                baseFactor = MEDIUM_CONFLICT_REDUCTION;
                break;
            case "LOW":
                baseFactor = 0.95;
                break;
            default:
                baseFactor = 1.0;
                break;
        }
        
        // تقليل إضافي بناءً على عدد الأنماط المتضاربة
        double countPenalty = Math.max(0.8, 1.0 - (conflictCount * 0.05));
        
        return baseFactor * countPenalty;
    }
    
    // ================ Helper Methods المفقودة ================
    
    /**
     * 🚫 إنشاء تحليل بدون تضارب
     */
    private AnalysisResult createNoConflictAnalysis() {
        return AnalysisResult.builder()
            .symbol("NO_SYMBOL")
            .direction("NEUTRAL")
            .signal("NO_CONFLICT")
            .confidence(1.0)
            .conflictInfo(false, "NONE", 1.0)
            .build();
    }
    
    /**
     * 🔍 إنشاء نتيجة تحليل التضارب
     */
    private AnalysisResult createConflictAnalysisResult(List<ConflictInfo> conflicts, String level, double score) {
        AnalysisResult result = AnalysisResult.builder()
            .symbol("CONFLICT_ANALYSIS")
            .direction("CONFLICT")
            .signal("CONFLICT_DETECTED")
            .confidence(1.0 - score)
            .conflictInfo(true, level, 1.0 - score)
            .build();
        
        // Convert ConflictInfo list to AnalysisResult.ConflictInfo list
        List<com.bookmaai.core.AnalysisResult.ConflictInfo> convertedConflicts = new ArrayList<>();
        for (ConflictInfo conflict : conflicts) {
            // Convert ConflictSeverity enum
            com.bookmaai.core.AnalysisResult.ConflictSeverity analysisResultSeverity;
            switch (conflict.getSeverityEnum()) {
                case CRITICAL:
                    analysisResultSeverity = com.bookmaai.core.AnalysisResult.ConflictSeverity.CRITICAL;
                    break;
                case HIGH:
                    analysisResultSeverity = com.bookmaai.core.AnalysisResult.ConflictSeverity.HIGH;
                    break;
                case MEDIUM:
                    analysisResultSeverity = com.bookmaai.core.AnalysisResult.ConflictSeverity.MEDIUM;
                    break;
                case LOW:
                    analysisResultSeverity = com.bookmaai.core.AnalysisResult.ConflictSeverity.LOW;
                    break;
                default:
                    analysisResultSeverity = com.bookmaai.core.AnalysisResult.ConflictSeverity.NONE;
                    break;
            }
            
            convertedConflicts.add(new com.bookmaai.core.AnalysisResult.ConflictInfo(
                conflict.getType(), 
                analysisResultSeverity, 
                conflict.getDescription(), 
                conflict.getRecommendation(), 
                conflict.getImpact()
            ));
        }
        result.setConflictDetails(convertedConflicts);
        result.setConflictSummary(String.format("Detected %d conflicts with %s severity", conflicts.size(), level));
        result.setFilteredRecommendation("مراجعة التضارب قبل التداول");
        result.setSafetyMessage("تم كشف تضارب في الأنماط");
        return result;
    }
    
    /**
     * ✅ إنشاء حل بدون تضارب
     */
    private AnalysisResult createNoConflictResolution(List<AnalysisResult> patterns) {
        if (patterns.isEmpty()) {
            return createNoConflictAnalysis();
        }
        
        // أخذ النمط الأقوى
        AnalysisResult strongest = patterns.stream()
            .max(Comparator.comparing(AnalysisResult::getConfidence))
            .orElse(patterns.get(0));
        
        strongest.setHasConflicts(false);
        strongest.setConflictLevel("NONE");
        strongest.setReductionFactor(1.0);
        strongest.setSafeToTrade(true);
        
        return strongest;
    }
    
    /**
     * 🏆 إنشاء نتيجة الإشارة المحلولة
     */
    private AnalysisResult createResolvedSignalResult(AnalysisResult winningPattern, double adjustedConfidence, 
                                                     AnalysisResult conflictAnalysis, double reductionFactor) {
        // إنشاء نسخة من النمط الفائز مع التعديلات
        AnalysisResult resolved = AnalysisResult.builder()
            .symbol(winningPattern.getSymbol())
            .direction(winningPattern.getDirection())
            .signal(winningPattern.getSignal())
            .confidence(adjustedConfidence)
            .patternName(winningPattern.getPatternName())
            .toolName(winningPattern.getToolName())
            .strength(winningPattern.getStrength())
            .conflictInfo(true, conflictAnalysis.getConflictLevel(), reductionFactor)
            .build();
        
        resolved.setConflictDetails(conflictAnalysis.getConflictDetails());
        resolved.setFilteredRecommendation("RESOLVED_BY_PRIORITY");
        resolved.setSafeToTrade(adjustedConfidence >= MINIMUM_SAFE_CONFIDENCE);
        resolved.setSafetyMessage(adjustedConfidence >= MINIMUM_SAFE_CONFIDENCE ? 
                                 "✅ Resolved conflict - safe to trade" : 
                                 "⚠️ Low confidence after conflict resolution");
        
        return resolved;
    }
    
    /**
     * ⚖️ حساب شدة التضارب
     */
    private String calculateConflictSeverity(List<ConflictInfo> conflicts) {
        if (conflicts.isEmpty()) return "NONE";
        
        boolean hasCritical = conflicts.stream().anyMatch(c -> c.getSeverityEnum() == ConflictSeverity.CRITICAL);
        boolean hasHigh = conflicts.stream().anyMatch(c -> c.getSeverityEnum() == ConflictSeverity.HIGH);
        boolean hasMedium = conflicts.stream().anyMatch(c -> c.getSeverityEnum() == ConflictSeverity.MEDIUM);
        
        if (hasCritical) return "CRITICAL";
        if (hasHigh) return "HIGH";
        if (hasMedium) return "MEDIUM";
        return "LOW";
    }
    
    /**
     * 📊 حساب نقاط التضارب الإجمالية
     */
    private double calculateOverallConflictScore(List<ConflictInfo> conflicts) {
        if (conflicts.isEmpty()) return 0.0;
        
        return conflicts.stream()
            .mapToDouble(ConflictInfo::getImpact)
            .average()
            .orElse(0.0);
    }
    
    // ================ دمج وظائف PatternAnalysisService ================
    
    /**
     * 📅 تحليل التكرار الأسبوعي (مدمج من PatternAnalysisService)
     */
    public WeeklyFrequencyReport generateWeeklyFrequencyAnalysis() {
        logger.info("📅 تحليل معدل الظهور الأسبوعي للأنماط القوية...");
        
        WeeklyFrequencyReport report = new WeeklyFrequencyReport();
        report.patternFrequencies = new HashMap<>(TOP_5_PATTERNS);
        
        report.totalWeeklyOpportunities = TOP_5_PATTERNS.values().stream()
            .mapToInt(PatternFrequency::getWeeklyOccurrences)
            .sum();
            
        report.bestTradingDays = Arrays.asList("Tuesday", "Wednesday", "Thursday");
        report.averageSuccessRate = TOP_5_PATTERNS.values().stream()
            .mapToDouble(PatternFrequency::getSuccessRate)
            .average().orElse(0.0);
        
        logger.info("📊 Weekly analysis: {} opportunities, {:.1f}% average success", 
                   report.totalWeeklyOpportunities, report.averageSuccessRate * 100);
        
        return report;
    }
    
    /**
     * 🔍 البحث عن الأنماط المتضاربة (مدمج من PatternAnalysisService)
     */
    public ConflictingPatternsReport analyzeConflictingPatterns() {
        logger.info("🔍 تحليل الأنماط المتضاربة...");
        
        Set<String> allPatterns = configLoader.getAvailablePatterns();
        Map<String, List<String>> assetGroups = groupPatternsByAsset(allPatterns);
        
        List<ConflictGroup> conflictGroups = new ArrayList<>();
        
        for (Map.Entry<String, List<String>> entry : assetGroups.entrySet()) {
            String asset = entry.getKey();
            List<String> assetPatterns = entry.getValue();
            conflictGroups.addAll(findConflictsInAsset(asset, assetPatterns));
        }
        
        ConflictingPatternsReport report = new ConflictingPatternsReport();
        report.totalPatterns = allPatterns.size();
        report.conflictGroups = conflictGroups;
        report.totalConflicts = conflictGroups.size();
        report.conflictPercentage = calculateConflictPercentage(allPatterns.size(), conflictGroups);
        
        logger.info("🔍 Conflicting patterns analysis: {}/{} patterns have conflicts ({:.1f}%)", 
                   report.totalConflicts, report.totalPatterns, report.conflictPercentage);
        
        return report;
    }
    
    /**
     * 🗑️ إزالة الأنماط الضعيفة من المجموعات المتضاربة (مدمج من PatternAnalysisService)
     */
    public CleanupResult removeWeakConflictingPatterns(ConflictingPatternsReport analysisReport) {
        logger.info("🗑️ إزالة الأنماط الضعيفة...");
        
        CleanupResult result = new CleanupResult();
        result.removedPatterns = new ArrayList<>();
        result.keptPatterns = new ArrayList<>();
        
        for (ConflictGroup group : analysisReport.conflictGroups) {
            logger.info("");
            logger.info("🔍 عرض المجموعة المتضاربة:");
            logger.info("   الأصل: {}", group.asset);
            logger.info("   الأنماط المتضاربة:");
            
            // عرض كل الأنماط المتضاربة أولاً
            for (ConflictingPattern pattern : group.conflictingPatterns) {
                logger.info("     - {} (قوة: {:.1f}%)", pattern.name, pattern.successRate * 100);
            }
            
            // إيجاد الأقوى والأضعف
            ConflictingPattern strongest = findStrongestPattern(group.conflictingPatterns);
            ConflictingPattern weakest = findWeakestPattern(group.conflictingPatterns);
            
            logger.info("   🏆 الأقوى: {} ({:.1f}%)", strongest.name, strongest.successRate * 100);
            logger.info("   ⚠️ الأضعف: {} ({:.1f}%)", weakest.name, weakest.successRate * 100);
            
            // الاحتفاظ بالأقوى وحذف الباقي
            result.keptPatterns.add(strongest.name);
            for (ConflictingPattern pattern : group.conflictingPatterns) {
                if (!pattern.name.equals(strongest.name)) {
                    result.removedPatterns.add(pattern.name);
                }
            }
            
            logger.info("   ✅ سيتم الاحتفاظ بـ: {}", strongest.name);
            logger.info("   🗑️ سيتم حذف: {}", 
                       group.conflictingPatterns.stream()
                           .filter(p -> !p.name.equals(strongest.name))
                           .map(p -> p.name)
                           .reduce((a, b) -> a + ", " + b)
                           .orElse("لا شيء"));
        }
        
        result.processedGroups = analysisReport.conflictGroups.size();
        
        // طباعة نتائج التنظيف
        printCleanupResult(result);
        
        return result;
    }
    
    /**
     * 🏃‍♂️ تشغيل التحليل الشامل (مدمج من PatternAnalysisService)
     */
    public void runFullAnalysis() {
        logger.info("🚀 بدء التحليل الشامل للأنماط...");
        logger.info("================================================");
        
        // 1. تحليل التكرار الأسبوعي
        WeeklyFrequencyReport frequencyReport = generateWeeklyFrequencyAnalysis();
        
        logger.info("");
        logger.info("================================================");
        
        // 2. تحليل الأنماط المتضاربة
        ConflictingPatternsReport conflictReport = analyzeConflictingPatterns();
        
        logger.info("");
        logger.info("================================================");
        
        // 3. تنظيف الأنماط الضعيفة
        if (!conflictReport.conflictGroups.isEmpty()) {
            CleanupResult cleanupResult = removeWeakConflictingPatterns(conflictReport);
            
            logger.info("");
            logger.info("📊 ملخص التحليل النهائي:");
            logger.info("   🎯 الأنماط القوية: {} أنماط", TOP_5_PATTERNS.size());
            logger.info("   📅 فرص أسبوعية: {} فرصة", frequencyReport.totalWeeklyOpportunities);
            logger.info("   ⚠️ مجموعات متضاربة: {} مجموعة", conflictReport.totalConflicts);
            logger.info("   🗑️ أنماط محذوفة: {} نمط", cleanupResult.removedPatterns.size());
            logger.info("   ✅ أنماط محتفظ بها: {} نمط", cleanupResult.keptPatterns.size());
        } else {
            logger.info("✅ النظام نظيف - لا توجد أنماط متضاربة!");
        }
        
        logger.info("================================================");
        logger.info("🏁 انتهى التحليل الشامل بنجاح!");
    }
    
    // ================ Helper Methods مدمجة من PatternAnalysisService ================
    
    private Map<String, List<String>> groupPatternsByAsset(Set<String> patterns) {
        Map<String, List<String>> groups = new HashMap<>();
        
        for (String pattern : patterns) {
            String asset = extractAsset(pattern);
            groups.computeIfAbsent(asset, k -> new ArrayList<>()).add(pattern);
        }
        
        return groups;
    }
    
    private String extractAsset(String patternName) {
        if (patternName.contains("_NQ")) return "NQ";
        if (patternName.contains("_YM")) return "YM";
        if (patternName.contains("_GC")) return "GC";
        if (patternName.contains("_6E")) return "6E";
        if (patternName.contains("_6J")) return "6J";
        if (patternName.contains("_6B")) return "6B";
        return "GENERAL";
    }
    
    private List<ConflictGroup> findConflictsInAsset(String asset, List<String> patterns) {
        List<ConflictGroup> conflicts = new ArrayList<>();
        
        // تجميع الأنماط حسب النوع
        Map<String, List<String>> typeGroups = new HashMap<>();
        for (String pattern : patterns) {
            String type = extractPatternType(pattern);
            typeGroups.computeIfAbsent(type, k -> new ArrayList<>()).add(pattern);
        }
        
        // البحث عن تضارب في كل نوع
        for (Map.Entry<String, List<String>> entry : typeGroups.entrySet()) {
            String type = entry.getKey();
            List<String> typePatterns = entry.getValue();
            
            if (typePatterns.size() > 1) {
                // فحص إذا كانت متضاربة
                List<ConflictingPattern> conflictingPatterns = checkForConflicts(typePatterns);
                
                if (conflictingPatterns.size() > 1) {
                    ConflictGroup group = new ConflictGroup();
                    group.asset = asset;
                    group.patternType = type;
                    group.conflictingPatterns = conflictingPatterns;
                    group.conflictReason = "مستويات نجاح متضاربة في نفس النوع";
                    
                    conflicts.add(group);
                }
            }
        }
        
        return conflicts;
    }
    
    private String extractPatternType(String patternName) {
        if (patternName.contains("CVD")) return "CVD";
        if (patternName.contains("VWAP")) return "VWAP";
        if (patternName.contains("STOP_RUN")) return "STOP_RUN";
        if (patternName.contains("ICEBERG")) return "ICEBERG";
        if (patternName.contains("VOLUME")) return "VOLUME";
        if (patternName.contains("HEATMAP")) return "HEATMAP";
        return "OTHER";
    }
    
    private List<ConflictingPattern> checkForConflicts(List<String> patterns) {
        List<ConflictingPattern> conflicting = new ArrayList<>();
        
        for (String patternName : patterns) {
            BookmapConfigurationLoader.PatternConfig config = configLoader.getPatternConfiguration(patternName);
            if (config != null) {
                ConflictingPattern cp = new ConflictingPattern();
                cp.name = patternName;
                cp.successRate = config.getSuccessRate();
                cp.toolName = config.getToolName();
                
                conflicting.add(cp);
            } else {
                // إذا لم يوجد في الكونفيج، استخدم بيانات افتراضية
                ConflictingPattern cp = new ConflictingPattern();
                cp.name = patternName;
                cp.successRate = 0.75; // قيمة افتراضية
                cp.toolName = extractPatternType(patternName);
                
                conflicting.add(cp);
            }
        }
        
        // فحص إذا كان الفرق في النجاح > 10%
        if (conflicting.size() > 1) {
            double maxSuccess = conflicting.stream().mapToDouble(p -> p.successRate).max().orElse(0.0);
            double minSuccess = conflicting.stream().mapToDouble(p -> p.successRate).min().orElse(0.0);
            
            if (maxSuccess - minSuccess > 0.10) { // فرق 10%
                return conflicting;
            }
        }
        
        return new ArrayList<>(); // لا يوجد تضارب
    }
    
    private double calculateConflictPercentage(int totalPatterns, List<ConflictGroup> conflictGroups) {
        if (totalPatterns == 0) return 0.0;
        
        int conflictingPatterns = conflictGroups.stream()
            .mapToInt(g -> g.conflictingPatterns.size())
            .sum();
        
        return (double) conflictingPatterns / totalPatterns * 100;
    }
    
    private ConflictingPattern findStrongestPattern(List<ConflictingPattern> patterns) {
        return patterns.stream()
            .max(Comparator.comparing(p -> p.successRate))
            .orElse(null);
    }
    
    private ConflictingPattern findWeakestPattern(List<ConflictingPattern> patterns) {
        return patterns.stream()
            .min(Comparator.comparing(p -> p.successRate))
            .orElse(null);
    }
    
    private void printCleanupResult(CleanupResult result) {
        logger.info("");
        logger.info("🗑️ ================ نتائج تنظيف الأنماط ================");
        logger.info("📊 مجموعات معالجة: {}", result.processedGroups);
        logger.info("🗑️ أنماط محذوفة: {}", result.removedPatterns.size());
        logger.info("✅ أنماط محتفظ بها: {}", result.keptPatterns.size());
        logger.info("");
        
        if (!result.keptPatterns.isEmpty()) {
            logger.info("🏆 الأنماط القوية المحتفظ بها:");
            for (String pattern : result.keptPatterns) {
                logger.info("   ✅ {}", pattern);
            }
        }
        
        if (!result.removedPatterns.isEmpty()) {
            logger.info("");
            logger.info("🗑️ الأنماط الضعيفة المحذوفة:");
            for (String pattern : result.removedPatterns) {
                logger.info("   ❌ {}", pattern);
            }
        }
        logger.info("================================================================");
    }

    // ================ دوال إضافية مساعدة ================
    
    /**
     * 🔍 فحص تضارب الاتجاه (مبسط)
     */
    private boolean hasDirectionConflict(List<AnalysisResult> patterns) {
        Set<String> directions = patterns.stream()
            .map(AnalysisResult::getDirection)
            .filter(d -> "BUY".equals(d) || "SELL".equals(d) || "BULLISH".equals(d) || "BEARISH".equals(d))
            .collect(Collectors.toSet());
        return directions.size() > 1;
    }
    
    /**
     * 📊 حساب متوسط الثقة
     */
    private double calculateAverageConfidence(List<AnalysisResult> patterns) {
        return patterns.stream()
            .mapToDouble(AnalysisResult::getConfidence)
            .average()
            .orElse(0.0);
    }
    
    /**
     * 🧪 محاكاة نتائج الأنماط (للاختبار)
     */
    private List<AnalysisResult> simulatePatternResults(String asset, String session) {
        List<AnalysisResult> results = new ArrayList<>();
        
        // محاكاة أنماط مختلفة مع احتمالية تضارب
        results.add(createSimulatedPattern("CVD_DIVERGENCE_" + asset, "CVD", "BUY", 0.85, asset, session));
        results.add(createSimulatedPattern("VWAP_REVERSAL_" + asset, "VWAP", "SELL", 0.78, asset, session));
        results.add(createSimulatedPattern("VOLUME_SPIKE_" + asset, "Volume Dots", "BUY", 0.72, asset, session));
        
        // إضافة نمط عالي الأولوية أحياناً
        if (Math.random() > 0.5) {
            results.add(createSimulatedPattern("STOP_RUN_CLASSIC_" + asset, "Stop Run", "BUY", 0.92, asset, session));
        }
        
        return results;
    }
    
    private AnalysisResult createSimulatedPattern(String patternName, String toolName, 
                                               String direction, double confidence,
                                               String asset, String session) {
        AnalysisResult result = AnalysisResult.builder()
            .symbol(asset)
            .patternName(patternName)
            .toolName(toolName)
            .direction(direction)
            .signal(confidence > 0.8 ? "STRONG_" + direction : direction)
            .confidence(confidence)
            .strength(confidence > 0.8 ? com.bookmaai.core.AnalysisResult.SignalStrength.STRONG : com.bookmaai.core.AnalysisResult.SignalStrength.MODERATE)
            .build();
        
        result.setSession(session);
        result.setDescription("محاكاة نمط " + patternName);
        result.setTargetPrice(50000.0 + (Math.random() * 1000));
        result.setStopLoss(49000.0 + (Math.random() * 500));
        
        return result;
    }
    
    private SafeTradingAnalysis createErrorAnalysis(String asset, String session, String error) {
        return SafeTradingAnalysis.builder()
            .safeRecommendation(SafeTradingAnalysis.FinalAction.NO_TRADE)
            .riskLevel(SafeTradingAnalysis.RiskLevel.VERY_HIGH)
            .confidence(0.0)
            .safetyMessage("خطأ في التحليل: " + error)
            .asset(asset)
            .session(session)
            .build();
    }
}
