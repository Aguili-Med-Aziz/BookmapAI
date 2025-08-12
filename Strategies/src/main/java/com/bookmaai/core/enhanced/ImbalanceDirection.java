package com.bookmaai.core.enhanced;

/**
 * 🎯 Imbalance Direction Classification
 * 
 * Enumeration defining the direction of market imbalance
 * indicating buy-side or sell-side pressure
 */
public enum ImbalanceDirection {
    BUY_PRESSURE("Buy Side Pressure"),
    SELL_PRESSURE("Sell Side Pressure"),
    BALANCED("Balanced Pressure");
    
    private final String displayName;
    
    ImbalanceDirection(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() { return displayName; }
    
    /**
     * Check if this represents bullish pressure
     */
    public boolean isBullish() {
        return this == BUY_PRESSURE;
    }
    
    /**
     * Check if this represents bearish pressure
     */
    public boolean isBearish() {
        return this == SELL_PRESSURE;
    }
    
    /**
     * Check if pressure is balanced
     */
    public boolean isBalanced() {
        return this == BALANCED;
    }
    
    /**
     * Get imbalance direction from volume ratio
     * @param buyRatio Ratio of buy volume (0.0 to 1.0)
     */
    public static ImbalanceDirection fromVolumeRatio(double buyRatio) {
        if (buyRatio >= 0.6) return BUY_PRESSURE;
        if (buyRatio <= 0.4) return SELL_PRESSURE;
        return BALANCED;
    }
}