package com.bookmaai.core.enhanced;

/**
 * 🎯 Timeframe Alignment Classification
 * 
 * Enumeration defining alignment of imbalances across different timeframes
 * for multi-timeframe analysis
 */
public enum TimeframeAlignment {
    BULLISH_ALIGNED("Bullish Aligned"),
    BEARISH_ALIGNED("Bearish Aligned"),
    DIVERGENT("Divergent");
    
    private final String displayName;
    
    TimeframeAlignment(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() { return displayName; }
    
    /**
     * Check if alignment is bullish across timeframes
     */
    public boolean isBullishAlignment() {
        return this == BULLISH_ALIGNED;
    }
    
    /**
     * Check if alignment is bearish across timeframes
     */
    public boolean isBearishAlignment() {
        return this == BEARISH_ALIGNED;
    }
    
    /**
     * Check if timeframes are divergent
     */
    public boolean isDivergent() {
        return this == DIVERGENT;
    }
    
    /**
     * Get alignment confidence factor
     * @return Confidence multiplier (higher for aligned, lower for divergent)
     */
    public double getConfidenceFactor() {
        switch (this) {
            case BULLISH_ALIGNED:
            case BEARISH_ALIGNED:
                return 1.2; // Boost confidence when aligned
            case DIVERGENT:
                return 0.8; // Reduce confidence when divergent
            default:
                return 1.0;
        }
    }
    
    /**
     * Determine alignment from multiple timeframe directions
     * @param shortTimeframeBullish Short timeframe is bullish
     * @param mediumTimeframeBullish Medium timeframe is bullish
     * @param longTimeframeBullish Long timeframe is bullish
     */
    public static TimeframeAlignment fromTimeframeDirections(boolean shortTimeframeBullish, 
                                                           boolean mediumTimeframeBullish, 
                                                           boolean longTimeframeBullish) {
        int bullishCount = 0;
        if (shortTimeframeBullish) bullishCount++;
        if (mediumTimeframeBullish) bullishCount++;
        if (longTimeframeBullish) bullishCount++;
        
        if (bullishCount >= 2) return BULLISH_ALIGNED;
        if (bullishCount <= 1) return BEARISH_ALIGNED;
        return DIVERGENT;
    }
}