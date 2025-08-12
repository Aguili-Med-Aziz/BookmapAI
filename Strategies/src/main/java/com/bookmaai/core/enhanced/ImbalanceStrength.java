package com.bookmaai.core.enhanced;

/**
 * 🎯 Imbalance Strength Classification
 * 
 * Enumeration defining different levels of market imbalance strength
 * from none to strong imbalance detection
 */
public enum ImbalanceStrength {
    NONE("No Imbalance", 0),
    WEAK("Weak Imbalance", 1),
    MODERATE("Moderate Imbalance", 2),
    STRONG("Strong Imbalance", 3);
    
    private final String displayName;
    private final int level;
    
    ImbalanceStrength(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }
    
    public String getDisplayName() { return displayName; }
    public int getLevel() { return level; }
    
    /**
     * Check if this strength indicates a significant imbalance
     */
    public boolean isSignificant() {
        return level >= 2; // Moderate or Strong
    }
    
    /**
     * Get the next higher strength level
     */
    public ImbalanceStrength getNext() {
        switch (this) {
            case NONE: return WEAK;
            case WEAK: return MODERATE;
            case MODERATE: return STRONG;
            case STRONG: return STRONG; // Already at max
            default: return this;
        }
    }
    
    /**
     * Get strength from confidence percentage
     */
    public static ImbalanceStrength fromConfidence(double confidence) {
        if (confidence >= 85.0) return STRONG;
        if (confidence >= 70.0) return MODERATE;
        if (confidence >= 50.0) return WEAK;
        return NONE;
    }
}