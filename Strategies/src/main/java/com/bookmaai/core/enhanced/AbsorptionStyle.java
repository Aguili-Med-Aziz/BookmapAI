package com.bookmaai.core.enhanced;

/**
 * 🎯 Absorption Style Classification
 * 
 * Enumeration defining different styles of absorption behavior
 * based on timing patterns and execution characteristics
 */
public enum AbsorptionStyle {
    AGGRESSIVE_ABSORPTION("Aggressive Absorption"),
    METHODICAL_ABSORPTION("Methodical Absorption"),
    GRADUAL_ABSORPTION("Gradual Absorption"),
    PASSIVE_ABSORPTION("Passive Absorption");
    
    private final String displayName;
    
    AbsorptionStyle(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() { return displayName; }
    
    /**
     * Check if this style indicates high-frequency activity
     */
    public boolean isHighFrequency() {
        return this == AGGRESSIVE_ABSORPTION;
    }
    
    /**
     * Check if this style indicates systematic behavior
     */
    public boolean isSystematic() {
        return this == METHODICAL_ABSORPTION || this == GRADUAL_ABSORPTION;
    }
    
    /**
     * Check if this style indicates opportunistic behavior
     */
    public boolean isOpportunistic() {
        return this == PASSIVE_ABSORPTION;
    }
    
    /**
     * Get the typical time horizon for this absorption style
     * @return Duration in minutes
     */
    public double getTypicalDurationMinutes() {
        switch (this) {
            case AGGRESSIVE_ABSORPTION: return 2.0;
            case METHODICAL_ABSORPTION: return 15.0;
            case GRADUAL_ABSORPTION: return 60.0;
            case PASSIVE_ABSORPTION: return 5.0;
            default: return 10.0;
        }
    }
    
    /**
     * Determine absorption style from timing and volume characteristics
     * @param averageIntervalSeconds Average time between absorptions
     * @param volumeConsistency Volume consistency score (0.0 to 1.0)
     */
    public static AbsorptionStyle fromTimingAndConsistency(double averageIntervalSeconds, double volumeConsistency) {
        if (averageIntervalSeconds < 30.0 && volumeConsistency < 0.4) {
            return AGGRESSIVE_ABSORPTION;
        } else if (averageIntervalSeconds < 60.0 && volumeConsistency >= 0.7) {
            return METHODICAL_ABSORPTION;
        } else if (averageIntervalSeconds >= 120.0) {
            return GRADUAL_ABSORPTION;
        } else {
            return PASSIVE_ABSORPTION;
        }
    }
}