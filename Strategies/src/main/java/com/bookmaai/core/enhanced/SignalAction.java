package com.bookmaai.core.enhanced;

/**
 * 🎯 Signal Action Recommendations
 * 
 * Enumeration defining recommended trading actions
 * based on composite signal analysis
 */
public enum SignalAction {
    STRONG_BUY("Strong Buy"),
    BUY("Buy"),
    HOLD("Hold"),
    SELL("Sell"),
    STRONG_SELL("Strong Sell");
    
    private final String displayName;
    
    SignalAction(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() { return displayName; }
    
    /**
     * Check if action is a buy recommendation
     */
    public boolean isBuyAction() {
        return this == STRONG_BUY || this == BUY;
    }
    
    /**
     * Check if action is a sell recommendation
     */
    public boolean isSellAction() {
        return this == STRONG_SELL || this == SELL;
    }
    
    /**
     * Check if action is neutral
     */
    public boolean isNeutralAction() {
        return this == HOLD;
    }
    
    /**
     * Check if action is high conviction
     */
    public boolean isHighConviction() {
        return this == STRONG_BUY || this == STRONG_SELL;
    }
    
    /**
     * Get action intensity level (0-4)
     */
    public int getIntensityLevel() {
        switch (this) {
            case STRONG_SELL: return 0;
            case SELL: return 1;
            case HOLD: return 2;
            case BUY: return 3;
            case STRONG_BUY: return 4;
            default: return 2;
        }
    }
    
    /**
     * Get recommended action from signal direction and strength
     */
    public static SignalAction fromDirectionAndStrength(CompositeSignalDirection direction, CompositeSignalStrength strength) {
        if (direction == CompositeSignalDirection.NEUTRAL || strength == CompositeSignalStrength.NONE) {
            return HOLD;
        }
        
        if (direction == CompositeSignalDirection.BULLISH) {
            if (strength == CompositeSignalStrength.VERY_STRONG) return STRONG_BUY;
            if (strength == CompositeSignalStrength.STRONG) return BUY;
            if (strength == CompositeSignalStrength.MODERATE) return BUY;
            return HOLD;
        } else { // BEARISH
            if (strength == CompositeSignalStrength.VERY_STRONG) return STRONG_SELL;
            if (strength == CompositeSignalStrength.STRONG) return SELL;
            if (strength == CompositeSignalStrength.MODERATE) return SELL;
            return HOLD;
        }
    }
}