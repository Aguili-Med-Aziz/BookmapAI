package com.bookmaai.core.enhanced;

/**
 * 🎯 Absorption Direction Classification
 * 
 * Enumeration defining the direction of absorption patterns
 * indicating which side of the order book is absorbing volume
 */
public enum AbsorptionDirection {
    BID_ABSORPTION("Bid Absorption"),
    ASK_ABSORPTION("Ask Absorption"),
    BALANCED_ABSORPTION("Balanced Absorption");
    
    private final String displayName;
    
    AbsorptionDirection(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() { return displayName; }
    
    /**
     * Check if this represents bullish absorption (bid side)
     */
    public boolean isBullish() {
        return this == BID_ABSORPTION;
    }
    
    /**
     * Check if this represents bearish absorption (ask side)
     */
    public boolean isBearish() {
        return this == ASK_ABSORPTION;
    }
    
    /**
     * Check if absorption is balanced between both sides
     */
    public boolean isBalanced() {
        return this == BALANCED_ABSORPTION;
    }
    
    /**
     * Get absorption direction from volume ratio
     * @param bidRatio Ratio of bid volume (0.0 to 1.0)
     */
    public static AbsorptionDirection fromVolumeRatio(double bidRatio) {
        if (bidRatio >= 0.6) return BID_ABSORPTION;
        if (bidRatio <= 0.4) return ASK_ABSORPTION;
        return BALANCED_ABSORPTION;
    }
}