package com.bookmaai.core.enhanced;

/**
 * 🎯 Composite Signal Direction Classification
 * 
 * Enumeration defining the overall direction of composite signals
 * after combining multiple individual signals
 */
public enum CompositeSignalDirection {
    BULLISH("Bullish Composite"),
    BEARISH("Bearish Composite"),
    NEUTRAL("Neutral Composite");
    
    private final String displayName;
    
    CompositeSignalDirection(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() { return displayName; }
    
    /**
     * Check if direction is bullish
     */
    public boolean isBullish() {
        return this == BULLISH;
    }
    
    /**
     * Check if direction is bearish
     */
    public boolean isBearish() {
        return this == BEARISH;
    }
    
    /**
     * Check if direction is neutral
     */
    public boolean isNeutral() {
        return this == NEUTRAL;
    }
    
    /**
     * Get the opposite direction
     */
    public CompositeSignalDirection getOpposite() {
        switch (this) {
            case BULLISH: return BEARISH;
            case BEARISH: return BULLISH;
            case NEUTRAL: return NEUTRAL;
            default: return NEUTRAL;
        }
    }
    
    /**
     * Get direction from signal consensus
     * @param bullishSignals Number of bullish signals
     * @param bearishSignals Number of bearish signals
     * @param neutralSignals Number of neutral signals
     */
    public static CompositeSignalDirection fromConsensus(int bullishSignals, int bearishSignals, int neutralSignals) {
        if (bullishSignals > bearishSignals && bullishSignals > neutralSignals) {
            return BULLISH;
        } else if (bearishSignals > bullishSignals && bearishSignals > neutralSignals) {
            return BEARISH;
        } else {
            return NEUTRAL;
        }
    }
}