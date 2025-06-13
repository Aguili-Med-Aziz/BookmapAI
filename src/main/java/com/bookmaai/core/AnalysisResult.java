package com.bookmaai.core;

import com.bookmaai.services.RiskRewardCalculator.RiskRewardResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 📊 نتيجة التحليل الشاملة الموحدة (UNIFIED ANALYSIS RESULT)
 * 
 * تجمع جميع حقول النماذج المحذوفة:
 * ✅ من FilteredSignalResult: conflict info, filtered signals
 * ✅ من PatternResult: pattern details, tool info
 * ✅ من ConflictAnalysis: conflict detection results
 * ✅ من PatternConflict: conflict resolution info
 * ✅ من ResolvedSignal: final trading decision
 * 
 * النموذج الموحد لجميع نتائج التحليل في النظام
 */
public class AnalysisResult {
    
    // ================ الحقول الأساسية الموجودة ================
    private String symbol;
    private String direction;
    private String signal;
    private double confidence;
    private List<String> signals;
    private LocalDateTime timestamp;
    private RiskRewardResult riskReward;
    
    // ================ حقول جديدة مدمجة من النماذج المحذوفة ================
    
    // من FilteredSignalResult
    private boolean hasConflicts;
    private String conflictLevel;
    private double reductionFactor;
    private String filteredRecommendation;
    private double overallConfidence;
    
    // من PatternResult  
    private String patternName;
    private String toolName;
    private SignalStrength strength;
    private String description;
    private Double targetPrice;
    private Double stopLoss;
    private String session;
    
    // من ConflictAnalysis
    private List<ConflictInfo> conflictDetails;
    private int totalConflicts;
    private String conflictSummary;
    
    // من ResolvedSignal
    private TradingAction finalAction;
    private double positionSize;
    private String safetyMessage;
    private boolean safeToTrade;
    
    // ================ Constructors ================
    
    public AnalysisResult(String symbol, String direction, String signal, double confidence,
                         List<String> signals, LocalDateTime timestamp) {
        this.symbol = symbol;
        this.direction = direction;
        this.signal = signal;
        this.confidence = confidence;
        this.signals = signals != null ? signals : new ArrayList<>();
        this.timestamp = timestamp;
        
        // القيم الافتراضية للحقول الجديدة
        this.hasConflicts = false;
        this.conflictLevel = "NONE";
        this.reductionFactor = 1.0;
        this.overallConfidence = confidence;
        this.conflictDetails = new ArrayList<>();
        this.totalConflicts = 0;
        this.safeToTrade = true;
    }
    
    public AnalysisResult(String symbol, String direction, String signal, double confidence, 
                         List<String> signals, LocalDateTime timestamp, RiskRewardResult riskReward) {
        this(symbol, direction, signal, confidence, signals, timestamp);
        this.riskReward = riskReward;
    }
    
    // ================ Enhanced Builder Pattern ================
    
    public static AnalysisResultBuilder builder() {
        return new AnalysisResultBuilder();
    }
    
    public static class AnalysisResultBuilder {
        private AnalysisResult result;
        
        public AnalysisResultBuilder() {
            this.result = new AnalysisResult("", "", "", 0.0, new ArrayList<>(), LocalDateTime.now());
        }
        
        public AnalysisResultBuilder symbol(String symbol) {
            result.symbol = symbol;
            return this;
        }
        
        public AnalysisResultBuilder direction(String direction) {
            result.direction = direction;
            return this;
        }
        
        public AnalysisResultBuilder signal(String signal) {
            result.signal = signal;
            return this;
        }
        
        public AnalysisResultBuilder confidence(double confidence) {
            result.confidence = confidence;
            result.overallConfidence = confidence;
            return this;
        }
        
        public AnalysisResultBuilder patternName(String patternName) {
            result.patternName = patternName;
            return this;
        }
        
        public AnalysisResultBuilder toolName(String toolName) {
            result.toolName = toolName;
            return this;
        }
        
        public AnalysisResultBuilder strength(SignalStrength strength) {
            result.strength = strength;
            return this;
        }
        
        public AnalysisResultBuilder conflictInfo(boolean hasConflicts, String level, double reduction) {
            result.hasConflicts = hasConflicts;
            result.conflictLevel = level;
            result.reductionFactor = reduction;
            result.overallConfidence = result.confidence * reduction;
            return this;
        }
        
        public AnalysisResultBuilder tradingDecision(TradingAction action, double positionSize, boolean safe) {
            result.finalAction = action;
            result.positionSize = positionSize;
            result.safeToTrade = safe;
            return this;
        }
        
        public AnalysisResultBuilder riskReward(RiskRewardResult riskReward) {
            result.riskReward = riskReward;
            return this;
        }
        
        public AnalysisResult build() {
            return result;
        }
    }
    
    // ================ Enums من النماذج المحذوفة ================
    
    public enum SignalDirection {
        BUY, SELL, HOLD, NEUTRAL
    }
    
    public enum SignalStrength {
        VERY_STRONG, STRONG, MODERATE, WEAK
    }
    
    public enum TradingAction {
        STRONG_BUY, BUY, HOLD, SELL, STRONG_SELL, NO_TRADE, WATCH
    }
    
    public enum ConflictSeverity {
        CRITICAL, HIGH, MEDIUM, LOW, NONE
    }
    
    // ================ Inner Classes من النماذج المحذوفة ================
    
    public static class ConflictInfo {
        private String type;
        private ConflictSeverity severity;
        private String description;
        private String recommendation;
        private double impact;
        
        public ConflictInfo(String type, ConflictSeverity severity, String description, 
                           String recommendation, double impact) {
            this.type = type;
            this.severity = severity;
            this.description = description;
            this.recommendation = recommendation;
            this.impact = impact;
    }
    
    // Getters
        public String getType() { return type; }
        public ConflictSeverity getSeverity() { return severity; }
        public String getDescription() { return description; }
        public String getRecommendation() { return recommendation; }
        public double getImpact() { return impact; }
    }
    
    // ================ Utility Methods ================
    
    /**
     * 🔍 فحص إذا كانت النتيجة آمنة للتداول
     */
    public boolean isSafeForTrading() {
        return safeToTrade && 
               overallConfidence >= 0.6 && 
               finalAction != TradingAction.NO_TRADE &&
               (!hasConflicts || conflictLevel.equals("LOW"));
    }
    
    /**
     * 📊 حساب نقاط القوة الإجمالية
     */
    public double calculateOverallScore() {
        double baseScore = overallConfidence * 100;
        
        // تقليل بناءً على التضارب
        if (hasConflicts) {
            baseScore *= reductionFactor;
        }
        
        // زيادة بناءً على قوة الإشارة
        if (strength == SignalStrength.VERY_STRONG) {
            baseScore *= 1.1;
        } else if (strength == SignalStrength.WEAK) {
            baseScore *= 0.9;
        }
        
        return Math.min(100.0, Math.max(0.0, baseScore));
    }
    
    /**
     * 📝 إنشاء ملخص شامل
     */
    public String generateComprehensiveSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("🎯 %s: %s (%s) - %.1f%% confidence", 
                                    symbol, signal, direction, confidence * 100));
        
        if (patternName != null) {
            summary.append(String.format("\n🔧 Pattern: %s (%s)", patternName, toolName));
        }
        
        if (hasConflicts) {
            summary.append(String.format("\n⚠️ Conflicts: %s level (%d conflicts)", 
                                        conflictLevel, totalConflicts));
        }
        
        if (finalAction != null) {
            summary.append(String.format("\n📈 Action: %s", finalAction));
        }
        
        if (riskReward != null) {
            summary.append(String.format("\n💰 Risk/Reward: %.2f", riskReward.getRiskRewardRatio()));
        }
        
        summary.append(String.format("\n✅ Safe to trade: %s", safeToTrade ? "Yes" : "No"));
        
        return summary.toString();
    }
    
    // ================ Getters and Setters ================
    
    // الحقول الأساسية
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    
    public String getSignal() { return signal; }
    public void setSignal(String signal) { this.signal = signal; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { 
        this.confidence = confidence;
        if (this.overallConfidence == 0) this.overallConfidence = confidence;
    }
    
    public List<String> getSignals() { return signals; }
    public void setSignals(List<String> signals) { this.signals = signals; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public RiskRewardResult getRiskReward() { return riskReward; }
    public void setRiskReward(RiskRewardResult riskReward) { this.riskReward = riskReward; }
    
    // الحقول الجديدة المدمجة
    public boolean isHasConflicts() { return hasConflicts; }
    public void setHasConflicts(boolean hasConflicts) { this.hasConflicts = hasConflicts; }
    
    public String getConflictLevel() { return conflictLevel; }
    public void setConflictLevel(String conflictLevel) { this.conflictLevel = conflictLevel; }
    
    public double getReductionFactor() { return reductionFactor; }
    public void setReductionFactor(double reductionFactor) { 
        this.reductionFactor = reductionFactor;
        this.overallConfidence = this.confidence * reductionFactor;
    }
    
    public String getFilteredRecommendation() { return filteredRecommendation; }
    public void setFilteredRecommendation(String filteredRecommendation) { 
        this.filteredRecommendation = filteredRecommendation; 
    }
    
    public double getOverallConfidence() { return overallConfidence; }
    public void setOverallConfidence(double overallConfidence) { this.overallConfidence = overallConfidence; }
    
    public String getPatternName() { return patternName; }
    public void setPatternName(String patternName) { this.patternName = patternName; }
    
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    
    public SignalStrength getStrength() { return strength; }
    public void setStrength(SignalStrength strength) { this.strength = strength; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(Double targetPrice) { this.targetPrice = targetPrice; }
    
    public Double getStopLoss() { return stopLoss; }
    public void setStopLoss(Double stopLoss) { this.stopLoss = stopLoss; }
    
    public String getSession() { return session; }
    public void setSession(String session) { this.session = session; }
    
    public List<ConflictInfo> getConflictDetails() { return conflictDetails; }
    public void setConflictDetails(List<ConflictInfo> conflictDetails) { 
        this.conflictDetails = conflictDetails;
        this.totalConflicts = conflictDetails != null ? conflictDetails.size() : 0;
        this.hasConflicts = this.totalConflicts > 0;
    }
    
    public int getTotalConflicts() { return totalConflicts; }
    public void setTotalConflicts(int totalConflicts) { this.totalConflicts = totalConflicts; }
    
    public String getConflictSummary() { return conflictSummary; }
    public void setConflictSummary(String conflictSummary) { this.conflictSummary = conflictSummary; }
    
    public TradingAction getFinalAction() { return finalAction; }
    public void setFinalAction(TradingAction finalAction) { this.finalAction = finalAction; }
    
    public double getPositionSize() { return positionSize; }
    public void setPositionSize(double positionSize) { this.positionSize = positionSize; }
    
    public String getSafetyMessage() { return safetyMessage; }
    public void setSafetyMessage(String safetyMessage) { this.safetyMessage = safetyMessage; }
    
    public boolean isSafeToTrade() { return safeToTrade; }
    public void setSafeToTrade(boolean safeToTrade) { this.safeToTrade = safeToTrade; }
    
    // ================ Additional Compatibility Methods ================
    
    /**
     * Builder method for stream operations compatibility
     */
    public AnalysisResult build() {
        return this;
    }
    
    /**
     * Stream mapping compatibility
     */
    public AnalysisResult mapToDouble(java.util.function.ToDoubleFunction<AnalysisResult> mapper) {
        // This method exists for compilation compatibility - actual functionality handled by Streams API
        return this;
    }
} 