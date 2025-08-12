package com.bookmaai.core.enhanced;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 🎯 Market Imbalance State Tracking
 * 
 * Maintains state for market imbalance detection across time
 */
public class ImbalanceState {
    private final String symbol;
    private final List<ImbalanceSnapshot> snapshots = new CopyOnWriteArrayList<>();
    private double currentImbalanceRatio;
    private double cumulativeImbalance;
    private long lastUpdateTime;
    private ImbalanceDirection currentDirection;
    private ImbalanceStrength currentStrength;
    
    public ImbalanceState(String symbol) {
        this.symbol = symbol;
        this.lastUpdateTime = System.currentTimeMillis();
        this.currentStrength = ImbalanceStrength.NONE;
        this.currentDirection = ImbalanceDirection.BALANCED;
    }
    
    public void updateImbalance(double buyVolume, double sellVolume, double[] orderBookLevels, long timestamp) {
        double totalVolume = buyVolume + sellVolume;
        this.currentImbalanceRatio = totalVolume > 0 ? (buyVolume - sellVolume) / totalVolume : 0.0;
        this.cumulativeImbalance += currentImbalanceRatio;
        this.lastUpdateTime = timestamp;
        
        // Update direction based on imbalance
        if (currentImbalanceRatio > 0.1) {
            this.currentDirection = ImbalanceDirection.BUY_PRESSURE;
        } else if (currentImbalanceRatio < -0.1) {
            this.currentDirection = ImbalanceDirection.SELL_PRESSURE;
        } else {
            this.currentDirection = ImbalanceDirection.BALANCED;
        }
        
        // Add snapshot
        snapshots.add(new ImbalanceSnapshot(timestamp, currentImbalanceRatio, buyVolume, sellVolume, orderBookLevels));
        
        // Keep only recent snapshots (last 100)
        if (snapshots.size() > 100) {
            snapshots.remove(0);
        }
    }
    
    // Getters
    public String getSymbol() { return symbol; }
    public List<ImbalanceSnapshot> getSnapshots() { return new ArrayList<>(snapshots); }
    public double getCurrentImbalanceRatio() { return currentImbalanceRatio; }
    public double getCumulativeImbalance() { return cumulativeImbalance; }
    public long getLastUpdateTime() { return lastUpdateTime; }
    public ImbalanceDirection getCurrentDirection() { return currentDirection; }
    public ImbalanceStrength getCurrentStrength() { return currentStrength; }
    
    public void setCurrentStrength(ImbalanceStrength currentStrength) { this.currentStrength = currentStrength; }
}

class ImbalanceSnapshot {
    private final long timestamp;
    private final double imbalanceRatio;
    private final double buyVolume;
    private final double sellVolume;
    private final double[] orderBookLevels;
    
    public ImbalanceSnapshot(long timestamp, double imbalanceRatio, double buyVolume, double sellVolume, double[] orderBookLevels) {
        this.timestamp = timestamp;
        this.imbalanceRatio = imbalanceRatio;
        this.buyVolume = buyVolume;
        this.sellVolume = sellVolume;
        this.orderBookLevels = orderBookLevels != null ? orderBookLevels.clone() : new double[0];
    }
    
    public long getTimestamp() { return timestamp; }
    public double getImbalanceRatio() { return imbalanceRatio; }
    public double getBuyVolume() { return buyVolume; }
    public double getSellVolume() { return sellVolume; }
    public double[] getOrderBookLevels() { return orderBookLevels.clone(); }
}

class ImbalanceEvent {
    private final String symbol;
    private final double price;
    private final double imbalanceRatio;
    private final ImbalanceStrength strength;
    private final ImbalanceDirection direction;
    private final long timestamp;
    
    public ImbalanceEvent(String symbol, double price, double imbalanceRatio, 
                         ImbalanceStrength strength, ImbalanceDirection direction, long timestamp) {
        this.symbol = symbol;
        this.price = price;
        this.imbalanceRatio = imbalanceRatio;
        this.strength = strength;
        this.direction = direction;
        this.timestamp = timestamp;
    }
    
    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public double getImbalanceRatio() { return imbalanceRatio; }
    public ImbalanceStrength getStrength() { return strength; }
    public ImbalanceDirection getDirection() { return direction; }
    public long getTimestamp() { return timestamp; }
}

class VolumeFlowAnalysis {
    private final double flowIntensity;
    private final double flowDirection;
    private final double flowPersistence;
    private final double flowAcceleration;
    
    public VolumeFlowAnalysis(double flowIntensity, double flowDirection, double flowPersistence, double flowAcceleration) {
        this.flowIntensity = flowIntensity;
        this.flowDirection = flowDirection;
        this.flowPersistence = flowPersistence;
        this.flowAcceleration = flowAcceleration;
    }
    
    public double getFlowIntensity() { return flowIntensity; }
    public double getFlowDirection() { return flowDirection; }
    public double getFlowPersistence() { return flowPersistence; }
    public double getFlowAcceleration() { return flowAcceleration; }
}

class SupportResistanceAnalysis {
    private final Map<Double, Double> supportLevels;
    private final Map<Double, Double> resistanceLevels;
    private final double strongestSupport;
    private final double strongestResistance;
    
    public SupportResistanceAnalysis(Map<Double, Double> supportLevels, Map<Double, Double> resistanceLevels,
                                   double strongestSupport, double strongestResistance) {
        this.supportLevels = supportLevels;
        this.resistanceLevels = resistanceLevels;
        this.strongestSupport = strongestSupport;
        this.strongestResistance = strongestResistance;
    }
    
    public Map<Double, Double> getSupportLevels() { return supportLevels; }
    public Map<Double, Double> getResistanceLevels() { return resistanceLevels; }
    public double getStrongestSupport() { return strongestSupport; }
    public double getStrongestResistance() { return strongestResistance; }
}

class ConfidenceInterval {
    private final double lowerBound;
    private final double upperBound;
    private final double confidenceLevel;
    
    public ConfidenceInterval(double lowerBound, double upperBound, double confidenceLevel) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.confidenceLevel = confidenceLevel;
    }
    
    public double getLowerBound() { return lowerBound; }
    public double getUpperBound() { return upperBound; }
    public double getConfidenceLevel() { return confidenceLevel; }
}