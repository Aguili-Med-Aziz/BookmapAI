package com.bookmaai.core.enhanced;

/**
 * 🎯 Timeframe Signal Alignment
 * 
 * Enumeration defining alignment of signals across different timeframes
 */
public enum TimeframeSignalAlignment { 
    ALIGNED,            // All timeframes agree
    PARTIALLY_ALIGNED,  // Some timeframes agree
    DIVERGENT;          // Timeframes disagree
    
    /**
     * Get alignment confidence multiplier
     */
    public double getConfidenceMultiplier() {
        switch (this) {
            case ALIGNED: return 1.3;           // Strong boost for alignment
            case PARTIALLY_ALIGNED: return 1.0; // No change
            case DIVERGENT: return 0.8;         // Reduce for divergence
            default: return 1.0;
        }
    }
}