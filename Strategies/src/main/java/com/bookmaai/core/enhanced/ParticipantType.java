package com.bookmaai.core.enhanced;

/**
 * 🎯 Market Participant Type Classification
 * 
 * Enumeration defining different types of market participants
 * based on their trading patterns and volume characteristics
 */
public enum ParticipantType {
    MARKET_MAKER("Market Maker"),
    INSTITUTIONAL("Institutional"),
    LARGE_RETAIL("Large Retail"),
    SMALL_RETAIL("Small Retail");
    
    private final String displayName;
    
    ParticipantType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() { return displayName; }
    
    /**
     * Check if this participant type is institutional
     */
    public boolean isInstitutional() {
        return this == MARKET_MAKER || this == INSTITUTIONAL;
    }
    
    /**
     * Check if this participant type is retail
     */
    public boolean isRetail() {
        return this == LARGE_RETAIL || this == SMALL_RETAIL;
    }
    
    /**
     * Get the typical volume threshold for this participant type
     */
    public double getTypicalVolumeThreshold() {
        switch (this) {
            case MARKET_MAKER: return 10000.0;
            case INSTITUTIONAL: return 5000.0;
            case LARGE_RETAIL: return 1000.0;
            case SMALL_RETAIL: return 100.0;
            default: return 100.0;
        }
    }
    
    /**
     * Determine participant type from volume and behavior patterns
     * @param volume Trading volume
     * @param consistencyScore Behavioral consistency (0.0 to 1.0)
     */
    public static ParticipantType fromVolumeAndBehavior(double volume, double consistencyScore) {
        if (volume >= 10000.0 && consistencyScore >= 0.8) {
            return MARKET_MAKER;
        } else if (volume >= 5000.0 && consistencyScore >= 0.6) {
            return INSTITUTIONAL;
        } else if (volume >= 1000.0) {
            return LARGE_RETAIL;
        } else {
            return SMALL_RETAIL;
        }
    }
}