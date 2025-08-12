package com.bookmaai.core.enhanced;

/**
 * 🎯 Comprehensive Market Imbalance Result
 * 
 * Main result class for Market Imbalance Analysis containing all
 * sub-analysis results and comprehensive imbalance assessment
 */
public class MarketImbalanceResult {
    private final String symbol;
    private final double price;
    private final long timestamp;
    
    private OrderBookImbalance orderBookImbalance;
    private VolumeImbalanceAnalysis volumeImbalanceAnalysis;
    private PriceLevelImbalance priceLevelImbalance;
    private CumulativeImbalanceAnalysis cumulativeImbalanceAnalysis;
    private MultiTimeframeImbalance multiTimeframeImbalance;
    private ImbalanceStatistics imbalanceStatistics;
    
    private double overallConfidence;
    private boolean imbalanceDetected;
    private ImbalanceStrength imbalanceStrength;
    private ImbalanceDirection imbalanceDirection;
    
    public MarketImbalanceResult(String symbol, double price, long timestamp) {
        this.symbol = symbol;
        this.price = price;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public long getTimestamp() { return timestamp; }
    
    public OrderBookImbalance getOrderBookImbalance() { return orderBookImbalance; }
    public void setOrderBookImbalance(OrderBookImbalance orderBookImbalance) { 
        this.orderBookImbalance = orderBookImbalance; }
    
    public VolumeImbalanceAnalysis getVolumeImbalanceAnalysis() { return volumeImbalanceAnalysis; }
    public void setVolumeImbalanceAnalysis(VolumeImbalanceAnalysis volumeImbalanceAnalysis) { 
        this.volumeImbalanceAnalysis = volumeImbalanceAnalysis; }
    
    public PriceLevelImbalance getPriceLevelImbalance() { return priceLevelImbalance; }
    public void setPriceLevelImbalance(PriceLevelImbalance priceLevelImbalance) { 
        this.priceLevelImbalance = priceLevelImbalance; }
    
    public CumulativeImbalanceAnalysis getCumulativeImbalanceAnalysis() { return cumulativeImbalanceAnalysis; }
    public void setCumulativeImbalanceAnalysis(CumulativeImbalanceAnalysis cumulativeImbalanceAnalysis) { 
        this.cumulativeImbalanceAnalysis = cumulativeImbalanceAnalysis; }
    
    public MultiTimeframeImbalance getMultiTimeframeImbalance() { return multiTimeframeImbalance; }
    public void setMultiTimeframeImbalance(MultiTimeframeImbalance multiTimeframeImbalance) { 
        this.multiTimeframeImbalance = multiTimeframeImbalance; }
    
    public ImbalanceStatistics getImbalanceStatistics() { return imbalanceStatistics; }
    public void setImbalanceStatistics(ImbalanceStatistics imbalanceStatistics) { 
        this.imbalanceStatistics = imbalanceStatistics; }
    
    public double getOverallConfidence() { return overallConfidence; }
    public void setOverallConfidence(double overallConfidence) { this.overallConfidence = overallConfidence; }
    
    public boolean isImbalanceDetected() { return imbalanceDetected; }
    public void setImbalanceDetected(boolean imbalanceDetected) { this.imbalanceDetected = imbalanceDetected; }
    
    public ImbalanceStrength getImbalanceStrength() { return imbalanceStrength; }
    public void setImbalanceStrength(ImbalanceStrength imbalanceStrength) { this.imbalanceStrength = imbalanceStrength; }
    
    public ImbalanceDirection getImbalanceDirection() { return imbalanceDirection; }
    public void setImbalanceDirection(ImbalanceDirection imbalanceDirection) { this.imbalanceDirection = imbalanceDirection; }
    
    /**
     * Check if this represents a significant market imbalance
     */
    public boolean isSignificantImbalance() {
        return imbalanceDetected && overallConfidence >= 80.0 && 
               imbalanceStrength != null && imbalanceStrength.getLevel() >= 2;
    }
    
    /**
     * Get the net imbalance score (positive = buy pressure, negative = sell pressure)
     */
    public double getNetImbalanceScore() {
        if (!imbalanceDetected) return 0.0;
        
        double score = overallConfidence / 100.0;
        if (imbalanceDirection == ImbalanceDirection.SELL_PRESSURE) {
            score = -score;
        }
        return score;
    }
    
    /**
     * Get imbalance persistence in minutes
     */
    public double getImbalancePersistenceMinutes() {
        if (multiTimeframeImbalance != null) {
            return multiTimeframeImbalance.getAverageDurationMinutes();
        }
        return 0.0;
    }
    
    @Override
    public String toString() {
        return String.format("MarketImbalanceResult{symbol='%s', price=%.4f, detected=%s, confidence=%.1f%%, direction=%s}", 
                           symbol, price, imbalanceDetected, overallConfidence, 
                           imbalanceDirection != null ? imbalanceDirection.getDisplayName() : "N/A");
    }
}

// Supporting classes that don't need to be public
class OrderBookImbalance {
    private double[] bidVolumes;
    private double[] askVolumes;
    private double weightedImbalance;
    private int activeLevels;
    
    public OrderBookImbalance(double[] bidVolumes, double[] askVolumes, double weightedImbalance, int activeLevels) {
        this.bidVolumes = bidVolumes;
        this.askVolumes = askVolumes;
        this.weightedImbalance = weightedImbalance;
        this.activeLevels = activeLevels;
    }
    
    public double[] getBidVolumes() { return bidVolumes; }
    public double[] getAskVolumes() { return askVolumes; }
    public double getWeightedImbalance() { return weightedImbalance; }
    public int getActiveLevels() { return activeLevels; }
}

class VolumeImbalanceAnalysis {
    private double totalBuyVolume;
    private double totalSellVolume;
    private double volumeRatio;
    private double netVolume;
    
    public VolumeImbalanceAnalysis(double totalBuyVolume, double totalSellVolume, double volumeRatio, double netVolume) {
        this.totalBuyVolume = totalBuyVolume;
        this.totalSellVolume = totalSellVolume;
        this.volumeRatio = volumeRatio;
        this.netVolume = netVolume;
    }
    
    public double getTotalBuyVolume() { return totalBuyVolume; }
    public double getTotalSellVolume() { return totalSellVolume; }
    public double getVolumeRatio() { return volumeRatio; }
    public double getNetVolume() { return netVolume; }
}

class PriceLevelImbalance {
    private java.util.Map<Double, Double> levelImbalances;
    private double maxImbalanceLevel;
    private double maxImbalanceValue;
    
    public PriceLevelImbalance(java.util.Map<Double, Double> levelImbalances, double maxImbalanceLevel, double maxImbalanceValue) {
        this.levelImbalances = levelImbalances;
        this.maxImbalanceLevel = maxImbalanceLevel;
        this.maxImbalanceValue = maxImbalanceValue;
    }
    
    public java.util.Map<Double, Double> getLevelImbalances() { return levelImbalances; }
    public double getMaxImbalanceLevel() { return maxImbalanceLevel; }
    public double getMaxImbalanceValue() { return maxImbalanceValue; }
}

class CumulativeImbalanceAnalysis {
    private double cumulativeImbalance;
    private double[] imbalanceHistory;
    private ImbalanceTrend trend;
    private double trendStrength;
    
    public CumulativeImbalanceAnalysis(double cumulativeImbalance, double[] imbalanceHistory, ImbalanceTrend trend, double trendStrength) {
        this.cumulativeImbalance = cumulativeImbalance;
        this.imbalanceHistory = imbalanceHistory;
        this.trend = trend;
        this.trendStrength = trendStrength;
    }
    
    public double getCumulativeImbalance() { return cumulativeImbalance; }
    public double[] getImbalanceHistory() { return imbalanceHistory; }
    public ImbalanceTrend getTrend() { return trend; }
    public double getTrendStrength() { return trendStrength; }
}

class MultiTimeframeImbalance {
    private java.util.Map<String, Double> timeframeImbalances;
    private TimeframeAlignment alignment;
    private double averageDurationMinutes;
    
    public MultiTimeframeImbalance(java.util.Map<String, Double> timeframeImbalances, TimeframeAlignment alignment, double averageDurationMinutes) {
        this.timeframeImbalances = timeframeImbalances;
        this.alignment = alignment;
        this.averageDurationMinutes = averageDurationMinutes;
    }
    
    public java.util.Map<String, Double> getTimeframeImbalances() { return timeframeImbalances; }
    public TimeframeAlignment getAlignment() { return alignment; }
    public double getAverageDurationMinutes() { return averageDurationMinutes; }
}

class ImbalanceStatistics {
    private double pValue;
    private double zScore;
    private double confidenceInterval;
    private boolean statisticallySignificant;
    
    public ImbalanceStatistics(double pValue, double zScore, double confidenceInterval, boolean statisticallySignificant) {
        this.pValue = pValue;
        this.zScore = zScore;
        this.confidenceInterval = confidenceInterval;
        this.statisticallySignificant = statisticallySignificant;
    }
    
    public double getPValue() { return pValue; }
    public double getZScore() { return zScore; }
    public double getConfidenceInterval() { return confidenceInterval; }
    public boolean isStatisticallySignificant() { return statisticallySignificant; }
}