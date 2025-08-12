package com.bookmaai.core.enhanced;

/**
 * 🎯 Individual Signal Direction Classification
 * 
 * Enumeration defining the direction of individual signals
 * before composite signal processing
 */
public enum SignalDirection {
    BULLISH("Bullish"),
    BEARISH("Bearish"),
    NEUTRAL("Neutral");
    
    private final String displayName;
    
    SignalDirection(String displayName) {
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
     * Convert to composite signal direction
     */
    public CompositeSignalDirection toCompositeDirection() {
        switch (this) {
            case BULLISH: return CompositeSignalDirection.BULLISH;
            case BEARISH: return CompositeSignalDirection.BEARISH;
            case NEUTRAL: return CompositeSignalDirection.NEUTRAL;
            default: return CompositeSignalDirection.NEUTRAL;
        }
    }
}