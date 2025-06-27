package com.strategies.dom.analysis.strategy;

public class DualTimeFrameValidator {
    
    private final AdaptiveThresholdSystem thresholdSystem;
    
    public DualTimeFrameValidator(AdaptiveThresholdSystem thresholdSystem) {
        this.thresholdSystem = thresholdSystem;
    }
    
    // Validate Iceberg signal across both timeframes
    public boolean validateIcebergSignal(double volume15m, double volume30m, 
                                       double price, double currentVwap) {
        
        AdaptiveThresholdSystem.DynamicThresholds thresholds15m = thresholdSystem.getPrimaryThresholds();
        AdaptiveThresholdSystem.DynamicThresholds thresholds30m = thresholdSystem.getConfirmationThresholds();
        
        // Check 15 minute timeframe
        boolean valid15m = volume15m >= thresholds15m.getIcebergMinVolume() &&
                          Math.abs(price - currentVwap) <= thresholds15m.getVwapDistanceMax();
        
        // Check 30 minute timeframe
        boolean valid30m = volume30m >= thresholds30m.getIcebergMinVolume() &&
                          Math.abs(price - currentVwap) <= thresholds30m.getVwapDistanceMax();
        
        return valid15m && valid30m;
    }
    
    // Validate Absorption signal across both timeframes
    public boolean validateAbsorptionSignal(double absorption15m, double absorption30m,
                                           double speed15m, double speed30m) {
        
        AdaptiveThresholdSystem.DynamicThresholds thresholds15m = thresholdSystem.getPrimaryThresholds();
        AdaptiveThresholdSystem.DynamicThresholds thresholds30m = thresholdSystem.getConfirmationThresholds();
        
        // Check 15 minute timeframe
        boolean valid15m = absorption15m >= thresholds15m.getAbsorptionMinVolume() &&
                          speed15m >= thresholds15m.getAbsorptionSpeedFactor();
        
        // Check 30 minute timeframe
        boolean valid30m = absorption30m >= thresholds30m.getAbsorptionMinVolume() &&
                          speed30m >= thresholds30m.getAbsorptionSpeedFactor();
        
        return valid15m && valid30m;
    }
    
    // Validate Heatmap signal across both timeframes
    public boolean validateHeatmapSignal(double intensity15m, double intensity30m,
                                       double colorStrength15m, double colorStrength30m) {
        
        AdaptiveThresholdSystem.DynamicThresholds thresholds15m = thresholdSystem.getPrimaryThresholds();
        AdaptiveThresholdSystem.DynamicThresholds thresholds30m = thresholdSystem.getConfirmationThresholds();
        
        // Check 15 minute timeframe
        boolean valid15m = intensity15m >= thresholds15m.getHeatmapThreshold() &&
                          colorStrength15m >= thresholds15m.getHeatmapColorThreshold();
        
        // Check 30 minute timeframe
        boolean valid30m = intensity30m >= thresholds30m.getHeatmapThreshold() &&
                          colorStrength30m >= thresholds30m.getHeatmapColorThreshold();
        
        return valid15m && valid30m;
    }
    
    // Validate Delta Imbalance signal across both timeframes
    public boolean validateDeltaImbalanceSignal(double delta15m, double delta30m,
                                               int ratio15m, int ratio30m) {
        
        AdaptiveThresholdSystem.DynamicThresholds thresholds15m = thresholdSystem.getPrimaryThresholds();
        AdaptiveThresholdSystem.DynamicThresholds thresholds30m = thresholdSystem.getConfirmationThresholds();
        
        // Check 15 minute timeframe
        boolean valid15m = delta15m >= thresholds15m.getDeltaImbalanceThreshold() &&
                          ratio15m >= thresholds15m.getDeltaMinRatio();
        
        // Check 30 minute timeframe
        boolean valid30m = delta30m >= thresholds30m.getDeltaImbalanceThreshold() &&
                          ratio30m >= thresholds30m.getDeltaMinRatio();
        
        return valid15m && valid30m;
    }
    
    // Validate Sweep signal across both timeframes
    public boolean validateSweepSignal(double sweepVolume15m, double sweepVolume30m,
                                     double liquidityThreshold15m, double liquidityThreshold30m) {
        
        AdaptiveThresholdSystem.DynamicThresholds thresholds15m = thresholdSystem.getPrimaryThresholds();
        AdaptiveThresholdSystem.DynamicThresholds thresholds30m = thresholdSystem.getConfirmationThresholds();
        
        // Check 15 minute timeframe
        boolean valid15m = sweepVolume15m >= thresholds15m.getSweepMinVolume() &&
                          liquidityThreshold15m >= thresholds15m.getSweepLiquidityThreshold();
        
        // Check 30 minute timeframe
        boolean valid30m = sweepVolume30m >= thresholds30m.getSweepMinVolume() &&
                          liquidityThreshold30m >= thresholds30m.getSweepLiquidityThreshold();
        
        return valid15m && valid30m;
    }
    
    // Calculate combined score for signal across timeframes
    public int calculateCombinedScore(double score15m, double score30m, String patternType) {
        
        // Weight: 40% for 15m, 60% for 30m
        double weightedScore = (score15m * 0.4) + (score30m * 0.6);
        
        // Add bonus for strong alignment
        if (Math.abs(score15m - score30m) <= 10) { // Strong alignment
            weightedScore *= 1.2; // 20% bonus
        }
        
        // Adjust based on pattern type
        switch (patternType) {
            case "Iceberg":
                weightedScore *= 1.1; // 10% bonus for Iceberg
                break;
            case "Absorption":
                weightedScore *= 1.15; // 15% bonus for Absorption
                break;
            case "Heatmap":
                weightedScore *= 1.05; // 5% bonus for Heatmap
                break;
            case "DeltaImbalance":
                weightedScore *= 1.08; // 8% bonus for Delta
                break;
            case "Sweep":
                weightedScore *= 1.12; // 12% bonus for Sweep
                break;
        }
        
        return (int) Math.round(weightedScore);
    }
    
    // Calculate pattern strength across timeframes
    public double calculatePatternStrength(double strength15m, double strength30m) {
        // Both timeframes must show positive strength
        if (strength15m <= 0 || strength30m <= 0) {
            return 0.0;
        }
        
        // Calculate weighted average
        double weightedStrength = (strength15m * 0.4) + (strength30m * 0.6);
        
        // Add alignment bonus
        double alignmentFactor = 1.0 - (Math.abs(strength15m - strength30m) / Math.max(strength15m, strength30m));
        
        return weightedStrength * (1.0 + alignmentFactor * 0.2); // Up to 20% alignment bonus
    }
    
    // Validate overall signal quality
    public boolean isHighQualitySignal(String patternType, double strength15m, double strength30m,
                                     double volume15m, double volume30m) {
        
        // Minimum strength requirements
        boolean strongEnough = strength15m >= 0.6 && strength30m >= 0.6;
        
        // Volume requirements based on pattern type
        boolean sufficientVolume = validateVolumeRequirements(patternType, volume15m, volume30m);
        
        // Timeframe alignment
        boolean aligned = thresholdSystem.areTimeFramesAligned(patternType, strength15m, strength30m);
        
        // Combined score threshold
        int combinedScore = calculateCombinedScore(strength15m * 100, strength30m * 100, patternType);
        boolean scoreMeetsThreshold = combinedScore >= 75; // Minimum 75 points
        
        return strongEnough && sufficientVolume && aligned && scoreMeetsThreshold;
    }
    
    private boolean validateVolumeRequirements(String patternType, double volume15m, double volume30m) {
        AdaptiveThresholdSystem.DynamicThresholds thresholds15m = thresholdSystem.getPrimaryThresholds();
        AdaptiveThresholdSystem.DynamicThresholds thresholds30m = thresholdSystem.getConfirmationThresholds();
        
        switch (patternType) {
            case "Iceberg":
                return volume15m >= thresholds15m.getIcebergMinVolume() && 
                       volume30m >= thresholds30m.getIcebergMinVolume();
            case "Absorption":
                return volume15m >= thresholds15m.getAbsorptionMinVolume() && 
                       volume30m >= thresholds30m.getAbsorptionMinVolume();
            case "Sweep":
                return volume15m >= thresholds15m.getSweepMinVolume() && 
                       volume30m >= thresholds30m.getSweepMinVolume();
            default:
                return volume15m >= 50 && volume30m >= 80; // Default volume requirements
        }
    }
    
    // Generate signal recommendation
    public SignalRecommendation generateRecommendation(String patternType, 
                                                     double strength15m, double strength30m,
                                                     double volume15m, double volume30m,
                                                     double price, double vwap) {
        
        boolean isHighQuality = isHighQualitySignal(patternType, strength15m, strength30m, volume15m, volume30m);
        double combinedStrength = calculatePatternStrength(strength15m, strength30m);
        int combinedScore = calculateCombinedScore(strength15m * 100, strength30m * 100, patternType);
        
        String recommendation;
        String confidence;
        
        if (isHighQuality && combinedScore >= 85) {
            recommendation = "STRONG_ENTRY";
            confidence = "HIGH";
        } else if (isHighQuality && combinedScore >= 75) {
            recommendation = "ENTRY";
            confidence = "MEDIUM";
        } else if (combinedScore >= 60) {
            recommendation = "WATCH";
            confidence = "LOW";
        } else {
            recommendation = "IGNORE";
            confidence = "NONE";
        }
        
        return new SignalRecommendation(patternType, recommendation, confidence, 
                                      combinedScore, combinedStrength, price, vwap);
    }
    
    // Signal recommendation class
    public static class SignalRecommendation {
        public final String patternType;
        public final String recommendation;
        public final String confidence;
        public final int score;
        public final double strength;
        public final double price;
        public final double vwap;
        public final long timestamp;
        
        public SignalRecommendation(String patternType, String recommendation, String confidence,
                                  int score, double strength, double price, double vwap) {
            this.patternType = patternType;
            this.recommendation = recommendation;
            this.confidence = confidence;
            this.score = score;
            this.strength = strength;
            this.price = price;
            this.vwap = vwap;
            this.timestamp = System.currentTimeMillis();
        }
        
        @Override
        public String toString() {
            return String.format("Signal[%s]: %s (%s confidence) - Score: %d, Strength: %.2f, Price: %.2f", 
                               patternType, recommendation, confidence, score, strength, price);
        }
    }
} 