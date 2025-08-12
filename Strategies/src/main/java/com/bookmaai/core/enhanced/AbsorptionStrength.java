package com.bookmaai.core.enhanced;

/**
 * 🎯 Absorption Strength Classification
 * 
 * Enumeration defining different levels of absorption pattern strength
 * from none to strong absorption detection
 */
public enum AbsorptionStrength {
    NONE("No Absorption", 0),
    WEAK("Weak Absorption", 1),
    MODERATE("Moderate Absorption", 2),
    STRONG("Strong Absorption", 3);
    
    private final String displayName;
    private final int level;
    
    AbsorptionStrength(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }
    
    public String getDisplayName() { return displayName; }
    public int getLevel() { return level; }
    
    /**
     * Check if this strength indicates a significant absorption
     */
    public boolean isSignificant() {
        return level >= 2; // Moderate or Strong
    }
    
    /**
     * Get the next higher strength level
     */
    public AbsorptionStrength getNext() {
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
    public static AbsorptionStrength fromConfidence(double confidence) {
        if (confidence >= 85.0) return STRONG;
        if (confidence >= 70.0) return MODERATE;
        if (confidence >= 50.0) return WEAK;
        return NONE;
    }
}