package com.bookmaai.core.enhanced;

/**
 * 🎯 Imbalance Trend Classification
 * 
 * Enumeration defining the trend direction of market imbalance
 * over time periods
 */
public enum ImbalanceTrend {
    INCREASING("Increasing Imbalance"),
    DECREASING("Decreasing Imbalance"),
    NEUTRAL("Neutral Trend");
    
    private final String displayName;
    
    ImbalanceTrend(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() { return displayName; }
    
    /**
     * Check if trend is strengthening
     */
    public boolean isStrengthening() {
        return this == INCREASING;
    }
    
    /**
     * Check if trend is weakening
     */
    public boolean isWeakening() {
        return this == DECREASING;
    }
    
    /**
     * Check if trend is stable
     */
    public boolean isStable() {
        return this == NEUTRAL;
    }
    
    /**
     * Get trend from slope value
     * @param slope Trend slope value (positive = increasing, negative = decreasing)
     */
    public static ImbalanceTrend fromSlope(double slope) {
        if (slope > 0.1) return INCREASING;
        if (slope < -0.1) return DECREASING;
        return NEUTRAL;
    }
}