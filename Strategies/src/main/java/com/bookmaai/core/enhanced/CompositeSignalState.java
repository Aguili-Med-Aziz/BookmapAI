package com.bookmaai.core.enhanced;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 🎯 Composite Signal State Tracking
 * 
 * Maintains state for composite signal processing across time
 */
public class CompositeSignalState {
    private final String symbol;
    private final List<CompositeSignalSnapshot> snapshots = new CopyOnWriteArrayList<>();
    private final Map<String, Double> currentSignalStrengths = new HashMap<>();
    private final Map<String, SignalDirection> currentSignalDirections = new HashMap<>();
    private double compositeConfidence;
    private CompositeSignalStrength currentStrength;
    private CompositeSignalDirection currentDirection;
    private long lastUpdateTime;
    
    public CompositeSignalState(String symbol) {
        this.symbol = symbol;
        this.currentStrength = CompositeSignalStrength.NONE;
        this.currentDirection = CompositeSignalDirection.NEUTRAL;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    public void updateSignal(String signalName, double strength, SignalDirection direction, long timestamp) {
        currentSignalStrengths.put(signalName, strength);
        currentSignalDirections.put(signalName, direction);
        this.lastUpdateTime = timestamp;
        
        // Add snapshot
        snapshots.add(new CompositeSignalSnapshot(timestamp, strength, direction, signalName));
        
        // Keep only recent snapshots (last 100)
        if (snapshots.size() > 100) {
            snapshots.remove(0);
        }
    }
    
    // Getters
    public String getSymbol() { return symbol; }
    public List<CompositeSignalSnapshot> getSnapshots() { return new ArrayList<>(snapshots); }
    public Map<String, Double> getCurrentSignalStrengths() { return new HashMap<>(currentSignalStrengths); }
    public Map<String, SignalDirection> getCurrentSignalDirections() { return new HashMap<>(currentSignalDirections); }
    public double getCompositeConfidence() { return compositeConfidence; }
    public CompositeSignalStrength getCurrentStrength() { return currentStrength; }
    public CompositeSignalDirection getCurrentDirection() { return currentDirection; }
    public long getLastUpdateTime() { return lastUpdateTime; }
    
    public void setCompositeConfidence(double compositeConfidence) { this.compositeConfidence = compositeConfidence; }
    public void setCurrentStrength(CompositeSignalStrength currentStrength) { this.currentStrength = currentStrength; }
    public void setCurrentDirection(CompositeSignalDirection currentDirection) { this.currentDirection = currentDirection; }
    
    // Additional methods needed by implementation
    public void updateWithSignal(CompositeSignalResult result, long timestamp) {
        this.lastUpdateTime = timestamp;
        // Simple implementation for compilation
    }
}

class CompositeSignalSnapshot {
    private final long timestamp;
    private final double signalStrength;
    private final SignalDirection signalDirection;
    private final String signalName;
    
    public CompositeSignalSnapshot(long timestamp, double signalStrength, SignalDirection signalDirection, String signalName) {
        this.timestamp = timestamp;
        this.signalStrength = signalStrength;
        this.signalDirection = signalDirection;
        this.signalName = signalName;
    }
    
    public long getTimestamp() { return timestamp; }
    public double getSignalStrength() { return signalStrength; }
    public SignalDirection getSignalDirection() { return signalDirection; }
    public String getSignalName() { return signalName; }
}

class CompositeSignalEvent {
    private final String symbol;
    private final double price;
    private final CompositeSignalStrength strength;
    private final CompositeSignalDirection direction;
    private final double confidence;
    private final long timestamp;
    
    public CompositeSignalEvent(String symbol, double price, CompositeSignalStrength strength, 
                               CompositeSignalDirection direction, double confidence, long timestamp) {
        this.symbol = symbol;
        this.price = price;
        this.strength = strength;
        this.direction = direction;
        this.confidence = confidence;
        this.timestamp = timestamp;
    }
    
    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public CompositeSignalStrength getStrength() { return strength; }
    public CompositeSignalDirection getDirection() { return direction; }
    public double getConfidence() { return confidence; }
    public long getTimestamp() { return timestamp; }
}

class SignalPerformance {
    private double successRate;
    private double averageReturn;
    private double sharpeRatio;
    private int totalSignals;
    private int successfulSignals;
    
    public SignalPerformance() {
        this.successRate = 0.0;
        this.averageReturn = 0.0;
        this.sharpeRatio = 0.0;
        this.totalSignals = 0;
        this.successfulSignals = 0;
    }
    
    public void updatePerformance(boolean successful, double returnValue) {
        totalSignals++;
        if (successful) {
            successfulSignals++;
        }
        
        successRate = totalSignals > 0 ? (double) successfulSignals / totalSignals : 0.0;
        // Simplified performance calculation
        averageReturn = (averageReturn * (totalSignals - 1) + returnValue) / totalSignals;
    }
    
    public double getSuccessRate() { return successRate; }
    public double getAverageReturn() { return averageReturn; }
    public double getSharpeRatio() { return sharpeRatio; }
    public int getTotalSignals() { return totalSignals; }
    public int getSuccessfulSignals() { return successfulSignals; }
}

class CompositeSignal {
    private final String symbol;
    private final CompositeSignalStrength strength;
    private final CompositeSignalDirection direction;
    private final double confidence;
    private final SignalAction recommendedAction;
    private final long timestamp;
    
    public CompositeSignal(String symbol, CompositeSignalStrength strength, CompositeSignalDirection direction,
                          double confidence, SignalAction recommendedAction, long timestamp) {
        this.symbol = symbol;
        this.strength = strength;
        this.direction = direction;
        this.confidence = confidence;
        this.recommendedAction = recommendedAction;
        this.timestamp = timestamp;
    }
    
    public String getSymbol() { return symbol; }
    public CompositeSignalStrength getStrength() { return strength; }
    public CompositeSignalDirection getDirection() { return direction; }
    public double getConfidence() { return confidence; }
    public SignalAction getRecommendedAction() { return recommendedAction; }
    public long getTimestamp() { return timestamp; }
}

// Additional helper classes for signal processing
class SignalConflict {
    private final String signal1;
    private final String signal2;
    private final ConflictType conflictType;
    private final double severity;
    
    public SignalConflict(String signal1, String signal2, ConflictType conflictType, double severity) {
        this.signal1 = signal1;
        this.signal2 = signal2;
        this.conflictType = conflictType;
        this.severity = severity;
    }
    
    public String getSignal1() { return signal1; }
    public String getSignal2() { return signal2; }
    public ConflictType getConflictType() { return conflictType; }
    public double getSeverity() { return severity; }
}

class SignalResolution {
    private final SignalDirection resolvedDirection;
    private final double resolvedStrength;
    private final ConflictResolutionStrategy strategy;
    private final double confidence;
    
    public SignalResolution(SignalDirection resolvedDirection, double resolvedStrength, 
                           ConflictResolutionStrategy strategy, double confidence) {
        this.resolvedDirection = resolvedDirection;
        this.resolvedStrength = resolvedStrength;
        this.strategy = strategy;
        this.confidence = confidence;
    }
    
    public SignalDirection getResolvedDirection() { return resolvedDirection; }
    public double getResolvedStrength() { return resolvedStrength; }
    public ConflictResolutionStrategy getStrategy() { return strategy; }
    public double getConfidence() { return confidence; }
}

class DetectedPattern {
    private final PatternType patternType;
    private final double strength;
    private final double confidence;
    private final long detectionTime;
    
    public DetectedPattern(PatternType patternType, double strength, double confidence, long detectionTime) {
        this.patternType = patternType;
        this.strength = strength;
        this.confidence = confidence;
        this.detectionTime = detectionTime;
    }
    
    public PatternType getPatternType() { return patternType; }
    public double getStrength() { return strength; }
    public double getConfidence() { return confidence; }
    public long getDetectionTime() { return detectionTime; }
}