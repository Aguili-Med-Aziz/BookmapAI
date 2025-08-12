package com.bookmaai.core.enhanced;

/**
 * 🎯 Comprehensive Absorption Analysis Result
 * 
 * Main result class for Advanced Absorption Analysis containing all
 * sub-analysis results and overall absorption assessment
 */
public class AbsorptionAnalysisResult {
    private final String symbol;
    private final double price;
    private final long timestamp;
    
    private VolumeWeightedAbsorption volumeWeightedAbsorption;
    private MultiLevelAbsorption multiLevelAbsorption;
    private TimeDecayAnalysis timeDecayAnalysis;
    private MarketParticipantClassification participantClassification;
    private AbsorptionStrengthAnalysis strengthAnalysis;
    
    private double confidence;
    private boolean absorptionDetected;
    private AbsorptionStrength absorptionStrength;
    
    public AbsorptionAnalysisResult(String symbol, double price, long timestamp) {
        this.symbol = symbol;
        this.price = price;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public long getTimestamp() { return timestamp; }
    
    public VolumeWeightedAbsorption getVolumeWeightedAbsorption() { return volumeWeightedAbsorption; }
    public void setVolumeWeightedAbsorption(VolumeWeightedAbsorption volumeWeightedAbsorption) { 
        this.volumeWeightedAbsorption = volumeWeightedAbsorption; }
    
    public MultiLevelAbsorption getMultiLevelAbsorption() { return multiLevelAbsorption; }
    public void setMultiLevelAbsorption(MultiLevelAbsorption multiLevelAbsorption) { 
        this.multiLevelAbsorption = multiLevelAbsorption; }
    
    public TimeDecayAnalysis getTimeDecayAnalysis() { return timeDecayAnalysis; }
    public void setTimeDecayAnalysis(TimeDecayAnalysis timeDecayAnalysis) { 
        this.timeDecayAnalysis = timeDecayAnalysis; }
    
    public MarketParticipantClassification getParticipantClassification() { return participantClassification; }
    public void setParticipantClassification(MarketParticipantClassification participantClassification) { 
        this.participantClassification = participantClassification; }
    
    public AbsorptionStrengthAnalysis getStrengthAnalysis() { return strengthAnalysis; }
    public void setStrengthAnalysis(AbsorptionStrengthAnalysis strengthAnalysis) { 
        this.strengthAnalysis = strengthAnalysis; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    
    public boolean isAbsorptionDetected() { return absorptionDetected; }
    public void setAbsorptionDetected(boolean absorptionDetected) { this.absorptionDetected = absorptionDetected; }
    
    public AbsorptionStrength getAbsorptionStrength() { return absorptionStrength; }
    public void setAbsorptionStrength(AbsorptionStrength absorptionStrength) { this.absorptionStrength = absorptionStrength; }
    
    /**
     * Get overall analysis quality score
     */
    public double getOverallQualityScore() {
        if (!absorptionDetected) return 0.0;
        
        double qualityScore = confidence;
        
        // Adjust based on sub-analysis results
        if (volumeWeightedAbsorption != null) {
            qualityScore *= 0.9 + (volumeWeightedAbsorption.getTotalWeightedVolume() / 10000.0) * 0.1;
        }
        
        if (participantClassification != null && participantClassification.isMarketMakerAbsorption()) {
            qualityScore *= 1.1; // Boost for market maker involvement
        }
        
        return Math.min(100.0, qualityScore);
    }
    
    /**
     * Get absorption persistence in minutes
     */
    public double getAbsorptionPersistenceMinutes() {
        if (timeDecayAnalysis != null) {
            return timeDecayAnalysis.getTimeDurationMinutes();
        }
        return 0.0;
    }
    
    /**
     * Check if this is a high-confidence absorption pattern
     */
    public boolean isHighConfidencePattern() {
        return absorptionDetected && confidence >= 85.0 && 
               absorptionStrength != null && absorptionStrength.getLevel() >= 2;
    }
    
    @Override
    public String toString() {
        return String.format("AbsorptionAnalysisResult{symbol='%s', price=%.4f, detected=%s, confidence=%.1f%%, strength=%s}", 
                           symbol, price, absorptionDetected, confidence, 
                           absorptionStrength != null ? absorptionStrength.getDisplayName() : "N/A");
    }
}

// Supporting classes that don't need to be public
class VolumeWeightedAbsorption {
    private double totalWeightedVolume;
    private double averagePrice;
    private double volumeDistribution;
    
    public VolumeWeightedAbsorption(double totalWeightedVolume, double averagePrice, double volumeDistribution) {
        this.totalWeightedVolume = totalWeightedVolume;
        this.averagePrice = averagePrice;
        this.volumeDistribution = volumeDistribution;
    }
    
    public double getTotalWeightedVolume() { return totalWeightedVolume; }
    public double getAveragePrice() { return averagePrice; }
    public double getVolumeDistribution() { return volumeDistribution; }
}

class MultiLevelAbsorption {
    private double[] levelStrengths;
    private double[] levelVolumes;
    private int activeLevels;
    
    public MultiLevelAbsorption(double[] levelStrengths, double[] levelVolumes, int activeLevels) {
        this.levelStrengths = levelStrengths;
        this.levelVolumes = levelVolumes;
        this.activeLevels = activeLevels;
    }
    
    public double[] getLevelStrengths() { return levelStrengths; }
    public double[] getLevelVolumes() { return levelVolumes; }
    public int getActiveLevels() { return activeLevels; }
}

class TimeDecayAnalysis {
    private double decayFactor;
    private double timeDurationMinutes;
    private double persistence;
    
    public TimeDecayAnalysis(double decayFactor, double timeDurationMinutes, double persistence) {
        this.decayFactor = decayFactor;
        this.timeDurationMinutes = timeDurationMinutes;
        this.persistence = persistence;
    }
    
    public double getDecayFactor() { return decayFactor; }
    public double getTimeDurationMinutes() { return timeDurationMinutes; }
    public double getPersistence() { return persistence; }
}

class MarketParticipantClassification {
    private ParticipantType participantType;
    private boolean isMarketMakerAbsorption;
    private double institutionalProbability;
    
    public MarketParticipantClassification(ParticipantType participantType, boolean isMarketMakerAbsorption, double institutionalProbability) {
        this.participantType = participantType;
        this.isMarketMakerAbsorption = isMarketMakerAbsorption;
        this.institutionalProbability = institutionalProbability;
    }
    
    public ParticipantType getParticipantType() { return participantType; }
    public boolean isMarketMakerAbsorption() { return isMarketMakerAbsorption; }
    public double getInstitutionalProbability() { return institutionalProbability; }
}

class AbsorptionStrengthAnalysis {
    private AbsorptionStrength strength;
    private double strengthScore;
    private AbsorptionStyle style;
    
    public AbsorptionStrengthAnalysis(AbsorptionStrength strength, double strengthScore, AbsorptionStyle style) {
        this.strength = strength;
        this.strengthScore = strengthScore;
        this.style = style;
    }
    
    public AbsorptionStrength getStrength() { return strength; }
    public double getStrengthScore() { return strengthScore; }
    public AbsorptionStyle getStyle() { return style; }
}