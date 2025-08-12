package com.bookmaai.core.enhanced;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 🎯 Absorption State Tracking
 * 
 * Maintains state for absorption pattern detection across time
 */
public class AbsorptionState {
    private final String symbol;
    private final List<AbsorptionSnapshot> snapshots = new CopyOnWriteArrayList<>();
    private double currentAbsorptionRate;
    private double totalAbsorbedVolume;
    private long lastUpdateTime;
    private AbsorptionDirection currentDirection;
    private ParticipantType dominantParticipant;
    
    public AbsorptionState(String symbol) {
        this.symbol = symbol;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    public void updateAbsorption(double bidVolume, double askVolume, long timestamp) {
        this.currentAbsorptionRate = calculateAbsorptionRate(bidVolume, askVolume);
        this.totalAbsorbedVolume += Math.max(bidVolume, askVolume);
        this.lastUpdateTime = timestamp;
        
        // Add snapshot
        snapshots.add(new AbsorptionSnapshot(timestamp, currentAbsorptionRate, bidVolume, askVolume));
        
        // Keep only recent snapshots (last 100)
        if (snapshots.size() > 100) {
            snapshots.remove(0);
        }
    }
    
    public void updateWithAbsorption(double bidVolume, double askVolume, double confidence, long timestamp) {
        updateAbsorption(bidVolume, askVolume, timestamp);
    }
    
    public double getAverageAbsorption() {
        if (snapshots.isEmpty()) return 0.0;
        return snapshots.stream().mapToDouble(AbsorptionSnapshot::getAbsorptionRate).average().orElse(0.0);
    }
    
    public List<AbsorptionSnapshot> getAbsorptionHistory() {
        return new ArrayList<>(snapshots);
    }
    
    public int getRecentActivityCount() {
        long recentTime = System.currentTimeMillis() - 300000; // 5 minutes ago
        return (int) snapshots.stream().filter(s -> s.getTimestamp() > recentTime).count();
    }
    
    public List<Double> getRecentAbsorptions() {
        return snapshots.stream().map(AbsorptionSnapshot::getAbsorptionRate).collect(java.util.stream.Collectors.toList());
    }
    
    public double getAbsorptionPersistence() {
        if (snapshots.size() < 2) return 0.0;
        // Simple persistence calculation
        long duration = snapshots.get(snapshots.size() - 1).getTimestamp() - snapshots.get(0).getTimestamp();
        return Math.min(1.0, duration / 300000.0); // Normalize to 5 minutes
    }
    
    private double calculateAbsorptionRate(double bidVolume, double askVolume) {
        double total = bidVolume + askVolume;
        if (total == 0) return 0.0;
        return Math.abs(bidVolume - askVolume) / total;
    }
    
    // Getters
    public String getSymbol() { return symbol; }
    public List<AbsorptionSnapshot> getSnapshots() { return new ArrayList<>(snapshots); }
    public double getCurrentAbsorptionRate() { return currentAbsorptionRate; }
    public double getTotalAbsorbedVolume() { return totalAbsorbedVolume; }
    public long getLastUpdateTime() { return lastUpdateTime; }
    public AbsorptionDirection getCurrentDirection() { return currentDirection; }
    public ParticipantType getDominantParticipant() { return dominantParticipant; }
    
    public void setCurrentDirection(AbsorptionDirection currentDirection) { this.currentDirection = currentDirection; }
    public void setDominantParticipant(ParticipantType dominantParticipant) { this.dominantParticipant = dominantParticipant; }
}

class AbsorptionSnapshot {
    private final long timestamp;
    private final double absorptionRate;
    private final double bidVolume;
    private final double askVolume;
    
    public AbsorptionSnapshot(long timestamp, double absorptionRate, double bidVolume, double askVolume) {
        this.timestamp = timestamp;
        this.absorptionRate = absorptionRate;
        this.bidVolume = bidVolume;
        this.askVolume = askVolume;
    }
    
    public long getTimestamp() { return timestamp; }
    public double getAbsorptionRate() { return absorptionRate; }
    public double getAbsorption() { return absorptionRate; }  // Alias for compatibility
    public double getBidVolume() { return bidVolume; }
    public double getAskVolume() { return askVolume; }
}

class AbsorptionEvent {
    private final String symbol;
    private final double price;
    private final double absorptionRatio;
    private final AbsorptionStrength strength;
    private final long timestamp;
    
    public AbsorptionEvent(String symbol, double price, double absorptionRatio, AbsorptionStrength strength, long timestamp) {
        this.symbol = symbol;
        this.price = price;
        this.absorptionRatio = absorptionRatio;
        this.strength = strength;
        this.timestamp = timestamp;
    }
    
    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public double getAbsorptionRatio() { return absorptionRatio; }
    public AbsorptionStrength getStrength() { return strength; }
    public long getTimestamp() { return timestamp; }
}

class LevelAbsorption {
    private final int level;
    private final double volume;
    private final double absorptionStrength;
    private final AbsorptionDirection direction;
    
    public LevelAbsorption(int level, double volume, double absorptionStrength, AbsorptionDirection direction) {
        this.level = level;
        this.volume = volume;
        this.absorptionStrength = absorptionStrength;
        this.direction = direction;
    }
    
    public int getLevel() { return level; }
    public double getVolume() { return volume; }
    public double getAbsorptionStrength() { return absorptionStrength; }
    public double getAbsorptionRatio() { return absorptionStrength; } // Alias for compatibility
    public AbsorptionDirection getDirection() { return direction; }
}

class LevelConcentration {
    private final Map<Integer, Double> concentrationMap;
    private final int maxConcentrationLevel;
    private final double maxConcentrationValue;
    
    public LevelConcentration(Map<Integer, Double> concentrationMap, int maxConcentrationLevel, double maxConcentrationValue) {
        this.concentrationMap = concentrationMap;
        this.maxConcentrationLevel = maxConcentrationLevel;
        this.maxConcentrationValue = maxConcentrationValue;
    }
    
    public Map<Integer, Double> getConcentrationMap() { return concentrationMap; }
    public int getMaxConcentrationLevel() { return maxConcentrationLevel; }
    public double getMaxConcentrationValue() { return maxConcentrationValue; }
}