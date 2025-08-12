package com.bookmaai.core.enhanced;

/**
 * 🎯 Signal Data Classes
 * 
 * Supporting data structures for individual signal types
 * used in composite signal processing
 */

// Iceberg Signal Data
class IcebergSignalData {
    private final String symbol;
    private final double hiddenSize;
    private final double visibleSize;
    private final double refillRate;
    private final double confidence;
    private final long timestamp;
    
    public IcebergSignalData(String symbol, double hiddenSize, double visibleSize, 
                            double refillRate, double confidence, long timestamp) {
        this.symbol = symbol;
        this.hiddenSize = hiddenSize;
        this.visibleSize = visibleSize;
        this.refillRate = refillRate;
        this.confidence = confidence;
        this.timestamp = timestamp;
    }
    
    public String getSymbol() { return symbol; }
    public double getHiddenSize() { return hiddenSize; }
    public double getVisibleSize() { return visibleSize; }
    public double getRefillRate() { return refillRate; }
    public double getConfidence() { return confidence; }
    public long getTimestamp() { return timestamp; }
    
    public double getHiddenRatio() {
        double total = hiddenSize + visibleSize;
        return total > 0 ? hiddenSize / total : 0.0;
    }
}

// Absorption Signal Data
class AbsorptionSignalData {
    private final String symbol;
    private final double absorptionRate;
    private final AbsorptionDirection direction;
    private final double volumeAbsorbed;
    private final double confidence;
    private final long timestamp;
    
    public AbsorptionSignalData(String symbol, double absorptionRate, AbsorptionDirection direction,
                               double volumeAbsorbed, double confidence, long timestamp) {
        this.symbol = symbol;
        this.absorptionRate = absorptionRate;
        this.direction = direction;
        this.volumeAbsorbed = volumeAbsorbed;
        this.confidence = confidence;
        this.timestamp = timestamp;
    }
    
    public String getSymbol() { return symbol; }
    public double getAbsorptionRate() { return absorptionRate; }
    public AbsorptionDirection getDirection() { return direction; }
    public double getVolumeAbsorbed() { return volumeAbsorbed; }
    public double getConfidence() { return confidence; }
    public long getTimestamp() { return timestamp; }
}

// Imbalance Signal Data
class ImbalanceSignalData {
    private final String symbol;
    private final double imbalanceRatio;
    private final ImbalanceDirection direction;
    private final double buyPressure;
    private final double sellPressure;
    private final double confidence;
    private final long timestamp;
    
    public ImbalanceSignalData(String symbol, double imbalanceRatio, ImbalanceDirection direction,
                              double buyPressure, double sellPressure, double confidence, long timestamp) {
        this.symbol = symbol;
        this.imbalanceRatio = imbalanceRatio;
        this.direction = direction;
        this.buyPressure = buyPressure;
        this.sellPressure = sellPressure;
        this.confidence = confidence;
        this.timestamp = timestamp;
    }
    
    public String getSymbol() { return symbol; }
    public double getImbalanceRatio() { return imbalanceRatio; }
    public ImbalanceDirection getDirection() { return direction; }
    public double getBuyPressure() { return buyPressure; }
    public double getSellPressure() { return sellPressure; }
    public double getConfidence() { return confidence; }
    public long getTimestamp() { return timestamp; }
}

// Order Execution Data
class OrderExecutionData {
    private final String symbol;
    private final double price;
    private final double volume;
    private final String side; // "BUY" or "SELL"
    private final long executionTime;
    private final boolean isIcebergExecution;
    
    public OrderExecutionData(String symbol, double price, double volume, String side, 
                             long executionTime, boolean isIcebergExecution) {
        this.symbol = symbol;
        this.price = price;
        this.volume = volume;
        this.side = side;
        this.executionTime = executionTime;
        this.isIcebergExecution = isIcebergExecution;
    }
    
    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public double getVolume() { return volume; }
    public String getSide() { return side; }
    public long getExecutionTime() { return executionTime; }
    public boolean isIcebergExecution() { return isIcebergExecution; }
    
    public boolean isBuyOrder() { return "BUY".equals(side); }
    public boolean isSellOrder() { return "SELL".equals(side); }
}