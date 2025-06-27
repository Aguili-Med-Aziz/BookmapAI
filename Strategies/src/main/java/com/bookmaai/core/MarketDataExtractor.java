package com.bookmaai.core;

import java.util.*;
import java.util.concurrent.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Market Data Extractor - Enhanced data extraction system
 * 
 * Comprehensive data extraction system for Bookmap integration:
 * - Real-time tick data extraction
 * - Order book depth analysis
 * - Volume profile generation
 * - Market session detection
 * - Multi-timeframe data aggregation
 */
public class MarketDataExtractor {
    
    private final Map<String, MarketSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, TickDataBuffer> tickBuffers = new ConcurrentHashMap<>();
    private final Map<String, OrderBookSnapshot> orderBooks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    
    // Market session definitions
    private static final Map<String, MarketHours> MARKET_SESSIONS = new HashMap<>();
    static {
        MARKET_SESSIONS.put("FOREX", new MarketHours("00:00", "23:59", "UTC"));
        MARKET_SESSIONS.put("US_STOCKS", new MarketHours("09:30", "16:00", "EST"));
        MARKET_SESSIONS.put("FUTURES_ES", new MarketHours("18:00", "17:00", "EST"));
        MARKET_SESSIONS.put("CRYPTO", new MarketHours("00:00", "23:59", "UTC"));
        MARKET_SESSIONS.put("LONDON", new MarketHours("08:00", "16:30", "GMT"));
        MARKET_SESSIONS.put("TOKYO", new MarketHours("09:00", "15:00", "JST"));
    }
    
    private volatile boolean isRunning = false;
    
    public void initialize() {
        System.out.println("[MarketDataExtractor] Initializing Market Data Extraction System...");
        
        // Start market session monitoring
        startMarketSessionMonitoring();
        
        // Start data cleanup scheduler
        startDataCleanupScheduler();
        
        isRunning = true;
        System.out.println("[MarketDataExtractor] Market Data Extractor ready for real-time processing");
    }
    
    /**
     * Extract tick data from Bookmap feed
     */
    public TickData extractTickData(String symbol, double price, double volume, 
                                   long timestamp, Map<String, Double> bookData) {
        
        // Create tick data object
        TickData tick = new TickData(symbol, price, volume, timestamp);
        
        // Extract order book data
        if (bookData != null) {
            tick.setBidPrice(bookData.getOrDefault("BidPrice", 0.0));
            tick.setAskPrice(bookData.getOrDefault("AskPrice", 0.0));
            tick.setBidSize(bookData.getOrDefault("BidSize", 0.0));
            tick.setAskSize(bookData.getOrDefault("AskSize", 0.0));
            tick.setSpread(tick.getAskPrice() - tick.getBidPrice());
        }
        
        // Add to buffer
        TickDataBuffer buffer = tickBuffers.computeIfAbsent(symbol, k -> new TickDataBuffer());
        buffer.addTick(tick);
        
        // Update order book snapshot
        updateOrderBookSnapshot(symbol, tick, bookData);
        
        // Detect market session
        detectMarketSession(symbol, timestamp);
        
        return tick;
    }
    
    /**
     * Extract order book depth data
     */
    public OrderBookDepth extractOrderBookDepth(String symbol, Map<String, Double> depthData) {
        OrderBookDepth depth = new OrderBookDepth(symbol, System.currentTimeMillis());
        
        // Extract bid levels
        for (int i = 1; i <= 10; i++) {
            double bidPrice = depthData.getOrDefault("Bid" + i + "Price", 0.0);
            double bidSize = depthData.getOrDefault("Bid" + i + "Size", 0.0);
            if (bidPrice > 0) {
                depth.addBidLevel(new PriceLevel(bidPrice, bidSize, i));
            }
        }
        
        // Extract ask levels
        for (int i = 1; i <= 10; i++) {
            double askPrice = depthData.getOrDefault("Ask" + i + "Price", 0.0);
            double askSize = depthData.getOrDefault("Ask" + i + "Size", 0.0);
            if (askPrice > 0) {
                depth.addAskLevel(new PriceLevel(askPrice, askSize, i));
            }
        }
        
        // Calculate depth metrics
        depth.calculateMetrics();
        
        return depth;
    }
    
    /**
     * Auto-detect open markets based on trading activity
     */
    public Set<String> detectOpenMarkets() {
        Set<String> openMarkets = new HashSet<>();
        LocalTime currentTime = LocalTime.now();
        
        for (Map.Entry<String, MarketHours> entry : MARKET_SESSIONS.entrySet()) {
            String market = entry.getKey();
            MarketHours hours = entry.getValue();
            
            if (isMarketOpen(hours, currentTime)) {
                openMarkets.add(market);
            }
        }
        
        // Also check for active trading sessions
        for (Map.Entry<String, MarketSession> entry : activeSessions.entrySet()) {
            if (entry.getValue().isActive()) {
                openMarkets.add(entry.getKey());
            }
        }
        
        return openMarkets;
    }
    
    /**
     * Get market data for specific symbol and timeframe
     */
    public MarketDataSnapshot getMarketDataSnapshot(String symbol, String timeframe) {
        TickDataBuffer buffer = tickBuffers.get(symbol);
        if (buffer == null) {
            return null;
        }
        
        MarketDataSnapshot snapshot = new MarketDataSnapshot(symbol, timeframe);
        
        // Get ticks for timeframe
        List<TickData> ticks = buffer.getTicksForTimeframe(timeframe);
        
        if (!ticks.isEmpty()) {
            // Calculate OHLC
            snapshot.setOpen(ticks.get(0).getPrice());
            snapshot.setClose(ticks.get(ticks.size() - 1).getPrice());
            snapshot.setHigh(ticks.stream().mapToDouble(TickData::getPrice).max().orElse(0.0));
            snapshot.setLow(ticks.stream().mapToDouble(TickData::getPrice).min().orElse(0.0));
            
            // Calculate volume
            snapshot.setVolume(ticks.stream().mapToDouble(TickData::getVolume).sum());
            
            // Calculate VWAP
            double totalVolumePrice = ticks.stream()
                .mapToDouble(t -> t.getPrice() * t.getVolume())
                .sum();
            snapshot.setVwap(totalVolumePrice / snapshot.getVolume());
            
            // Calculate additional metrics
            snapshot.setTickCount(ticks.size());
            snapshot.setSpreadAvg(ticks.stream().mapToDouble(TickData::getSpread).average().orElse(0.0));
        }
        
        return snapshot;
    }
    
    /**
     * Get all active symbols with recent data
     */
    public Set<String> getActiveSymbols() {
        Set<String> activeSymbols = new HashSet<>();
        long cutoffTime = System.currentTimeMillis() - (5 * 60 * 1000); // 5 minutes
        
        for (Map.Entry<String, TickDataBuffer> entry : tickBuffers.entrySet()) {
            if (entry.getValue().getLastTickTime() > cutoffTime) {
                activeSymbols.add(entry.getKey());
            }
        }
        
        return activeSymbols;
    }
    
    private void updateOrderBookSnapshot(String symbol, TickData tick, Map<String, Double> bookData) {
        if (bookData == null) return;
        
        OrderBookSnapshot snapshot = orderBooks.computeIfAbsent(symbol, 
            k -> new OrderBookSnapshot(symbol));
        
        snapshot.update(tick.getBidPrice(), tick.getAskPrice(), 
                       tick.getBidSize(), tick.getAskSize(), tick.getTimestamp());
    }
    
    private void detectMarketSession(String symbol, long timestamp) {
        // Simple session detection based on trading activity
        MarketSession session = activeSessions.computeIfAbsent(symbol, 
            k -> new MarketSession(symbol));
        
        session.updateActivity(timestamp);
    }
    
    private boolean isMarketOpen(MarketHours hours, LocalTime currentTime) {
        LocalTime openTime = LocalTime.parse(hours.getOpenTime());
        LocalTime closeTime = LocalTime.parse(hours.getCloseTime());
        
        // Handle overnight sessions (e.g., futures)
        if (closeTime.isBefore(openTime)) {
            return currentTime.isAfter(openTime) || currentTime.isBefore(closeTime);
        } else {
            return currentTime.isAfter(openTime) && currentTime.isBefore(closeTime);
        }
    }
    
    private void startMarketSessionMonitoring() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                // Update market sessions every minute
                Set<String> openMarkets = detectOpenMarkets();
                if (!openMarkets.isEmpty()) {
                    System.out.println("[MarketDataExtractor] Open Markets: " + openMarkets);
                }
            } catch (Exception e) {
                System.err.println("Error in market session monitoring: " + e.getMessage());
            }
        }, 0, 60, TimeUnit.SECONDS);
    }
    
    private void startDataCleanupScheduler() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                // Clean old data every hour
                long cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000); // 24 hours
                
                for (TickDataBuffer buffer : tickBuffers.values()) {
                    buffer.cleanup(cutoffTime);
                }
                
            } catch (Exception e) {
                System.err.println("Error in data cleanup: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.HOURS);
    }
    
    public void shutdown() {
        System.out.println("[MarketDataExtractor] Shutting down...");
        isRunning = false;
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        
        System.out.println("[MarketDataExtractor] Market Data Extractor shutdown completed");
    }
    
    // Supporting Classes
    
    public static class TickData {
        private final String symbol;
        private final double price;
        private final double volume;
        private final long timestamp;
        private double bidPrice, askPrice, bidSize, askSize, spread;
        
        public TickData(String symbol, double price, double volume, long timestamp) {
            this.symbol = symbol;
            this.price = price;
            this.volume = volume;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public double getVolume() { return volume; }
        public long getTimestamp() { return timestamp; }
        public double getBidPrice() { return bidPrice; }
        public void setBidPrice(double bidPrice) { this.bidPrice = bidPrice; }
        public double getAskPrice() { return askPrice; }
        public void setAskPrice(double askPrice) { this.askPrice = askPrice; }
        public double getBidSize() { return bidSize; }
        public void setBidSize(double bidSize) { this.bidSize = bidSize; }
        public double getAskSize() { return askSize; }
        public void setAskSize(double askSize) { this.askSize = askSize; }
        public double getSpread() { return spread; }
        public void setSpread(double spread) { this.spread = spread; }
    }
    
    private static class TickDataBuffer {
        private final List<TickData> ticks = new ArrayList<>();
        private static final int MAX_TICKS = 10000;
        
        synchronized void addTick(TickData tick) {
            ticks.add(tick);
            if (ticks.size() > MAX_TICKS) {
                ticks.remove(0);
            }
        }
        
        synchronized List<TickData> getTicksForTimeframe(String timeframe) {
            long timeframeMs = parseTimeframe(timeframe);
            long cutoffTime = System.currentTimeMillis() - timeframeMs;
            
            return ticks.stream()
                .filter(tick -> tick.getTimestamp() > cutoffTime)
                .collect(ArrayList::new, (list, tick) -> list.add(tick), ArrayList::addAll);
        }
        
        synchronized long getLastTickTime() {
            return ticks.isEmpty() ? 0 : ticks.get(ticks.size() - 1).getTimestamp();
        }
        
        synchronized void cleanup(long cutoffTime) {
            ticks.removeIf(tick -> tick.getTimestamp() < cutoffTime);
        }
        
        private long parseTimeframe(String timeframe) {
            switch (timeframe.toUpperCase()) {
                case "1M": return 60 * 1000;
                case "5M": return 5 * 60 * 1000;
                case "15M": return 15 * 60 * 1000;
                case "1H": return 60 * 60 * 1000;
                case "4H": return 4 * 60 * 60 * 1000;
                case "1D": return 24 * 60 * 60 * 1000;
                default: return 60 * 1000; // Default 1 minute
            }
        }
    }
    
    public static class OrderBookDepth {
        private final String symbol;
        private final long timestamp;
        private final List<PriceLevel> bidLevels = new ArrayList<>();
        private final List<PriceLevel> askLevels = new ArrayList<>();
        private double totalBidSize, totalAskSize, weightedBidPrice, weightedAskPrice;
        
        public OrderBookDepth(String symbol, long timestamp) {
            this.symbol = symbol;
            this.timestamp = timestamp;
        }
        
        public void addBidLevel(PriceLevel level) { bidLevels.add(level); }
        public void addAskLevel(PriceLevel level) { askLevels.add(level); }
        
        public void calculateMetrics() {
            totalBidSize = bidLevels.stream().mapToDouble(PriceLevel::getSize).sum();
            totalAskSize = askLevels.stream().mapToDouble(PriceLevel::getSize).sum();
            
            if (totalBidSize > 0) {
                weightedBidPrice = bidLevels.stream()
                    .mapToDouble(l -> l.getPrice() * l.getSize())
                    .sum() / totalBidSize;
            }
            
            if (totalAskSize > 0) {
                weightedAskPrice = askLevels.stream()
                    .mapToDouble(l -> l.getPrice() * l.getSize())
                    .sum() / totalAskSize;
            }
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public long getTimestamp() { return timestamp; }
        public List<PriceLevel> getBidLevels() { return bidLevels; }
        public List<PriceLevel> getAskLevels() { return askLevels; }
        public double getTotalBidSize() { return totalBidSize; }
        public double getTotalAskSize() { return totalAskSize; }
        public double getWeightedBidPrice() { return weightedBidPrice; }
        public double getWeightedAskPrice() { return weightedAskPrice; }
    }
    
    public static class PriceLevel {
        private final double price;
        private final double size;
        private final int level;
        
        public PriceLevel(double price, double size, int level) {
            this.price = price;
            this.size = size;
            this.level = level;
        }
        
        public double getPrice() { return price; }
        public double getSize() { return size; }
        public int getLevel() { return level; }
    }
    
    public static class MarketDataSnapshot {
        private final String symbol;
        private final String timeframe;
        private double open, high, low, close, volume, vwap;
        private int tickCount;
        private double spreadAvg;
        
        public MarketDataSnapshot(String symbol, String timeframe) {
            this.symbol = symbol;
            this.timeframe = timeframe;
        }
        
        // Getters and setters
        public String getSymbol() { return symbol; }
        public String getTimeframe() { return timeframe; }
        public double getOpen() { return open; }
        public void setOpen(double open) { this.open = open; }
        public double getHigh() { return high; }
        public void setHigh(double high) { this.high = high; }
        public double getLow() { return low; }
        public void setLow(double low) { this.low = low; }
        public double getClose() { return close; }
        public void setClose(double close) { this.close = close; }
        public double getVolume() { return volume; }
        public void setVolume(double volume) { this.volume = volume; }
        public double getVwap() { return vwap; }
        public void setVwap(double vwap) { this.vwap = vwap; }
        public int getTickCount() { return tickCount; }
        public void setTickCount(int tickCount) { this.tickCount = tickCount; }
        public double getSpreadAvg() { return spreadAvg; }
        public void setSpreadAvg(double spreadAvg) { this.spreadAvg = spreadAvg; }
    }
    
    private static class MarketHours {
        private final String openTime;
        private final String closeTime;
        private final String timezone;
        
        public MarketHours(String openTime, String closeTime, String timezone) {
            this.openTime = openTime;
            this.closeTime = closeTime;
            this.timezone = timezone;
        }
        
        public String getOpenTime() { return openTime; }
        public String getCloseTime() { return closeTime; }
        public String getTimezone() { return timezone; }
    }
    
    private static class MarketSession {
        private final String symbol;
        private long lastActivity;
        private boolean active;
        
        public MarketSession(String symbol) {
            this.symbol = symbol;
            this.lastActivity = System.currentTimeMillis();
            this.active = true;
        }
        
        public void updateActivity(long timestamp) {
            this.lastActivity = timestamp;
            this.active = (System.currentTimeMillis() - lastActivity) < (5 * 60 * 1000); // 5 minutes
        }
        
        public boolean isActive() { return active; }
    }
    
    private static class OrderBookSnapshot {
        private final String symbol;
        private double bidPrice, askPrice, bidSize, askSize;
        private long lastUpdate;
        
        public OrderBookSnapshot(String symbol) {
            this.symbol = symbol;
        }
        
        public void update(double bidPrice, double askPrice, double bidSize, double askSize, long timestamp) {
            this.bidPrice = bidPrice;
            this.askPrice = askPrice;
            this.bidSize = bidSize;
            this.askSize = askSize;
            this.lastUpdate = timestamp;
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public double getBidPrice() { return bidPrice; }
        public double getAskPrice() { return askPrice; }
        public double getBidSize() { return bidSize; }
        public double getAskSize() { return askSize; }
        public long getLastUpdate() { return lastUpdate; }
    }
} 