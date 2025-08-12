package com.bookmaai.core.enhanced;

/**
 * 🎯 Comprehensive Composite Signal Result
 * 
 * Main result class for Composite Signal Processing containing all
 * signal analysis results and final decision-making structures
 */
public class CompositeSignalResult {
    private final String symbol;
    private final double price;
    private final long timestamp;
    
    private IndividualSignalAnalysis individualSignalAnalysis;
    private SignalCorrelationAnalysis signalCorrelationAnalysis;
    private WeightedSignalCombination weightedSignalCombination;
    private ConflictResolutionAnalysis conflictResolutionAnalysis;
    private MultiTimeframeSignalSynthesis multiTimeframeSignalSynthesis;
    private AdaptiveLearningResults adaptiveLearningResults;
    private CompositeSignalDecision compositeSignalDecision;
    
    public CompositeSignalResult(String symbol, double price, long timestamp) {
        this.symbol = symbol;
        this.price = price;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public long getTimestamp() { return timestamp; }
    
    public IndividualSignalAnalysis getIndividualSignalAnalysis() { return individualSignalAnalysis; }
    public void setIndividualSignalAnalysis(IndividualSignalAnalysis individualSignalAnalysis) { 
        this.individualSignalAnalysis = individualSignalAnalysis; }
    
    public SignalCorrelationAnalysis getSignalCorrelationAnalysis() { return signalCorrelationAnalysis; }
    public void setSignalCorrelationAnalysis(SignalCorrelationAnalysis signalCorrelationAnalysis) { 
        this.signalCorrelationAnalysis = signalCorrelationAnalysis; }
    
    public WeightedSignalCombination getWeightedSignalCombination() { return weightedSignalCombination; }
    public void setWeightedSignalCombination(WeightedSignalCombination weightedSignalCombination) { 
        this.weightedSignalCombination = weightedSignalCombination; }
    
    public ConflictResolutionAnalysis getConflictResolutionAnalysis() { return conflictResolutionAnalysis; }
    public void setConflictResolutionAnalysis(ConflictResolutionAnalysis conflictResolutionAnalysis) { 
        this.conflictResolutionAnalysis = conflictResolutionAnalysis; }
    
    public MultiTimeframeSignalSynthesis getMultiTimeframeSignalSynthesis() { return multiTimeframeSignalSynthesis; }
    public void setMultiTimeframeSignalSynthesis(MultiTimeframeSignalSynthesis multiTimeframeSignalSynthesis) { 
        this.multiTimeframeSignalSynthesis = multiTimeframeSignalSynthesis; }
    
    public AdaptiveLearningResults getAdaptiveLearningResults() { return adaptiveLearningResults; }
    public void setAdaptiveLearningResults(AdaptiveLearningResults adaptiveLearningResults) { 
        this.adaptiveLearningResults = adaptiveLearningResults; }
    
    public CompositeSignalDecision getCompositeSignalDecision() { return compositeSignalDecision; }
    public void setCompositeSignalDecision(CompositeSignalDecision compositeSignalDecision) { 
        this.compositeSignalDecision = compositeSignalDecision; }
    
    /**
     * Get overall signal quality score
     */
    public double getOverallQualityScore() {
        if (compositeSignalDecision == null) return 0.0;
        return compositeSignalDecision.getQualityScore();
    }
    
    /**
     * Check if this represents a high-confidence composite signal
     */
    public boolean isHighConfidenceSignal() {
        return compositeSignalDecision != null && 
               compositeSignalDecision.getFinalConfidence() >= 85.0 &&
               compositeSignalDecision.getSignalStrength() != CompositeSignalStrength.NONE;
    }
    
    /**
     * Get recommended position size based on signal strength and confidence
     */
    public double getRecommendedPositionSize(double baseSize) {
        if (compositeSignalDecision == null) return 0.0;
        
        double multiplier = 1.0;
        
        // Adjust based on signal strength
        switch (compositeSignalDecision.getSignalStrength()) {
            case VERY_STRONG: multiplier = 1.5; break;
            case STRONG: multiplier = 1.2; break;
            case MODERATE: multiplier = 1.0; break;
            case WEAK: multiplier = 0.5; break;
            case NONE: return 0.0;
        }
        
        // Adjust based on confidence
        double confidenceFactor = compositeSignalDecision.getFinalConfidence() / 100.0;
        
        // Adjust based on risk level
        double riskFactor = 1.0 - (compositeSignalDecision.getRiskLevel() / 100.0);
        
        return baseSize * multiplier * confidenceFactor * riskFactor;
    }
    
    @Override
    public String toString() {
        return String.format("CompositeSignalResult{symbol='%s', price=%.4f, decision=%s}", 
                           symbol, price, 
                           compositeSignalDecision != null ? compositeSignalDecision.toString() : "N/A");
    }
}

// Supporting classes that don't need to be public
class IndividualSignalAnalysis {
    private java.util.Map<String, Double> signalStrengths;
    private java.util.Map<String, Double> signalConfidences;
    private java.util.Map<String, SignalDirection> signalDirections;
    
    public IndividualSignalAnalysis(java.util.Map<String, Double> signalStrengths,
                                  java.util.Map<String, Double> signalConfidences,
                                  java.util.Map<String, SignalDirection> signalDirections) {
        this.signalStrengths = signalStrengths;
        this.signalConfidences = signalConfidences;
        this.signalDirections = signalDirections;
    }
    
    public java.util.Map<String, Double> getSignalStrengths() { return signalStrengths; }
    public java.util.Map<String, Double> getSignalConfidences() { return signalConfidences; }
    public java.util.Map<String, SignalDirection> getSignalDirections() { return signalDirections; }
}

class SignalCorrelationAnalysis {
    private double[][] correlationMatrix;
    private double averageCorrelation;
    private CorrelationTrend correlationTrend;
    
    public SignalCorrelationAnalysis(double[][] correlationMatrix, double averageCorrelation, CorrelationTrend correlationTrend) {
        this.correlationMatrix = correlationMatrix;
        this.averageCorrelation = averageCorrelation;
        this.correlationTrend = correlationTrend;
    }
    
    public double[][] getCorrelationMatrix() { return correlationMatrix; }
    public double getAverageCorrelation() { return averageCorrelation; }
    public CorrelationTrend getCorrelationTrend() { return correlationTrend; }
}

class WeightedSignalCombination {
    private java.util.Map<String, Double> signalWeights;
    private double combinedSignalStrength;
    private SignalConsensus consensus;
    
    public WeightedSignalCombination(java.util.Map<String, Double> signalWeights, 
                                   double combinedSignalStrength, 
                                   SignalConsensus consensus) {
        this.signalWeights = signalWeights;
        this.combinedSignalStrength = combinedSignalStrength;
        this.consensus = consensus;
    }
    
    public java.util.Map<String, Double> getSignalWeights() { return signalWeights; }
    public double getCombinedSignalStrength() { return combinedSignalStrength; }
    public SignalConsensus getConsensus() { return consensus; }
}

class ConflictResolutionAnalysis {
    private java.util.List<ConflictType> detectedConflicts;
    private ConflictResolutionStrategy resolutionStrategy;
    private double resolutionConfidence;
    
    public ConflictResolutionAnalysis(java.util.List<ConflictType> detectedConflicts,
                                    ConflictResolutionStrategy resolutionStrategy,
                                    double resolutionConfidence) {
        this.detectedConflicts = detectedConflicts;
        this.resolutionStrategy = resolutionStrategy;
        this.resolutionConfidence = resolutionConfidence;
    }
    
    public java.util.List<ConflictType> getDetectedConflicts() { return detectedConflicts; }
    public ConflictResolutionStrategy getResolutionStrategy() { return resolutionStrategy; }
    public double getResolutionConfidence() { return resolutionConfidence; }
    
    public boolean hasConflicts() { return detectedConflicts != null && !detectedConflicts.isEmpty(); }
}

class MultiTimeframeSignalSynthesis {
    private java.util.Map<String, TimeframeSignalAlignment> timeframeAlignments;
    private double synthesisPower;
    private PatternType dominantPattern;
    
    public MultiTimeframeSignalSynthesis(java.util.Map<String, TimeframeSignalAlignment> timeframeAlignments,
                                       double synthesisPower,
                                       PatternType dominantPattern) {
        this.timeframeAlignments = timeframeAlignments;
        this.synthesisPower = synthesisPower;
        this.dominantPattern = dominantPattern;
    }
    
    public java.util.Map<String, TimeframeSignalAlignment> getTimeframeAlignments() { return timeframeAlignments; }
    public double getSynthesisPower() { return synthesisPower; }
    public PatternType getDominantPattern() { return dominantPattern; }
}

class AdaptiveLearningResults {
    private double performanceScore;
    private java.util.Map<String, Double> adaptedWeights;
    private double learningRate;
    
    public AdaptiveLearningResults(double performanceScore, 
                                 java.util.Map<String, Double> adaptedWeights,
                                 double learningRate) {
        this.performanceScore = performanceScore;
        this.adaptedWeights = adaptedWeights;
        this.learningRate = learningRate;
    }
    
    public double getPerformanceScore() { return performanceScore; }
    public java.util.Map<String, Double> getAdaptedWeights() { return adaptedWeights; }
    public double getLearningRate() { return learningRate; }
}

class CompositeSignalDecision {
    private CompositeSignalStrength signalStrength;
    private CompositeSignalDirection signalDirection;
    private SignalAction recommendedAction;
    private double finalConfidence;
    private double riskLevel;
    private double qualityScore;
    
    public CompositeSignalDecision(CompositeSignalStrength signalStrength,
                                 CompositeSignalDirection signalDirection,
                                 SignalAction recommendedAction,
                                 double finalConfidence,
                                 double riskLevel,
                                 double qualityScore) {
        this.signalStrength = signalStrength;
        this.signalDirection = signalDirection;
        this.recommendedAction = recommendedAction;
        this.finalConfidence = finalConfidence;
        this.riskLevel = riskLevel;
        this.qualityScore = qualityScore;
    }
    
    public CompositeSignalStrength getSignalStrength() { return signalStrength; }
    public CompositeSignalDirection getSignalDirection() { return signalDirection; }
    public SignalAction getRecommendedAction() { return recommendedAction; }
    public double getFinalConfidence() { return finalConfidence; }
    public double getRiskLevel() { return riskLevel; }
    public double getQualityScore() { return qualityScore; }
    
    @Override
    public String toString() {
        return String.format("Decision{strength=%s, direction=%s, action=%s, confidence=%.1f%%}",
                           signalStrength, signalDirection, recommendedAction, finalConfidence);
    }
}