package com.bookmaai.core.enhanced;

/**
 * 🎯 Signal Types Classification
 * 
 * Enumeration defining the different types of signals
 * used in the composite signal system
 */
public enum SignalType {
    ICEBERG("Iceberg Detection"),
    ABSORPTION("Absorption Pattern"),
    IMBALANCE("Market Imbalance");
    
    private final String displayName;
    
    SignalType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() { return displayName; }
    
    /**
     * Get default weight for this signal type
     */
    public double getDefaultWeight() {
        switch (this) {
            case ICEBERG: return 0.35;     // High weight for iceberg detection
            case ABSORPTION: return 0.35;  // High weight for absorption patterns
            case IMBALANCE: return 0.30;   // Moderate weight for imbalance
            default: return 0.33;
        }
    }
    
    /**
     * Get typical confidence threshold for this signal type
     */
    public double getConfidenceThreshold() {
        switch (this) {
            case ICEBERG: return 85.0;     // High threshold for iceberg
            case ABSORPTION: return 80.0;  // Medium-high threshold for absorption
            case IMBALANCE: return 75.0;   // Medium threshold for imbalance
            default: return 80.0;
        }
    }
}