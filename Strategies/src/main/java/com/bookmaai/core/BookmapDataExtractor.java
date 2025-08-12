package com.bookmaai.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Instant;

/**
 * Enhanced Bookmap Data Extractor v3.0
 * Extracts real-time data from all open Bookmap windows for AI processing
 * NOW CONNECTED TO DASHBOARD via RealTimeMarketDataStore
 */
public class BookmapDataExtractor {
    
    private static final String VERSION = "3.0-AI-Enhanced-Dashboard-Connected";
    
    // Data structures
    private final Map<String, String> activeInstruments = new ConcurrentHashMap<>();
    private final Map<String, BookmapDataStream> dataStreams = new ConcurrentHashMap<>();
    
    // AI-optimized data structures
    private final Map<String, AIDataBuffer> aiBuffers = new ConcurrentHashMap<>();
    private final ExecutorService aiProcessingPool = Executors.newFixedThreadPool(8);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    
    // Performance metrics
    private final AtomicLong totalTicksExtracted = new AtomicLong(0);
    private final AtomicLong totalOrderBookUpdates = new AtomicLong(0);
    private final AtomicLong avgExtractionLatency = new AtomicLong(0);
    
    // Data listeners
    private final Map<String, List<AIDataListener>> dataListeners = new ConcurrentHashMap<>();
    
    // Dashboard integration
    private BookmapAddonIntegration addonIntegration;
    private volatile boolean dashboardStarted = false;
    
    // REAL-TIME DATA STORE CONNECTION FOR DASHBOARD
    private final RealTimeMarketDataStore realTimeDataStore;
    
    public BookmapDataExtractor() {
        // Connect to the real-time data store for dashboard integration
        this.realTimeDataStore = RealTimeMarketDataStore.getInstance();
        initializeExtractor();
        startDashboardIntegration();
        System.out.println("📊 BookmapDataExtractor connected to dashboard data store");
    }
    
    // ==================== PUBLIC API ====================
    
    public void initialize(String alias, String symbol) {
        activeInstruments.put(symbol, symbol);
        
        // Initialize AI data buffer for this instrument
        AIDataBuffer buffer = new AIDataBuffer(symbol, 50000); // 50k data points
        aiBuffers.put(symbol, buffer);
        
        // Create data stream
        BookmapDataStream stream = new BookmapDataStream(symbol, symbol);
        dataStreams.put(symbol, stream);
        
        // Start AI processing for this instrument
        startAIProcessing(symbol);
        
        System.out.println("📊 BookmapAI Extractor connected to: " + symbol);
        System.out.println("🔗 Real-time data will flow to dashboard for: " + symbol);
    }
    
    public void onTrade(String alias, double price, int size) {
        long startTime = System.nanoTime();
        
        try {
            // Extract trade data
            EnhancedTradeData trade = new EnhancedTradeData(
                alias, price, size, System.currentTimeMillis()
            );
            
            // Store in AI buffer
            AIDataBuffer buffer = aiBuffers.get(alias);
            if (buffer != null) {
                buffer.addTrade(trade);
                totalTicksExtracted.incrementAndGet();
            }
            
            // ===== REAL BOOKMAP DATA TO DASHBOARD =====
            // Update real-time data store for dashboard display
            realTimeDataStore.updateMarketData(alias, price, size, "REAL_TRADE");
            
            // ===== FORWARD TO COMPREHENSIVE MANAGER =====
            // Process through all AI components with real data (INSTEAD OF FAKE DATA!)
            ComprehensiveBookmapAIManager.getInstance().processRealMarketData(alias, price, size, "REAL_TRADE");
            
            // Process for immediate AI analysis
            processTradeForAI(trade);
            
            // Notify listeners
            notifyDataListeners(alias, trade);
            
            // Forward to dashboard integration
            if (addonIntegration != null) {
                Map<String, Double> bookData = new HashMap<>();
                bookData.put("price", price);
                bookData.put("volume", (double) size);
                addonIntegration.onMarketData(alias, price, size, System.currentTimeMillis(), bookData);
            }
            
            // Update latency metrics
            long latency = (System.nanoTime() - startTime) / 1000; // microseconds
            updateLatencyMetrics(latency);
            
            // Log real data extraction every 50 trades to verify it's working
            if (totalTicksExtracted.get() % 50 == 0) {
                System.out.println("🚀 REAL BOOKMAP DATA: " + alias + " @ " + String.format("%.5f", price) + 
                                 ", Vol: " + size + " (Total: " + totalTicksExtracted.get() + " real trades)");
            }
            
        } catch (Exception e) {
            System.err.println("Trade processing error for " + alias + ": " + e.getMessage());
        }
    }
    
    public void onDepth(String alias, boolean isBid, int price, int size) {
        try {
            // Extract order book data
            EnhancedDepthData depth = new EnhancedDepthData(
                alias, isBid, price, size, System.currentTimeMillis()
            );
            
            // Store in AI buffer
            AIDataBuffer buffer = aiBuffers.get(alias);
            if (buffer != null) {
                buffer.addDepth(depth);
                totalOrderBookUpdates.incrementAndGet();
            }
            
            // ===== REAL BOOKMAP DEPTH DATA TO DASHBOARD =====
            // Update real-time data store with order book information  
            realTimeDataStore.updateMarketData(alias, price, size, "REAL_DEPTH");
            
            // ===== FORWARD TO COMPREHENSIVE MANAGER =====
            // Process through all AI components with real depth data (INSTEAD OF FAKE DATA!)
            ComprehensiveBookmapAIManager.getInstance().processRealMarketData(alias, price, size, "REAL_DEPTH");
            
            // Process for liquidity analysis
            processDepthForAI(depth);
            
            // Log real depth data extraction periodically
            if (totalOrderBookUpdates.get() % 100 == 0) {
                System.out.println("📚 REAL BOOKMAP DEPTH: " + alias + " " + (isBid ? "BID" : "ASK") + 
                                 " @ " + price + ", Size: " + size + " (Total: " + totalOrderBookUpdates.get() + " depth updates)");
            }
            
        } catch (Exception e) {
            System.err.println("Depth processing error for " + alias + ": " + e.getMessage());
        }
    }
    
    public void onInstrumentAdded(String alias, String symbol) {
        System.out.println("📈 REAL INSTRUMENT DETECTED: " + alias + " (" + symbol + ")");
        initialize(alias, symbol);
        
        // Update dashboard with new active instrument
        System.out.println("🔗 Dashboard will now show real data for: " + symbol);
    }
    
    public void onInstrumentRemoved(String alias) {
        System.out.println("📉 REAL INSTRUMENT REMOVED: " + alias);
        
        // Cleanup resources
        aiBuffers.remove(alias);
        dataStreams.remove(alias);
        activeInstruments.remove(alias);
        dataListeners.remove(alias);
    }
    
    // ==================== AI DATA PROCESSING ====================
    
    private void startAIProcessing(String symbol) {
        // Real-time AI feature extraction every 5 seconds
        scheduler.scheduleAtFixedRate(() -> extractAIFeatures(symbol), 0, 5, TimeUnit.SECONDS);
        
        // Pattern detection every 10 seconds
        scheduler.scheduleAtFixedRate(() -> detectPatternsAI(symbol), 0, 10, TimeUnit.SECONDS);
        
        // Market microstructure analysis every 30 seconds
        scheduler.scheduleAtFixedRate(() -> analyzeMicrostructure(symbol), 0, 30, TimeUnit.SECONDS);
    }
    
    private void processTradeForAI(EnhancedTradeData trade) {
        aiProcessingPool.submit(() -> {
            try {
                // Extract immediate features
                TradeFeatures features = extractTradeFeatures(trade);
                
                // Update real-time indicators
                updateRealTimeIndicators(trade.getSymbol(), features);
                
                // Check for immediate patterns
                checkImmediatePatterns(trade, features);
                
            } catch (Exception e) {
                System.err.println("AI trade processing error: " + e.getMessage());
            }
        });
    }
    
    private void processDepthForAI(EnhancedDepthData depth) {
        aiProcessingPool.submit(() -> {
            try {
                // Extract order book features
                OrderBookFeatures features = extractOrderBookFeatures(depth);
                
                // Analyze liquidity changes
                analyzeLiquidityChanges(depth.getSymbol(), features);
                
                // Detect order flow patterns
                detectOrderFlowPatterns(depth, features);
                
            } catch (Exception e) {
                System.err.println("AI depth processing error: " + e.getMessage());
            }
        });
    }
    
    private void extractAIFeatures(String symbol) {
        try {
            AIDataBuffer buffer = aiBuffers.get(symbol);
            if (buffer == null) return;
            
            // Get recent data for feature extraction
            List<EnhancedTradeData> recentTrades = buffer.getRecentTrades(1000);
            List<EnhancedDepthData> recentDepth = buffer.getRecentDepth(500);
            
            if (recentTrades.isEmpty()) return;
            
            // Generate AI feature vector
            AIFeatureVector features = generateAIFeatureVector(symbol, recentTrades, recentDepth);
            buffer.addFeatureVector(features);
            
            // Process with AI models
            processWithAIModels(symbol, features);
            
        } catch (Exception e) {
            System.err.println("AI feature extraction error for " + symbol + ": " + e.getMessage());
        }
    }
    
    private AIFeatureVector generateAIFeatureVector(String symbol, 
                                                   List<EnhancedTradeData> trades,
                                                   List<EnhancedDepthData> depths) {
        Map<String, Double> features = new HashMap<>();
        
        // Price-based features
        features.put("price_return_1m", calculatePriceReturn(trades, 60000));
        features.put("price_return_5m", calculatePriceReturn(trades, 300000));
        features.put("price_volatility", calculatePriceVolatility(trades, 300000));
        features.put("vwap", calculateVWAP(trades));
        
        // Volume-based features
        features.put("delta_flow", calculateDeltaFlow(trades));
        features.put("cumulative_delta", calculateCumulativeDelta(trades));
        
        // Order book features
        features.put("bid_ask_spread", calculateBidAskSpread(depths));
        
        // ICT pattern features
        features.put("fvg_strength", detectFairValueGapStrength(trades));
        features.put("order_block_quality", assessOrderBlockQuality(trades, depths));
        
        return new AIFeatureVector(symbol, features, Instant.now());
    }
    
    private void detectPatternsAI(String symbol) {
        try {
            AIDataBuffer buffer = aiBuffers.get(symbol);
            if (buffer == null) return;
            
            List<EnhancedTradeData> recentTrades = buffer.getRecentTrades(2000);
            List<EnhancedDepthData> recentDepth = buffer.getRecentDepth(1000);
            
            if (recentTrades.isEmpty()) return;
            
            // Detect ICT patterns
            List<AIPattern> patterns = detectICTPatterns(symbol, recentTrades, recentDepth);
            
            // Store patterns
            for (AIPattern pattern : patterns) {
                buffer.addPattern(pattern);
                triggerPatternAlert(pattern);
            }
            
        } catch (Exception e) {
            System.err.println("Pattern detection error for " + symbol + ": " + e.getMessage());
        }
    }
    
    private List<AIPattern> detectICTPatterns(String symbol, List<EnhancedTradeData> trades, List<EnhancedDepthData> depths) {
        List<AIPattern> patterns = new ArrayList<>();
        
        // Detect Fair Value Gaps
        AIPattern fvg = detectEnhancedFairValueGap(symbol, trades);
        if (fvg != null) patterns.add(fvg);
        
        // Detect Order Blocks
        AIPattern ob = detectEnhancedOrderBlock(symbol, trades, depths);
        if (ob != null) patterns.add(ob);
        
        return patterns;
    }
    
    // ==================== UTILITY METHODS ====================
    
    private double calculatePriceReturn(List<EnhancedTradeData> trades, long timeWindow) {
        if (trades.size() < 2) return 0.0;
        
        long cutoff = System.currentTimeMillis() - timeWindow;
        List<EnhancedTradeData> recent = trades.stream()
            .filter(t -> t.getTimestamp() >= cutoff)
            .collect(Collectors.toList());
        
        if (recent.size() < 2) return 0.0;
        
        double firstPrice = recent.get(0).getPrice();
        double lastPrice = recent.get(recent.size() - 1).getPrice();
        
        return (lastPrice - firstPrice) / firstPrice;
    }
    
    private double calculatePriceVolatility(List<EnhancedTradeData> trades, long timeWindow) {
        if (trades.size() < 2) return 0.0;
        
        long cutoff = System.currentTimeMillis() - timeWindow;
        List<EnhancedTradeData> recent = trades.stream()
            .filter(t -> t.getTimestamp() >= cutoff)
            .collect(Collectors.toList());
        
        if (recent.size() < 2) return 0.0;
        
        double[] returns = new double[recent.size() - 1];
        for (int i = 1; i < recent.size(); i++) {
            returns[i-1] = (recent.get(i).getPrice() - recent.get(i-1).getPrice()) / recent.get(i-1).getPrice();
        }
        
        double mean = Arrays.stream(returns).average().orElse(0.0);
        double variance = Arrays.stream(returns)
            .map(r -> Math.pow(r - mean, 2))
            .average().orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    private double calculateVWAP(List<EnhancedTradeData> trades) {
        if (trades.isEmpty()) return 0.0;
        
        double totalVolume = trades.stream().mapToDouble(t -> t.getSize()).sum();
        if (totalVolume == 0) return 0.0;
        
        return trades.stream()
            .mapToDouble(t -> t.getPrice() * t.getSize())
            .sum() / totalVolume;
    }
    
    private double calculateDeltaFlow(List<EnhancedTradeData> trades) {
        return trades.stream()
            .mapToDouble(t -> t.getSize())
            .sum();
    }
    
    private double calculateCumulativeDelta(List<EnhancedTradeData> trades) {
        return trades.stream()
            .mapToDouble(t -> t.getSize())
            .sum();
    }
    
    private double calculateBidAskSpread(List<EnhancedDepthData> depths) {
        if (depths.isEmpty()) return 0.0;
        
        double bestBid = depths.stream()
            .filter(d -> d.isBid())
            .mapToDouble(d -> d.getPrice())
            .max().orElse(0.0);
        
        double bestAsk = depths.stream()
            .filter(d -> !d.isBid())
            .mapToDouble(d -> d.getPrice())
            .min().orElse(Double.MAX_VALUE);
        
        return bestAsk - bestBid;
    }
    
    private double detectFairValueGapStrength(List<EnhancedTradeData> trades) {
        if (trades.size() < 3) return 0.0;
        
        // Simplified FVG detection
        double strength = 0.0;
        for (int i = 1; i < trades.size() - 1; i++) {
            double gap = trades.get(i+1).getPrice() - trades.get(i-1).getPrice();
            if (Math.abs(gap) > 0.0001) {
                strength += Math.abs(gap);
            }
        }
        
        return strength / trades.size();
    }
    
    private double assessOrderBlockQuality(List<EnhancedTradeData> trades, List<EnhancedDepthData> depths) {
        if (trades.isEmpty()) return 0.0;
        
        // Simplified order block quality assessment
        double avgVolume = trades.stream()
            .mapToDouble(t -> t.getSize())
            .average().orElse(0.0);
        
        double avgPrice = trades.stream()
            .mapToDouble(t -> t.getPrice())
            .average().orElse(0.0);
        
        return avgVolume * avgPrice / 1000000.0; // Normalized quality score
    }
    
    private AIPattern detectEnhancedFairValueGap(String symbol, List<EnhancedTradeData> trades) {
        double strength = detectFairValueGapStrength(trades);
        if (strength > 0.001) {
            return new AIPattern(symbol, "FAIR_VALUE_GAP", strength, 0.85, System.currentTimeMillis());
        }
        return null;
    }
    
    private AIPattern detectEnhancedOrderBlock(String symbol, List<EnhancedTradeData> trades, List<EnhancedDepthData> depths) {
        double quality = assessOrderBlockQuality(trades, depths);
        if (quality > 0.5) {
            return new AIPattern(symbol, "ORDER_BLOCK", quality, 0.82, System.currentTimeMillis());
        }
        return null;
    }
    
    private void initializeExtractor() {
        System.out.println("🚀 Initializing BookmapAI Data Extractor v" + VERSION);
        System.out.println("📊 AI processing pool: 8 threads");
        System.out.println("⏱️  Scheduled tasks: Feature extraction, Pattern detection, Microstructure analysis");
        System.out.println("🎯 Real-time data processing enabled");
    }
    
    private void startDashboardIntegration() {
        try {
            addonIntegration = new BookmapAddonIntegration();
            addonIntegration.initialize();
            dashboardStarted = true;
            System.out.println("🌐 Dashboard integration started");
        } catch (Exception e) {
            System.err.println("Dashboard integration error: " + e.getMessage());
        }
    }
    
    private void logPerformanceMetrics() {
        System.out.println("📈 Performance Metrics:");
        System.out.println("  Total ticks extracted: " + totalTicksExtracted.get());
        System.out.println("  Total order book updates: " + totalOrderBookUpdates.get());
        System.out.println("  Average latency: " + avgExtractionLatency.get() + " μs");
        System.out.println("  Active instruments: " + activeInstruments.size());
    }
    
    private void updateLatencyMetrics(long latency) {
        long current = avgExtractionLatency.get();
        long newAvg = (current + latency) / 2;
        avgExtractionLatency.set(newAvg);
    }
    
    private TradeFeatures extractTradeFeatures(EnhancedTradeData trade) {
        return new TradeFeatures(trade.getPrice(), trade.getSize(), trade.getTimestamp(), true);
    }
    
    private OrderBookFeatures extractOrderBookFeatures(EnhancedDepthData depth) {
        return new OrderBookFeatures(depth.getPrice(), depth.getSize(), depth.isBid(), depth.getTimestamp());
    }
    
    private void updateRealTimeIndicators(String symbol, TradeFeatures features) {
        // Update real-time indicators
    }
    
    private void checkImmediatePatterns(EnhancedTradeData trade, TradeFeatures features) {
        // Check for immediate patterns
    }
    
    private void analyzeLiquidityChanges(String symbol, OrderBookFeatures features) {
        // Analyze liquidity changes
    }
    
    private void detectOrderFlowPatterns(EnhancedDepthData depth, OrderBookFeatures features) {
        // Detect order flow patterns
    }
    
    private void analyzeMicrostructure(String symbol) {
        // Analyze market microstructure
    }
    
    private void processWithAIModels(String symbol, AIFeatureVector features) {
        // Process with AI models
    }
    
    private void triggerPatternAlert(AIPattern pattern) {
        System.out.println("🚨 Pattern Alert: " + pattern.getPatternType() + 
                          " on " + pattern.getSymbol() + 
                          " (confidence: " + String.format("%.2f", pattern.getConfidence()) + ")");
    }
    
    private void notifyDataListeners(String symbol, EnhancedTradeData trade) {
        List<AIDataListener> listeners = dataListeners.get(symbol);
        if (listeners != null) {
            for (AIDataListener listener : listeners) {
                try {
                    listener.onNewTrade(trade);
                } catch (Exception e) {
                    System.err.println("Listener notification error: " + e.getMessage());
                }
            }
        }
    }
    
    public void addDataListener(String symbol, AIDataListener listener) {
        dataListeners.computeIfAbsent(symbol, k -> new ArrayList<>()).add(listener);
    }
    
    public AIDataBuffer getDataBuffer(String symbol) {
        return aiBuffers.get(symbol);
    }
    
    public ExtractionMetrics getMetrics() {
        return new ExtractionMetrics(
            activeInstruments.size(),
            totalTicksExtracted.get(),
            totalOrderBookUpdates.get(),
            avgExtractionLatency.get()
        );
    }
    
    public Set<String> detectOpenMarkets() {
        Set<String> markets = new HashSet<>();
        
        // Simulate market detection
        for (Map.Entry<String, String> entry : activeInstruments.entrySet()) {
            String symbol = entry.getValue();
            if (symbol != null && !symbol.isEmpty()) {
                markets.add(symbol);
            }
        }
        
        return markets;
    }
    
    public void shutdown() {
        System.out.println("🛑 Shutting down BookmapAI Data Extractor...");
        
        try {
            aiProcessingPool.shutdown();
            scheduler.shutdown();
            
            if (addonIntegration != null) {
                addonIntegration.shutdown();
            }
            
            System.out.println("✅ BookmapAI Data Extractor shutdown complete");
        } catch (Exception e) {
            System.err.println("Shutdown error: " + e.getMessage());
        }
    }
    
    // ==================== DATA CLASSES ====================
    
    public static class EnhancedTradeData {
        private final String symbol;
        private final double price;
        private final int size;
        private final long timestamp;
        
        public EnhancedTradeData(String symbol, double price, int size, long timestamp) {
            this.symbol = symbol;
            this.price = price;
            this.size = size;
            this.timestamp = timestamp;
        }
        
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public int getSize() { return size; }
        public long getTimestamp() { return timestamp; }
    }
    
    public static class EnhancedDepthData {
        private final String symbol;
        private final boolean isBid;
        private final int price;
        private final int size;
        private final long timestamp;
        
        public EnhancedDepthData(String symbol, boolean isBid, int price, int size, long timestamp) {
            this.symbol = symbol;
            this.isBid = isBid;
            this.price = price;
            this.size = size;
            this.timestamp = timestamp;
        }
        
        public String getSymbol() { return symbol; }
        public boolean isBid() { return isBid; }
        public int getPrice() { return price; }
        public int getSize() { return size; }
        public long getTimestamp() { return timestamp; }
    }
    
    public static class AIFeatureVector {
        private final String symbol;
        private final Map<String, Double> features;
        private final Instant timestamp;
        
        public AIFeatureVector(String symbol, Map<String, Double> features, Instant timestamp) {
            this.symbol = symbol;
            this.features = new HashMap<>(features);
            this.timestamp = timestamp;
        }
        
        public String getSymbol() { return symbol; }
        public Map<String, Double> getFeatures() { return new HashMap<>(features); }
        public Instant getTimestamp() { return timestamp; }
        public double getFeature(String name) { return features.getOrDefault(name, 0.0); }
    }
    
    public static class AIPattern {
        private final String symbol;
        private final String patternType;
        private final double confidence;
        private final double probability;
        private final long timestamp;
        
        public AIPattern(String symbol, String patternType, double confidence, double probability, long timestamp) {
            this.symbol = symbol;
            this.patternType = patternType;
            this.confidence = confidence;
            this.probability = probability;
            this.timestamp = timestamp;
        }
        
        public String getSymbol() { return symbol; }
        public String getPatternType() { return patternType; }
        public double getConfidence() { return confidence; }
        public double getProbability() { return probability; }
        public long getTimestamp() { return timestamp; }
    }
    
    public static class AIDataBuffer {
        private final String symbol;
        private final int maxSize;
        private final ConcurrentLinkedQueue<EnhancedTradeData> trades = new ConcurrentLinkedQueue<>();
        private final ConcurrentLinkedQueue<EnhancedDepthData> depths = new ConcurrentLinkedQueue<>();
        private final ConcurrentLinkedQueue<AIFeatureVector> features = new ConcurrentLinkedQueue<>();
        private final ConcurrentLinkedQueue<AIPattern> patterns = new ConcurrentLinkedQueue<>();
        
        public AIDataBuffer(String symbol, int maxSize) {
            this.symbol = symbol;
            this.maxSize = maxSize;
        }
        
        public void addTrade(EnhancedTradeData trade) {
            trades.offer(trade);
            if (trades.size() > maxSize) trades.poll();
        }
        
        public void addDepth(EnhancedDepthData depth) {
            depths.offer(depth);
            if (depths.size() > maxSize) depths.poll();
        }
        
        public void addFeatureVector(AIFeatureVector feature) {
            features.offer(feature);
            if (features.size() > maxSize) features.poll();
        }
        
        public void addPattern(AIPattern pattern) {
            patterns.offer(pattern);
            if (patterns.size() > maxSize) patterns.poll();
        }
        
        public List<EnhancedTradeData> getRecentTrades(int count) {
            return new ArrayList<>(trades).subList(Math.max(0, trades.size() - count), trades.size());
        }
        
        public List<EnhancedDepthData> getRecentDepth(int count) {
            return new ArrayList<>(depths).subList(Math.max(0, depths.size() - count), depths.size());
        }
        
        public List<AIFeatureVector> getRecentFeatures(int count) {
            return new ArrayList<>(features).subList(Math.max(0, features.size() - count), features.size());
        }
        
        public List<AIPattern> getRecentPatterns(int count) {
            return new ArrayList<>(patterns).subList(Math.max(0, patterns.size() - count), patterns.size());
        }
        
        public String getSymbol() { return symbol; }
    }
    
    public static class BookmapDataStream {
        private final String symbol;
        private final String info;
        
        public BookmapDataStream(String symbol, String info) {
            this.symbol = symbol;
            this.info = info;
        }
        
        public String getSymbol() { return symbol; }
        public String getInfo() { return info; }
    }
    
    public static class TradeFeatures {
        private final double price;
        private final int size;
        private final long timestamp;
        private final boolean isAggressive;
        
        public TradeFeatures(double price, int size, long timestamp, boolean isAggressive) {
            this.price = price;
            this.size = size;
            this.timestamp = timestamp;
            this.isAggressive = isAggressive;
        }
        
        public double getPrice() { return price; }
        public int getSize() { return size; }
        public long getTimestamp() { return timestamp; }
        public boolean isAggressive() { return isAggressive; }
    }
    
    public static class OrderBookFeatures {
        private final int price;
        private final int size;
        private final boolean isBid;
        private final long timestamp;
        
        public OrderBookFeatures(int price, int size, boolean isBid, long timestamp) {
            this.price = price;
            this.size = size;
            this.isBid = isBid;
            this.timestamp = timestamp;
        }
        
        public int getPrice() { return price; }
        public int getSize() { return size; }
        public boolean isBid() { return isBid; }
        public long getTimestamp() { return timestamp; }
    }
    
    public static class ExtractionMetrics {
        private final int activeInstruments;
        private final long totalTicks;
        private final long totalDepthUpdates;
        private final long avgLatency;
        
        public ExtractionMetrics(int activeInstruments, long totalTicks, long totalDepthUpdates, long avgLatency) {
            this.activeInstruments = activeInstruments;
            this.totalTicks = totalTicks;
            this.totalDepthUpdates = totalDepthUpdates;
            this.avgLatency = avgLatency;
        }
        
        public int getActiveInstruments() { return activeInstruments; }
        public long getTotalTicks() { return totalTicks; }
        public long getTotalDepthUpdates() { return totalDepthUpdates; }
        public long getAvgLatency() { return avgLatency; }
    }
    
    public interface AIDataListener {
        void onNewTrade(EnhancedTradeData trade);
        void onNewDepth(EnhancedDepthData depth);
        void onNewFeature(AIFeatureVector feature);
        void onNewPattern(AIPattern pattern);
    }
} 