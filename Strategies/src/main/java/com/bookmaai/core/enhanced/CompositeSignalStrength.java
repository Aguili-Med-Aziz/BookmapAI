package com.bookmaai.core.enhanced;

/**
 * 🎯 Composite Signal Strength Classification
 * 
 * Enumeration defining different levels of composite signal strength
 * from none to very strong signals
 */
public enum CompositeSignalStrength {
    NONE("No Signal", 0),
    WEAK("Weak Signal", 1),
    MODERATE("Moderate Signal", 2),
    STRONG("Strong Signal", 3),
    VERY_STRONG("Very Strong Signal", 4);
    
    private final String displayName;
    private final int level;
    
    CompositeSignalStrength(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }
    
    public String getDisplayName() { return displayName; }
    public int getLevel() { return level; }
    
    /**
     * Check if this strength indicates a tradeable signal
     */
    public boolean isTradeable() {
        return level >= 2; // Moderate or higher
    }
    
    /**
     * Check if this is a high-conviction signal
     */
    public boolean isHighConviction() {
        return level >= 3; // Strong or Very Strong
    }
    
    /**
     * Get position size multiplier based on signal strength
     */
    public double getPositionSizeMultiplier() {
        switch (this) {
            case VERY_STRONG: return 1.5;
            case STRONG: return 1.2;
            case MODERATE: return 1.0;
            case WEAK: return 0.5;
            case NONE: return 0.0;
            default: return 0.0;
        }
    }
    
    /**
     * Get signal strength from confidence and quality scores
     */
    public static CompositeSignalStrength fromScores(double confidence, double quality) {
        double combinedScore = (confidence + quality) / 2.0;
        
        if (combinedScore >= 90.0) return VERY_STRONG;
        if (combinedScore >= 80.0) return STRONG;
        if (combinedScore >= 65.0) return MODERATE;
        if (combinedScore >= 50.0) return WEAK;
        return NONE;
    }
}