package com.bookmaai.core.enhanced;

/**
 * 🎯 Pattern Type Classification
 * 
 * Enumeration defining types of market patterns detected
 */
public enum PatternType { 
    REVERSAL,        // Pattern indicates trend reversal
    CONTINUATION,    // Pattern indicates trend continuation
    BREAKOUT,        // Pattern indicates breakout from range
    CONSOLIDATION;   // Pattern indicates consolidation/range
    
    /**
     * Get typical success rate for this pattern type
     */
    public double getTypicalSuccessRate() {
        switch (this) {
            case BREAKOUT: return 0.85;      // High success rate
            case CONTINUATION: return 0.80;  // Good success rate
            case REVERSAL: return 0.75;      // Moderate success rate
            case CONSOLIDATION: return 0.65; // Lower success rate
            default: return 0.70;
        }
    }
    
    /**
     * Check if pattern suggests directional movement
     */
    public boolean isDirectional() {
        return this == REVERSAL || this == BREAKOUT || this == CONTINUATION;
    }
}