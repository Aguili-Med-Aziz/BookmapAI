package com.bookmaai.core.enhanced;

/**
 * 🎯 Signal Conflict Types
 * 
 * Enumeration defining types of conflicts between signals
 */
public enum ConflictType { 
    DIRECTION_CONFLICT,  // Signals disagree on direction
    STRENGTH_CONFLICT,   // Signals disagree on strength
    TIMING_CONFLICT;     // Signals disagree on timing
    
    /**
     * Get severity of this conflict type
     */
    public int getSeverity() {
        switch (this) {
            case DIRECTION_CONFLICT: return 3; // High severity
            case STRENGTH_CONFLICT: return 2;  // Medium severity
            case TIMING_CONFLICT: return 1;    // Low severity
            default: return 1;
        }
    }
}