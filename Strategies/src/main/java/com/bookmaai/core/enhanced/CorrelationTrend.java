package com.bookmaai.core.enhanced;

/**
 * 🎯 Signal Correlation Trend
 * 
 * Enumeration defining trends in signal correlation over time
 */
public enum CorrelationTrend { 
    INCREASING, 
    DECREASING, 
    STABLE;
    
    /**
     * Get trend from correlation change rate
     */
    public static CorrelationTrend fromChangeRate(double changeRate) {
        if (changeRate > 0.1) return INCREASING;
        if (changeRate < -0.1) return DECREASING;
        return STABLE;
    }
}