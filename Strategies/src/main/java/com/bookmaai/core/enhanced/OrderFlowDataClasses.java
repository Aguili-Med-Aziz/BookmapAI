package com.bookmaai.core.enhanced;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDateTime;

/**
 * Data Classes for Advanced Order Flow Engine
 * Contains all data structures for Bookmap-style analysis
 */
public class OrderFlowDataClasses {
    
    // ==================== MAIN ANALYSIS RESULT ====================
    
    public static class OrderFlowAnalysis {
        private final String symbol;
        private final double price;
        private final double volume;
        private final String side;
        private final long timestamp;
        
        private StopIcebergResult stopIcebergResult;
        private AbsorptionResult absorptionResult;
        private LargeTradeResult largeTradeResult;
        private MarketPulseResult marketPulseResult;
        private LiquidityAnalysis liquidityAnalysis;
        
        private double overallConfidence;
        private boolean hasSignificantPattern;
        
        public OrderFlowAnalysis(String symbol, double price, double volume, String side, long timestamp) {
            this.symbol = symbol;
            this.price = price;
            this.volume = volume;
            this.side = side;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public double getVolume() { return volume; }
        public String getSide() { return side; }
        public long getTimestamp() { return timestamp; }
        
        public StopIcebergResult getStopIcebergResult() { return stopIcebergResult; }
        public void setStopIcebergResult(StopIcebergResult result) { this.stopIcebergResult = result; }
        
        public AbsorptionResult getAbsorptionResult() { return absorptionResult; }
        public void setAbsorptionResult(AbsorptionResult result) { this.absorptionResult = result; }
        
        public LargeTradeResult getLargeTradeResult() { return largeTradeResult; }
        public void setLargeTradeResult(LargeTradeResult result) { this.largeTradeResult = result; }
        
        public MarketPulseResult getMarketPulseResult() { return marketPulseResult; }
        public void setMarketPulseResult(MarketPulseResult result) { this.marketPulseResult = result; }
        
        public LiquidityAnalysis getLiquidityAnalysis() { return liquidityAnalysis; }
        public void setLiquidityAnalysis(LiquidityAnalysis analysis) { this.liquidityAnalysis = analysis; }
        
        public double getOverallConfidence() { return overallConfidence; }
        public void setOverallConfidence(double confidence) { this.overallConfidence = confidence; }
        
        public boolean hasSignificantPattern() {
            return (stopIcebergResult != null && (stopIcebergResult.isStopDetected() || stopIcebergResult.isIcebergDetected())) ||
                   (absorptionResult != null && absorptionResult.isSignificant()) ||
                   (largeTradeResult != null && largeTradeResult.isLargeTrade()) ||
                   overallConfidence > 80.0;
        }
    }
    
    // ==================== STOPS & ICEBERGS ====================
    
    public static class StopIcebergResult {
        private final String symbol;
        private final double price;
        private final double volume;
        private final String side;
        
        private boolean stopDetected = false;
        private boolean icebergDetected = false;
        private double stopConfidence = 0.0;
        private double icebergConfidence = 0.0;
        private String stopType = "";
        private double estimatedTotalSize = 0.0;
        
        public StopIcebergResult(String symbol, double price, double volume, String side) {
            this.symbol = symbol;
            this.price = price;
            this.volume = volume;
            this.side = side;
        }
        
        // Getters and setters
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public double getVolume() { return volume; }
        public String getSide() { return side; }
        
        public boolean isStopDetected() { return stopDetected; }
        public void setStopDetected(boolean detected) { this.stopDetected = detected; }
        
        public boolean isIcebergDetected() { return icebergDetected; }
        public void setIcebergDetected(boolean detected) { this.icebergDetected = detected; }
        
        public double getStopConfidence() { return stopConfidence; }
        public void setStopConfidence(double confidence) { this.stopConfidence = confidence; }
        
        public double getIcebergConfidence() { return icebergConfidence; }
        public void setIcebergConfidence(double confidence) { this.icebergConfidence = confidence; }
        
        public String getStopType() { return stopType; }
        public void setStopType(String type) { this.stopType = type; }
        
        public double getEstimatedTotalSize() { return estimatedTotalSize; }
        public void setEstimatedTotalSize(double size) { this.estimatedTotalSize = size; }
    }
    
    public static class StopOrderData {
        private final String symbol;
        private final double price;
        private final double volume;
        private final String side;
        private final long timestamp;
        
        public StopOrderData(String symbol, double price, double volume, String side, long timestamp) {
            this.symbol = symbol;
            this.price = price;
            this.volume = volume;
            this.side = side;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public double getVolume() { return volume; }
        public String getSide() { return side; }
        public long getTimestamp() { return timestamp; }
    }
    
    public static class IcebergOrderData {
        private final String symbol;
        private final double price;
        private final double visibleSize;
        private final String side;
        private final long timestamp;
        
        public IcebergOrderData(String symbol, double price, double visibleSize, String side, long timestamp) {
            this.symbol = symbol;
            this.price = price;
            this.visibleSize = visibleSize;
            this.side = side;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public double getVisibleSize() { return visibleSize; }
        public String getSide() { return side; }
        public long getTimestamp() { return timestamp; }
    }
    
    // ==================== ABSORPTION INDICATOR ====================
    
    public static class AbsorptionResult {
        private final String symbol;
        private final double price;
        
        private double absorptionRatio;
        private AbsorptionType absorptionType;
        private double confidence;
        private boolean significant;
        
        public AbsorptionResult(String symbol, double price) {
            this.symbol = symbol;
            this.price = price;
        }
        
        // Getters and setters
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public double getAbsorptionRatio() { return absorptionRatio; }
        public void setAbsorptionRatio(double ratio) { this.absorptionRatio = ratio; }
        public AbsorptionType getAbsorptionType() { return absorptionType; }
        public void setAbsorptionType(AbsorptionType type) { this.absorptionType = type; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public boolean isSignificant() { return significant; }
        public void setSignificant(boolean significant) { this.significant = significant; }
    }
    
    public enum AbsorptionType {
        STRONG_ABSORPTION, MODERATE_ABSORPTION, WEAK_ABSORPTION, NO_ABSORPTION
    }
    
    public static class AbsorptionEvent {
        private final String symbol;
        private final double price;
        private final double absorptionRatio;
        private final AbsorptionType type;
        private final double confidence;
        private final long timestamp;
        
        public AbsorptionEvent(String symbol, double price, double absorptionRatio, 
                              AbsorptionType type, double confidence, long timestamp) {
            this.symbol = symbol;
            this.price = price;
            this.absorptionRatio = absorptionRatio;
            this.type = type;
            this.confidence = confidence;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public double getAbsorptionRatio() { return absorptionRatio; }
        public AbsorptionType getType() { return type; }
        public double getConfidence() { return confidence; }
        public long getTimestamp() { return timestamp; }
    }
    
    // ==================== LARGE TRADES ====================
    
    public static class LargeTradeResult {
        private final String symbol;
        private final double price;
        private final double volume;
        private final String side;
        
        private boolean largeTrade = false;
        private LargeTradeType tradeType;
        private double volumeRatio;
        private double significance;
        private double marketImpact;
        
        public LargeTradeResult(String symbol, double price, double volume, String side) {
            this.symbol = symbol;
            this.price = price;
            this.volume = volume;
            this.side = side;
        }
        
        // Getters and setters
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public double getVolume() { return volume; }
        public String getSide() { return side; }
        
        public boolean isLargeTrade() { return largeTrade; }
        public void setLargeTrade(boolean largeTrade) { this.largeTrade = largeTrade; }
        
        public LargeTradeType getTradeType() { return tradeType; }
        public void setTradeType(LargeTradeType type) { this.tradeType = type; }
        
        public double getVolumeRatio() { return volumeRatio; }
        public void setVolumeRatio(double ratio) { this.volumeRatio = ratio; }
        
        public double getSignificance() { return significance; }
        public void setSignificance(double significance) { this.significance = significance; }
        
        public double getMarketImpact() { return marketImpact; }
        public void setMarketImpact(double impact) { this.marketImpact = impact; }
    }
    
    public enum LargeTradeType {
        INSTITUTIONAL_BLOCK, LARGE_INSTITUTIONAL, MEDIUM_INSTITUTIONAL, LARGE_RETAIL
    }
    
    public static class LargeTrade {
        private final String symbol;
        private final double price;
        private final double volume;
        private final String side;
        private final LargeTradeType type;
        private final double significance;
        private final long timestamp;
        
        public LargeTrade(String symbol, double price, double volume, String side, 
                         LargeTradeType type, double significance, long timestamp) {
            this.symbol = symbol;
            this.price = price;
            this.volume = volume;
            this.side = side;
            this.type = type;
            this.significance = significance;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public double getVolume() { return volume; }
        public String getSide() { return side; }
        public LargeTradeType getType() { return type; }
        public double getSignificance() { return significance; }
        public long getTimestamp() { return timestamp; }
    }
    
    // ==================== LIQUIDITY ANALYSIS ====================
    
    public static class LiquidityAnalysis {
        private final String symbol;
        private final double price;
        
        private double totalBidLiquidity;
        private double totalAskLiquidity;
        private double liquidityImbalance;
        private List<LiquidityZone> liquidityZones;
        private double liquidityStress;
        private String liquidityRating;
        
        public LiquidityAnalysis(String symbol, double price) {
            this.symbol = symbol;
            this.price = price;
            this.liquidityZones = new ArrayList<>();
        }
        
        // Getters and setters
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        
        public double getTotalBidLiquidity() { return totalBidLiquidity; }
        public void setTotalBidLiquidity(double liquidity) { this.totalBidLiquidity = liquidity; }
        
        public double getTotalAskLiquidity() { return totalAskLiquidity; }
        public void setTotalAskLiquidity(double liquidity) { this.totalAskLiquidity = liquidity; }
        
        public double getLiquidityImbalance() { return liquidityImbalance; }
        public void setLiquidityImbalance(double imbalance) { this.liquidityImbalance = imbalance; }
        
        public List<LiquidityZone> getLiquidityZones() { return liquidityZones; }
        public void setLiquidityZones(List<LiquidityZone> zones) { this.liquidityZones = zones; }
        
        public double getLiquidityStress() { return liquidityStress; }
        public void setLiquidityStress(double stress) { this.liquidityStress = stress; }
        
        public String getLiquidityRating() { return liquidityRating; }
        public void setLiquidityRating(String rating) { this.liquidityRating = rating; }
    }
    
    public static class LiquidityZone {
        private final double price;
        private final double volume;
        private final String side;
        private final double strength;
        
        public LiquidityZone(double price, double volume, String side, double strength) {
            this.price = price;
            this.volume = volume;
            this.side = side;
            this.strength = strength;
        }
        
        // Getters
        public double getPrice() { return price; }
        public double getVolume() { return volume; }
        public String getSide() { return side; }
        public double getStrength() { return strength; }
    }
    
    public static class LiquidityProfile {
        private final String symbol;
        private final AtomicLong updateCount = new AtomicLong(0);
        private volatile double averageLiquidity = 0.0;
        private volatile double averageImbalance = 0.0;
        private volatile long lastUpdate = 0;
        
        public LiquidityProfile(String symbol) {
            this.symbol = symbol;
        }
        
        public void update(LiquidityAnalysis analysis) {
            long count = updateCount.incrementAndGet();
            double totalLiquidity = analysis.getTotalBidLiquidity() + analysis.getTotalAskLiquidity();
            
            // Exponential moving average
            if (count == 1) {
                averageLiquidity = totalLiquidity;
                averageImbalance = analysis.getLiquidityImbalance();
            } else {
                double alpha = 0.1; // Smoothing factor
                averageLiquidity = (1 - alpha) * averageLiquidity + alpha * totalLiquidity;
                averageImbalance = (1 - alpha) * averageImbalance + alpha * analysis.getLiquidityImbalance();
            }
            
            lastUpdate = System.currentTimeMillis();
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public double getAverageLiquidity() { return averageLiquidity; }
        public double getAverageImbalance() { return averageImbalance; }
        public long getUpdateCount() { return updateCount.get(); }
        public long getLastUpdate() { return lastUpdate; }
    }
    
    // ==================== MARKET PULSE ====================
    
    public static class MarketPulseResult {
        private final String symbol;
        private final long timestamp;
        
        private double intensity;
        private double momentum;
        private double pressure;
        private String sentiment;
        private double volatility;
        private double trendStrength;
        
        public MarketPulseResult(String symbol, long timestamp) {
            this.symbol = symbol;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getSymbol() { return symbol; }
        public long getTimestamp() { return timestamp; }
        
        public double getIntensity() { return intensity; }
        public void setIntensity(double intensity) { this.intensity = intensity; }
        
        public double getMomentum() { return momentum; }
        public void setMomentum(double momentum) { this.momentum = momentum; }
        
        public double getPressure() { return pressure; }
        public void setPressure(double pressure) { this.pressure = pressure; }
        
        public String getSentiment() { return sentiment; }
        public void setSentiment(String sentiment) { this.sentiment = sentiment; }
        
        public double getVolatility() { return volatility; }
        public void setVolatility(double volatility) { this.volatility = volatility; }
        
        public double getTrendStrength() { return trendStrength; }
        public void setTrendStrength(double strength) { this.trendStrength = strength; }
    }
    
    public static class MarketPulse {
        private final String symbol;
        private final List<PriceTick> recentTicks = new CopyOnWriteArrayList<>();
        private volatile double recentVolume = 0.0;
        private volatile double averageVolume = 1000.0;
        private volatile double buyPressure = 0.0;
        private volatile double sellPressure = 0.0;
        
        public MarketPulse(String symbol) {
            this.symbol = symbol;
        }
        
        public void addTick(double price, double volume, long timestamp) {
            PriceTick tick = new PriceTick(price, volume, timestamp);
            recentTicks.add(tick);
            
            // Keep only last 100 ticks
            if (recentTicks.size() > 100) {
                recentTicks.remove(0);
            }
            
            // Update metrics
            updateMetrics();
        }
        
        private void updateMetrics() {
            if (recentTicks.isEmpty()) return;
            
            // Calculate recent volume
            recentVolume = recentTicks.stream()
                .filter(tick -> tick.timestamp > System.currentTimeMillis() - 60000) // Last minute
                .mapToDouble(tick -> tick.volume)
                .sum();
            
            // Update average volume with exponential moving average
            averageVolume = (averageVolume * 0.95) + (recentVolume * 0.05);
            
            // Calculate buy/sell pressure (simplified)
            buyPressure = recentTicks.stream()
                .filter(tick -> isUpTick(tick))
                .mapToDouble(tick -> tick.volume)
                .sum();
            
            sellPressure = recentTicks.stream()
                .filter(tick -> !isUpTick(tick))
                .mapToDouble(tick -> tick.volume)
                .sum();
        }
        
        private boolean isUpTick(PriceTick tick) {
            int index = recentTicks.indexOf(tick);
            if (index == 0) return true;
            return tick.price > recentTicks.get(index - 1).price;
        }
        
        public double getPriceMomentum() {
            if (recentTicks.size() < 2) return 0.0;
            double firstPrice = recentTicks.get(0).price;
            double lastPrice = recentTicks.get(recentTicks.size() - 1).price;
            return (lastPrice - firstPrice) / firstPrice;
        }
        
        public double getVolatility() {
            if (recentTicks.size() < 2) return 0.0;
            
            double sum = 0.0;
            for (int i = 1; i < recentTicks.size(); i++) {
                double change = (recentTicks.get(i).price - recentTicks.get(i-1).price) / recentTicks.get(i-1).price;
                sum += Math.pow(change, 2);
            }
            
            return Math.sqrt(sum / (recentTicks.size() - 1));
        }
        
        public double getTrendStrength() {
            if (recentTicks.size() < 3) return 0.0;
            
            int upMoves = 0;
            int downMoves = 0;
            
            for (int i = 1; i < recentTicks.size(); i++) {
                if (recentTicks.get(i).price > recentTicks.get(i-1).price) {
                    upMoves++;
                } else if (recentTicks.get(i).price < recentTicks.get(i-1).price) {
                    downMoves++;
                }
            }
            
            int totalMoves = upMoves + downMoves;
            if (totalMoves == 0) return 0.0;
            
            return Math.abs(upMoves - downMoves) / (double) totalMoves;
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public double getRecentVolume() { return recentVolume; }
        public double getAverageVolume() { return averageVolume; }
        public double getBuyPressure() { return buyPressure; }
        public double getSellPressure() { return sellPressure; }
    }
    
    public static class PriceTick {
        public final double price;
        public final double volume;
        public final long timestamp;
        
        public PriceTick(double price, double volume, long timestamp) {
            this.price = price;
            this.volume = volume;
            this.timestamp = timestamp;
        }
    }
    
    // ==================== ALERTS ====================
    
    public static class TradingAlert {
        private final String type;
        private final String symbol;
        private final String message;
        private final double confidence;
        private final long timestamp;
        
        public TradingAlert(String type, String symbol, String message, double confidence) {
            this.type = type;
            this.symbol = symbol;
            this.message = message;
            this.confidence = confidence;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public String getType() { return type; }
        public String getSymbol() { return symbol; }
        public String getMessage() { return message; }
        public double getConfidence() { return confidence; }
        public long getTimestamp() { return timestamp; }
    }
    
    // ==================== MARKET MICROSTRUCTURE ====================
    
    public static class MarketMicrostructure {
        private final String symbol;
        private final Map<String, Object> metrics = new ConcurrentHashMap<>();
        private volatile long lastUpdate = 0;
        
        public MarketMicrostructure(String symbol) {
            this.symbol = symbol;
        }
        
        public void updateMetric(String key, Object value) {
            metrics.put(key, value);
            lastUpdate = System.currentTimeMillis();
        }
        
        public Object getMetric(String key) {
            return metrics.get(key);
        }
        
        public Map<String, Object> getAllMetrics() {
            return new HashMap<>(metrics);
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public long getLastUpdate() { return lastUpdate; }
    }
}