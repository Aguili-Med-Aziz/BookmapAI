package com.bookmaai.core.enhanced;

/**
 * 🎯 Signal Consensus Classification
 * 
 * Enumeration defining levels of consensus among multiple signals
 */
public enum SignalConsensus { 
    CONSENSUS,         // All signals agree
    PARTIAL_CONSENSUS, // Most signals agree
    NO_CONSENSUS;      // Signals disagree
    
    /**
     * Get consensus from agreement ratio
     */
    public static SignalConsensus fromAgreementRatio(double ratio) {
        if (ratio >= 0.8) return CONSENSUS;
        if (ratio >= 0.6) return PARTIAL_CONSENSUS;
        return NO_CONSENSUS;
    }
    
    /**
     * Get confidence multiplier based on consensus
     */
    public double getConfidenceMultiplier() {
        switch (this) {
            case CONSENSUS: return 1.2;
            case PARTIAL_CONSENSUS: return 1.0;
            case NO_CONSENSUS: return 0.7;
            default: return 1.0;
        }
    }
}