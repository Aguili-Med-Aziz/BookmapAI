package com.bookmaai.core.enhanced;

/**
 * 🎯 Conflict Resolution Strategies
 * 
 * Enumeration defining strategies for resolving signal conflicts
 */
public enum ConflictResolutionStrategy { 
    WEIGHTED_AVERAGE,     // Use weighted average of conflicting signals
    HIGHEST_CONFIDENCE,   // Take signal with highest confidence
    MAJORITY_VOTE;        // Use majority consensus
    
    /**
     * Get resolution effectiveness score
     */
    public double getEffectivenessScore() {
        switch (this) {
            case WEIGHTED_AVERAGE: return 0.9;    // Most sophisticated
            case HIGHEST_CONFIDENCE: return 0.8;  // Good for quality
            case MAJORITY_VOTE: return 0.7;       // Simple but effective
            default: return 0.5;
        }
    }
}