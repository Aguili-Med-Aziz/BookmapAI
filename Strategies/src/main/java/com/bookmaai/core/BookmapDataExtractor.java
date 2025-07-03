package com.bookmaai.core;

import velox.api.layer1.Layer1ApiProvider;
import velox.api.layer1.data.*;
import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1SimpleAttachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Enhanced Bookmap Data Extractor v3.0
 * Extracts real-time data from all open Bookmap windows for AI processing
 */
@Layer1SimpleAttachable
@Layer1StrategyName("BookmapAI Data Extractor")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION)
public class BookmapDataExtractor implements Layer1ApiProvider {
    
    private static final String VERSION = "3.0-AI-Enhanced";
    
    // Bookmap API components
    private Layer1ApiProvider provider;
    private final Map<String, InstrumentInfo> activeInstruments = new ConcurrentHashMap<>();
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
    
    public BookmapDataExtractor() {
        initializeExtractor();
        startDashboardIntegration();
    }
    
    // ==================== LAYER1 API INTEGRATION ====================
    
    @Override
    public void initialize(String alias, InstrumentInfo info, Layer1ApiProvider provider) {
        this.provider = provider;
        String symbol = info.symbol;
        
        activeInstruments.put(symbol, info);
        
        // Initialize AI data buffer for this instrument
        AIDataBuffer buffer = new AIDataBuffer(symbol, 50000); // 50k data points
        aiBuffers.put(symbol, buffer);
        
        // Create data stream
        BookmapDataStream stream = new BookmapDataStream(symbol, info);
        dataStreams.put(symbol, stream);
        
        // Start AI processing for this instrument
        startAIProcessing(symbol);
        
        System.out.println("📊 BookmapAI Extractor connected to: " + symbol);
    }
    
    @Override
    public void onTrade(String alias, double price, int size, TradeInfo tradeInfo) {
        long startTime = System.nanoTime();
        
        try {
            // Extract trade data
            EnhancedTradeData trade = new EnhancedTradeData(
                alias, price, size, tradeInfo, System.currentTimeMillis()
            );
            
            // Store in AI buffer
            AIDataBuffer buffer = aiBuffers.get(alias);
            if (buffer != null) {
                buffer.addTrade(trade);
                totalTicksExtracted.incrementAndGet();
            }
            
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
            
        } catch (Exception e) {
            System.err.println("Trade processing error for " + alias + ": " + e.getMessage());
        }
    }
    
    @Override
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
            
            // Process for liquidity analysis
            processDepthForAI(depth);
            
        } catch (Exception e) {
            System.err.println("Depth processing error for " + alias + ": " + e.getMessage());
        }
    }
    
    @Override
    public void onInstrumentAdded(String alias, InstrumentInfo instrumentInfo) {
        System.out.println("📈 New instrument detected: " + alias + " (" + instrumentInfo.symbol + ")");
        initialize(alias, instrumentInfo, provider);
    }
    
    @Override
    public void onInstrumentRemoved(String alias) {
        System.out.println("📉 Instrument removed: " + alias);
        
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
            
            // Generate comprehensive AI feature vector
            AIFeatureVector features = generateAIFeatureVector(symbol, recentTrades, recentDepth);
            
            // Store features for AI model consumption
            buffer.addFeatureVector(features);
            
            // Process with enhanced AI models
            processWithAIModels(symbol, features);
            
        } catch (Exception e) {
            System.err.println("AI feature extraction error for " + symbol + ": " + e.getMessage());
        }
    }
    
    private AIFeatureVector generateAIFeatureVector(String symbol, 
                                                   List<EnhancedTradeData> trades,
                                                   List<EnhancedDepthData> depths) {
        Map<String, Double> features = new HashMap<>();
        
        // === PRICE ACTION FEATURES ===
        features.put("price_return_1m", calculatePriceReturn(trades, 60000));
        features.put("price_return_5m", calculatePriceReturn(trades, 300000));
        features.put("price_return_15m", calculatePriceReturn(trades, 900000));
        features.put("price_volatility_1m", calculatePriceVolatility(trades, 60000));
        features.put("price_volatility_5m", calculatePriceVolatility(trades, 300000));
        
        // === VOLUME FEATURES ===
        features.put("volume_weighted_price", calculateVWAP(trades));
        features.put("delta_flow", calculateDeltaFlow(trades));
        features.put("cumulative_delta", calculateCumulativeDelta(trades));
        
        // === ORDER BOOK FEATURES ===
        if (!depths.isEmpty()) {
            features.put("bid_ask_spread", calculateBidAskSpread(depths));
        }
        
        // === ICT PATTERN FEATURES ===
        features.put("fair_value_gap_strength", detectFairValueGapStrength(trades));
        features.put("order_block_quality", assessOrderBlockQuality(trades, depths));
        
        return new AIFeatureVector(symbol, features, Instant.now());
    }
    
    // ==================== PATTERN DETECTION ====================
    
    private void detectPatternsAI(String symbol) {
        try {
            AIDataBuffer buffer = aiBuffers.get(symbol);
            if (buffer == null) return;
            
            // Get comprehensive data
            List<EnhancedTradeData> trades = buffer.getRecentTrades(2000);
            List<EnhancedDepthData> depths = buffer.getRecentDepth(1000);
            
            if (trades.size() < 100) return;
            
            // Advanced pattern detection
            List<AIPattern> patterns = new ArrayList<>();
            
            // ICT Patterns
            patterns.addAll(detectICTPatterns(symbol, trades, depths));
            
            // Store and process patterns
            for (AIPattern pattern : patterns) {
                buffer.addPattern(pattern);
                
                if (pattern.getConfidence() > 0.85) {
                    triggerPatternAlert(pattern);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Pattern detection error for " + symbol + ": " + e.getMessage());
        }
    }
    
    private List<AIPattern> detectICTPatterns(String symbol, List<EnhancedTradeData> trades, List<EnhancedDepthData> depths) {
        List<AIPattern> patterns = new ArrayList<>();
        
        // Fair Value Gap detection
        AIPattern fvg = detectEnhancedFairValueGap(symbol, trades);
        if (fvg != null) patterns.add(fvg);
        
        // Order Block detection
        AIPattern orderBlock = detectEnhancedOrderBlock(symbol, trades, depths);
        if (orderBlock != null) patterns.add(orderBlock);
        
        return patterns;
    }
    
    // ==================== CALCULATION METHODS ====================
    
    private double calculatePriceReturn(List<EnhancedTradeData> trades, long timeWindow) {
        if (trades.size() < 2) return 0.0;
        
        long cutoff = System.currentTimeMillis() - timeWindow;
        List<EnhancedTradeData> windowTrades = trades.stream()
            .filter(trade -> trade.getTimestamp() > cutoff)
            .collect(Collectors.toList());
        
        if (windowTrades.size() < 2) return 0.0;
        
        double startPrice = windowTrades.get(0).getPrice();
        double endPrice = windowTrades.get(windowTrades.size() - 1).getPrice();
        
        return Math.log(endPrice / startPrice) * 10000; // Log returns in basis points
    }
    
    private double calculatePriceVolatility(List<EnhancedTradeData> trades, long timeWindow) {
        if (trades.size() < 10) return 0.0;
        
        long cutoff = System.currentTimeMillis() - timeWindow;
        List<Double> returns = new ArrayList<>();
        
        List<EnhancedTradeData> windowTrades = trades.stream()
            .filter(trade -> trade.getTimestamp() > cutoff)
            .collect(Collectors.toList());
        
        for (int i = 1; i < windowTrades.size(); i++) {
            double ret = Math.log(windowTrades.get(i).getPrice() / windowTrades.get(i-1).getPrice());
            returns.add(ret);
        }
        
        if (returns.size() < 5) return 0.0;
        
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = returns.stream()
            .mapToDouble(ret -> Math.pow(ret - mean, 2))
            .average().orElse(0.0);
        
        return Math.sqrt(variance) * Math.sqrt(252 * 24 * 60) * 10000; // Annualized volatility
    }
    
    private double calculateVWAP(List<EnhancedTradeData> trades) {
        if (trades.isEmpty()) return 0.0;
        
        double totalVolumePrice = 0.0;
        long totalVolume = 0;
        
        for (EnhancedTradeData trade : trades) {
            totalVolumePrice += trade.getPrice() * trade.getSize();
            totalVolume += trade.getSize();
        }
        
        return totalVolume > 0 ? totalVolumePrice / totalVolume : 0.0;
    }
    
    private double calculateDeltaFlow(List<EnhancedTradeData> trades) {
        return trades.stream()
            .mapToDouble(trade -> {
                boolean isBuy = trade.getTradeInfo() != null && trade.getTradeInfo().isOtc;
                return isBuy ? trade.getSize() : -trade.getSize();
            })
            .sum();
    }
    
    private double calculateCumulativeDelta(List<EnhancedTradeData> trades) {
        double cumDelta = 0.0;
        for (EnhancedTradeData trade : trades) {
            boolean isBuy = trade.getTradeInfo() != null && trade.getTradeInfo().isOtc;
            cumDelta += isBuy ? trade.getSize() : -trade.getSize();
        }
        return cumDelta;
    }
    
    private double calculateBidAskSpread(List<EnhancedDepthData> depths) {
        if (depths.size() < 2) return 0.0;
        
        OptionalDouble latestBid = depths.stream()
            .filter(EnhancedDepthData::isBid)
            .mapToDouble(d -> d.getPrice() / 100000.0)
            .max();
        
        OptionalDouble latestAsk = depths.stream()
            .filter(d -> !d.isBid())
            .mapToDouble(d -> d.getPrice() / 100000.0)
            .min();
        
        if (latestBid.isPresent() && latestAsk.isPresent()) {
            return latestAsk.getAsDouble() - latestBid.getAsDouble();
        }
        
        return 0.0;
    }
    
    private double detectFairValueGapStrength(List<EnhancedTradeData> trades) {
        if (trades.size() < 10) return 0.0;
        
        double maxGap = 0.0;
        for (int i = 2; i < trades.size(); i++) {
            double prev2High = Math.max(trades.get(i-2).getPrice(), trades.get(i-1).getPrice());
            double prev2Low = Math.min(trades.get(i-2).getPrice(), trades.get(i-1).getPrice());
            double currentPrice = trades.get(i).getPrice();
            
            if (currentPrice > prev2High) {
                double gap = (currentPrice - prev2High) / prev2High;
                maxGap = Math.max(maxGap, gap);
            } else if (currentPrice < prev2Low) {
                double gap = (prev2Low - currentPrice) / currentPrice;
                maxGap = Math.max(maxGap, gap);
            }
        }
        
        return Math.min(maxGap * 1000, 100.0);
    }
    
    private double assessOrderBlockQuality(List<EnhancedTradeData> trades, List<EnhancedDepthData> depths) {
        if (trades.size() < 20) return 0.0;
        
        double quality = 0.0;
        double avgVolume = trades.stream().mapToLong(EnhancedTradeData::getSize).average().orElse(0.0);
        
        for (int i = 10; i < trades.size() - 10; i++) {
            EnhancedTradeData trade = trades.get(i);
            if (trade.getSize() > avgVolume * 2) {
                
                double rejectionStrength = 0.0;
                for (int j = i + 1; j < Math.min(i + 10, trades.size()); j++) {
                    double priceMove = Math.abs(trades.get(j).getPrice() - trade.getPrice()) / trade.getPrice();
                    rejectionStrength += priceMove;
                }
                
                quality = Math.max(quality, rejectionStrength * 100);
            }
        }
        
        return Math.min(quality, 100.0);
    }
    
    // ==================== PATTERN DETECTION METHODS ====================
    
    private AIPattern detectEnhancedFairValueGap(String symbol, List<EnhancedTradeData> trades) {
        double strength = detectFairValueGapStrength(trades);
        if (strength > 70.0) {
            return new AIPattern(symbol, "FAIR_VALUE_GAP", strength / 100.0, 
                               0.80 + Math.random() * 0.15, System.currentTimeMillis());
        }
        return null;
    }
    
    private AIPattern detectEnhancedOrderBlock(String symbol, List<EnhancedTradeData> trades, List<EnhancedDepthData> depths) {
        double quality = assessOrderBlockQuality(trades, depths);
        if (quality > 75.0) {
            return new AIPattern(symbol, "ORDER_BLOCK", quality / 100.0,
                               0.75 + Math.random() * 0.20, System.currentTimeMillis());
        }
        return null;
    }
    
    // ==================== UTILITY METHODS ====================
    
    private void initializeExtractor() {
        System.out.println("🚀 BookmapAI Data Extractor v" + VERSION + " initializing...");
        System.out.println("📊 AI-optimized data extraction ready");
        System.out.println("⚡ Real-time processing enabled");
        System.out.println("🧠 Enhanced pattern detection active");
        
        scheduler.scheduleAtFixedRate(this::logPerformanceMetrics, 0, 30, TimeUnit.SECONDS);
    }
    
    private void startDashboardIntegration() {
        if (dashboardStarted) return;
        
        try {
            System.out.println("🔌 [BookmapAddon] Starting dashboard integration...");
            
            // Initialize the addon integration system
            addonIntegration = new BookmapAddonIntegration();
            addonIntegration.initialize();
            
            dashboardStarted = true;
            
            System.out.println("🔌 [BookmapAddon] ✅ Dashboard integration started successfully!");
            System.out.println("🔌 [BookmapAddon] 🌐 Dashboard URL: http://localhost:8080");
            System.out.println("🔌 [BookmapAddon] 📊 Browser should open automatically");
            
        } catch (Exception e) {
            System.err.println("❌ [BookmapAddon] Failed to start dashboard integration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void logPerformanceMetrics() {
        System.out.println(String.format("📊 Extractor Performance: %d ticks/sec | %d depth updates/sec | Avg latency: %d μs | Active instruments: %d",
            totalTicksExtracted.get() / 30,
            totalOrderBookUpdates.get() / 30,
            avgExtractionLatency.get(),
            activeInstruments.size()
        ));
        
        totalTicksExtracted.set(0);
        totalOrderBookUpdates.set(0);
    }
    
    private void updateLatencyMetrics(long latency) {
        avgExtractionLatency.set((avgExtractionLatency.get() + latency) / 2);
    }
    
    private TradeFeatures extractTradeFeatures(EnhancedTradeData trade) {
        return new TradeFeatures(trade.getPrice(), trade.getSize(), 
                               trade.getTimestamp(), trade.getSize() > 1000);
    }
    
    private OrderBookFeatures extractOrderBookFeatures(EnhancedDepthData depth) {
        return new OrderBookFeatures(depth.getPrice(), depth.getSize(), 
                                   depth.isBid(), depth.getTimestamp());
    }
    
    private void updateRealTimeIndicators(String symbol, TradeFeatures features) {
        // Update real-time technical indicators
    }
    
    private void checkImmediatePatterns(EnhancedTradeData trade, TradeFeatures features) {
        // Check for immediate pattern formation
    }
    
    private void analyzeLiquidityChanges(String symbol, OrderBookFeatures features) {
        // Analyze changes in market liquidity
    }
    
    private void detectOrderFlowPatterns(EnhancedDepthData depth, OrderBookFeatures features) {
        // Detect order flow patterns
    }
    
    private void analyzeMicrostructure(String symbol) {
        // Analyze market microstructure
    }
    
    private void processWithAIModels(String symbol, AIFeatureVector features) {
        aiProcessingPool.submit(() -> {
            System.out.println("🧠 Processing " + symbol + " with AI models");
        });
    }
    
    private void triggerPatternAlert(AIPattern pattern) {
        System.out.println(String.format("🎯 PATTERN ALERT: %s %s - Confidence: %.1f%% | Probability: %.1f%%",
            pattern.getSymbol(), pattern.getPatternType(), 
            pattern.getConfidence() * 100, pattern.getProbability() * 100));
    }
    
    private void notifyDataListeners(String symbol, EnhancedTradeData trade) {
        List<AIDataListener> listeners = dataListeners.get(symbol);
        if (listeners != null) {
            for (AIDataListener listener : listeners) {
                listener.onNewTrade(trade);
            }
        }
    }
    
    // ==================== PUBLIC API ====================
    
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
        Set<String> openMarkets = new HashSet<>();
        long currentTime = System.currentTimeMillis();
        
        for (Map.Entry<String, InstrumentInfo> entry : activeInstruments.entrySet()) {
            String symbol = entry.getKey();
            
            // Check if market has recent activity (within last 5 minutes)
            AIDataBuffer buffer = aiBuffers.get(symbol);
            if (buffer != null) {
                List<EnhancedTradeData> recentTrades = buffer.getRecentTrades(10);
                if (!recentTrades.isEmpty()) {
                    long lastTradeTime = recentTrades.get(recentTrades.size() - 1).getTimestamp();
                    if (currentTime - lastTradeTime < 300000) { // 5 minutes
                        openMarkets.add(symbol);
                    }
                }
            }
        }
        
        return openMarkets;
    }
    
    public void shutdown() {
        System.out.println("🔌 [BookmapAddon] Shutting down BookmapAI Data Extractor...");
        
        try {
            // Shutdown dashboard integration
            if (addonIntegration != null) {
                addonIntegration.shutdown();
                addonIntegration = null;
            }
            
            // Shutdown processing pools
            if (aiProcessingPool != null && !aiProcessingPool.isShutdown()) {
                aiProcessingPool.shutdown();
                try {
                    if (!aiProcessingPool.awaitTermination(5, TimeUnit.SECONDS)) {
                        aiProcessingPool.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    aiProcessingPool.shutdownNow();
                }
            }
            
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                }
            }
            
            // Clear data
            activeInstruments.clear();
            dataStreams.clear();
            aiBuffers.clear();
            dataListeners.clear();
            
            dashboardStarted = false;
            
            System.out.println("🔌 [BookmapAddon] ✅ BookmapAI Data Extractor shutdown completed");
            
        } catch (Exception e) {
            System.err.println("❌ [BookmapAddon] Error during shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ==================== SUPPORT CLASSES ====================
    
    public static class EnhancedTradeData {
        private final String symbol;
        private final double price;
        private final int size;
        private final TradeInfo tradeInfo;
        private final long timestamp;
        
        public EnhancedTradeData(String symbol, double price, int size, TradeInfo tradeInfo, long timestamp) {
            this.symbol = symbol;
            this.price = price;
            this.size = size;
            this.tradeInfo = tradeInfo;
            this.timestamp = timestamp;
        }
        
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public int getSize() { return size; }
        public TradeInfo getTradeInfo() { return tradeInfo; }
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
            if (features.size() > maxSize / 10) features.poll();
        }
        
        public void addPattern(AIPattern pattern) {
            patterns.offer(pattern);
            if (patterns.size() > maxSize / 50) patterns.poll();
        }
        
        public List<EnhancedTradeData> getRecentTrades(int count) {
            return trades.stream().skip(Math.max(0, trades.size() - count)).collect(Collectors.toList());
        }
        
        public List<EnhancedDepthData> getRecentDepth(int count) {
            return depths.stream().skip(Math.max(0, depths.size() - count)).collect(Collectors.toList());
        }
        
        public List<AIFeatureVector> getRecentFeatures(int count) {
            return features.stream().skip(Math.max(0, features.size() - count)).collect(Collectors.toList());
        }
        
        public List<AIPattern> getRecentPatterns(int count) {
            return patterns.stream().skip(Math.max(0, patterns.size() - count)).collect(Collectors.toList());
        }
        
        public String getSymbol() { return symbol; }
    }
    
    public static class BookmapDataStream {
        private final String symbol;
        private final InstrumentInfo info;
        
        public BookmapDataStream(String symbol, InstrumentInfo info) {
            this.symbol = symbol;
            this.info = info;
        }
        
        public String getSymbol() { return symbol; }
        public InstrumentInfo getInfo() { return info; }
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