package com.bookmaai.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Real-Time Market Data Store
 * 
 * Centralized storage for live market data that updates in real-time.
 * Provides dynamic data to dashboard directly from Bookmap feeds.
 * REAL DATA ONLY - No simulated data!
 */
public class RealTimeMarketDataStore {
    
    private static RealTimeMarketDataStore instance;
    private final Map<String, MarketData> marketData = new ConcurrentHashMap<>();
    private final Map<String, PatternData> patternData = new ConcurrentHashMap<>();
    private final List<TradeEvent> recentTrades = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, SystemMetrics> systemMetrics = new ConcurrentHashMap<>();
    
    // Active Bookmap Windows tracking (REAL DATA)
    private final Map<String, BookmapWindow> activeBookmapWindows = new ConcurrentHashMap<>();
    private volatile long windowsLastUpdate = 0;
    private volatile boolean hasRealDataConnections = false;
    
    // Enhanced instrument tracking
    private final Map<String, InstrumentDetails> instrumentDetails = new ConcurrentHashMap<>();
    private final Map<String, InstrumentHealthStatus> instrumentHealth = new ConcurrentHashMap<>();
    private final Map<String, Long> lastDataTimestamp = new ConcurrentHashMap<>();
    private final Map<String, Integer> dataFlowCounts = new ConcurrentHashMap<>();
    
    // Performance caching
    private volatile String cachedActiveWindowsJson = null;
    private volatile long cacheTimestamp = 0;
    private static final long CACHE_DURATION_MS = 1000; // 1 second cache
    private volatile java.util.Set<String> cachedReallyActiveInstruments = null;
    private volatile long activeInstrumentsCacheTimestamp = 0;
    
    // Session-based data structures (REAL DATA)
    private final Map<String, TradingSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, List<PatternDetection>> sessionPatterns = new ConcurrentHashMap<>();
    private final Map<String, List<AIPrediction>> sessionPredictions = new ConcurrentHashMap<>();
    private final Map<String, SessionAnalytics> sessionAnalytics = new ConcurrentHashMap<>();
    
    private volatile long lastUpdateTime = 0;
    private volatile boolean isActive = false;

    // Listeners for Bookmap window lifecycle
    private final java.util.Set<BookmapWindowListener> windowListeners = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
    
    private RealTimeMarketDataStore() {
        lastUpdateTime = System.currentTimeMillis();
        isActive = true;
        System.out.println("🔥 RealTimeMarketDataStore initialized - REAL DATA ONLY MODE");
    }
    
    public static synchronized RealTimeMarketDataStore getInstance() {
        if (instance == null) {
            instance = new RealTimeMarketDataStore();
        }
        return instance;
    }
    
    // ==================== REAL BOOKMAP DATA UPDATES ====================
    
    /**
     * Add active Bookmap window (called when user opens a chart)
     */
    public void addActiveWindow(String symbol, String status, String alias) {
        String windowId = alias != null ? alias : symbol;
        BookmapWindow window = new BookmapWindow(windowId, symbol, status, "LIVE", System.currentTimeMillis());
        activeBookmapWindows.put(windowId, window);
        windowsLastUpdate = System.currentTimeMillis();
        hasRealDataConnections = true;
        
        // Invalidate caches on data change
        invalidateCaches();
        
        System.out.println("📊 REAL Bookmap window opened: " + symbol + " (" + status + ")");

        // Notify listeners
        notifyWindowOpened(window);
    }
    
    /**
     * Check if system has real data connections
     */
    public boolean hasRealDataConnections() {
        return hasRealDataConnections;
    }
    
    /**
     * Get market data for symbol
     */
    public MarketData getMarketData(String symbol) {
        return marketData.get(symbol);
    }
    
    /**
     * Remove active Bookmap window (called when user closes a chart)
     */
    public void removeActiveWindow(String symbol) {
        java.util.List<java.util.Map.Entry<String, BookmapWindow>> toRemove = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, BookmapWindow> entry : activeBookmapWindows.entrySet()) {
            if (entry.getValue().getSymbol().equals(symbol)) {
                toRemove.add(entry);
            }
        }
        for (java.util.Map.Entry<String, BookmapWindow> entry : toRemove) {
            activeBookmapWindows.remove(entry.getKey());
            notifyWindowClosed(entry.getKey(), entry.getValue().getSymbol());
        }
        windowsLastUpdate = System.currentTimeMillis();
        
        if (activeBookmapWindows.isEmpty()) {
            hasRealDataConnections = false;
        }
        
        System.out.println("📊 REAL Bookmap window closed: " + symbol);
    }

    /**
     * Remove active window by windowId (alias)
     */
    public void removeActiveWindowById(String windowId) {
        BookmapWindow removed = activeBookmapWindows.remove(windowId);
        if (removed != null) {
            notifyWindowClosed(windowId, removed.getSymbol());
        }
        
        // Clean up enhanced tracking data
        instrumentDetails.remove(windowId);
        instrumentHealth.remove(windowId);
        lastDataTimestamp.remove(windowId);
        dataFlowCounts.remove(windowId);
        
        // Invalidate caches on data change
        invalidateCaches();
        
        windowsLastUpdate = System.currentTimeMillis();
        if (activeBookmapWindows.isEmpty()) {
            hasRealDataConnections = false;
        }
        System.out.println("📊 REAL Bookmap window closed (by id): " + windowId);
    }
    
    // ==================== ENHANCED INSTRUMENT TRACKING ====================
    
    /**
     * Register comprehensive instrument details
     */
    public void registerInstrumentDetails(String alias, String symbol, String exchange, double pips) {
        InstrumentDetails details = new InstrumentDetails(alias, symbol, exchange, pips, System.currentTimeMillis());
        instrumentDetails.put(alias, details);
        
        // Initialize health tracking
        instrumentHealth.put(alias, new InstrumentHealthStatus(alias, true, System.currentTimeMillis()));
        lastDataTimestamp.put(alias, System.currentTimeMillis());
        dataFlowCounts.put(alias, 0);
        
        System.out.println("📋 Registered instrument details: " + symbol + " (" + exchange + ")");
    }
    
    /**
     * Initialize tracking for an instrument
     */
    public void initializeInstrumentTracking(String alias) {
        instrumentHealth.put(alias, new InstrumentHealthStatus(alias, true, System.currentTimeMillis()));
        lastDataTimestamp.put(alias, System.currentTimeMillis());
        dataFlowCounts.put(alias, 0);
        
        System.out.println("🎯 Initialized tracking for instrument: " + alias);
    }
    
    /**
     * Validate and track data flow for an instrument
     */
    public void validateInstrumentDataFlow(String alias, double price, int size, long timestamp) {
        // Update data flow tracking
        lastDataTimestamp.put(alias, timestamp);
        dataFlowCounts.put(alias, dataFlowCounts.getOrDefault(alias, 0) + 1);
        
        // Update health status
        InstrumentHealthStatus health = instrumentHealth.get(alias);
        if (health != null) {
            health.updateHealth(true, timestamp);
            health.recordDataPoint(price, size);
        }
    }
    
    /**
     * Check if instrument is really active with recent data flow
     */
    public boolean isInstrumentReallyActive(String alias) {
        // 1. Check if registered in active windows
        if (!activeBookmapWindows.containsKey(alias)) {
            return false;
        }
        
        // 2. Verify recent data flow (last 5 minutes)
        Long lastData = lastDataTimestamp.get(alias);
        if (lastData == null || (System.currentTimeMillis() - lastData) > (5 * 60 * 1000)) {
            return false;
        }
        
        // 3. Check health status
        InstrumentHealthStatus health = instrumentHealth.get(alias);
        if (health == null || !health.isHealthy()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get comprehensive instrument status
     */
    public Map<String, Object> getInstrumentStatus(String alias) {
        Map<String, Object> status = new HashMap<>();
        
        // Basic info
        status.put("alias", alias);
        status.put("is_active", activeBookmapWindows.containsKey(alias));
        status.put("is_really_active", isInstrumentReallyActive(alias));
        
        // Details
        InstrumentDetails details = instrumentDetails.get(alias);
        if (details != null) {
            status.put("symbol", details.getSymbol());
            status.put("exchange", details.getExchange());
            status.put("pips", details.getPips());
        }
        
        // Health and data flow
        InstrumentHealthStatus health = instrumentHealth.get(alias);
        if (health != null) {
            status.put("health_status", health.isHealthy() ? "HEALTHY" : "UNHEALTHY");
            status.put("last_health_check", health.getLastHealthCheck());
            status.put("data_points_count", health.getDataPointsCount());
        }
        
        status.put("last_data_timestamp", lastDataTimestamp.get(alias));
        status.put("data_flow_count", dataFlowCounts.getOrDefault(alias, 0));
        
        return status;
    }
    
    /**
     * Get all really active instruments (with data flow validation and caching)
     */
    public java.util.Set<String> getReallyActiveInstruments() {
        long now = System.currentTimeMillis();
        
        // Use cached result if still fresh
        if (cachedReallyActiveInstruments != null && 
            (now - activeInstrumentsCacheTimestamp) < CACHE_DURATION_MS) {
            return cachedReallyActiveInstruments;
        }
        
        // Compute fresh result
        java.util.Set<String> result = activeBookmapWindows.keySet().stream()
            .filter(this::isInstrumentReallyActive)
            .collect(java.util.stream.Collectors.toSet());
            
        // Cache the result
        cachedReallyActiveInstruments = result;
        activeInstrumentsCacheTimestamp = now;
        
        return result;
    }
    
    /**
     * Invalidate performance caches when data changes
     */
    private void invalidateCaches() {
        cachedActiveWindowsJson = null;
        cacheTimestamp = 0;
        cachedReallyActiveInstruments = null;
        activeInstrumentsCacheTimestamp = 0;
    }
    
    /**
     * Update market data from REAL Bookmap feeds
     */
    public void updateMarketData(String symbol, double price, double volume, String side) {
        // Calculate change percentage from previous price
        MarketData existing = marketData.get(symbol);
        double change = 0.0;
        if (existing != null && existing.getPrice() > 0) {
            change = ((price - existing.getPrice()) / existing.getPrice()) * 100;
        }
        
        // Update market data with REAL values
        MarketData data = new MarketData(
            symbol, 
            price, 
            (long)volume, 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            change,
            side
        );
        marketData.put(symbol, data);
        
        // Add to recent trades
        TradeEvent trade = new TradeEvent(symbol, price, (long)volume, side, System.currentTimeMillis());
        recentTrades.add(trade);
        
        // Keep only last 1000 trades
        if (recentTrades.size() > 1000) {
            recentTrades.remove(0);
        }
        
        lastUpdateTime = System.currentTimeMillis();
        hasRealDataConnections = true;
        
        // Update the corresponding Bookmap window data
        updateBookmapWindowData(symbol, price, volume, change);
    }
    
    /**
     * Update market data with integer price/size (for different data types)
     */
    public void updateMarketData(String symbol, int price, int size, String side) {
        updateMarketData(symbol, (double)price, (double)size, side);
    }
    
    /**
     * Update pattern data from REAL analysis
     */
    public void updatePatternData(String pattern, double confidence, String symbol) {
        PatternData data = new PatternData(pattern, confidence, symbol, LocalDateTime.now().toString());
        patternData.put(symbol + "_" + pattern, data);
        System.out.println("📊 REAL pattern detected: " + pattern + " for " + symbol + " (confidence: " + confidence + "%)");

        // Also export to per-instrument CSV (thread-safe)
        try {
            String detailsJson = "{\"source\":\"RealTimeMarketDataStore\"}";
            com.bookmaai.core.PatternRecorder.getInstance().writePattern(symbol, pattern, confidence, detailsJson);
        } catch (Throwable t) {
            System.err.println("Pattern CSV export failed: " + t.getMessage());
        }
    }
    
    // ==================== SESSION MANAGEMENT (REAL DATA) ====================
    
    /**
     * Create or update a trading session for a specific Bookmap window
     */
    public void updateTradingSession(String sessionId, String symbol, String marketType, 
                                   double currentPrice, String priceChange, String volume, String volumeLevel) {
        TradingSession session = new TradingSession(
            sessionId, symbol, marketType, currentPrice, priceChange, 
            volume, volumeLevel, System.currentTimeMillis()
        );
        activeSessions.put(sessionId, session);
        
        // Initialize session data structures if new
        sessionPatterns.putIfAbsent(sessionId, Collections.synchronizedList(new ArrayList<>()));
        sessionPredictions.putIfAbsent(sessionId, Collections.synchronizedList(new ArrayList<>()));
        sessionAnalytics.putIfAbsent(sessionId, new SessionAnalytics(sessionId, "Analyzing...", "Neutral", "Normal", 0));
        
        System.out.println("📈 Trading session updated: " + sessionId + " (" + symbol + ")");
    }
    
    /**
     * Add pattern detection to a specific session
     */
    public void addSessionPattern(String sessionId, String patternType, double confidence, String description) {
        List<PatternDetection> patterns = sessionPatterns.get(sessionId);
        if (patterns != null) {
            PatternDetection pattern = new PatternDetection(
                patternType, confidence, description, System.currentTimeMillis()
            );
            patterns.add(pattern);
            
            // Keep only last 20 patterns per session
            if (patterns.size() > 20) {
                patterns.remove(0);
            }
            
            System.out.println("🔍 Pattern added to session " + sessionId + ": " + patternType + " (" + confidence + "%)");
        }
    }
    
    /**
     * Add AI prediction to a specific session
     */
    public void addSessionPrediction(String sessionId, String direction, double confidence, 
                                   String target, String timeframe) {
        List<AIPrediction> predictions = sessionPredictions.get(sessionId);
        if (predictions != null) {
            AIPrediction prediction = new AIPrediction(
                direction, confidence, target, timeframe, System.currentTimeMillis()
            );
            predictions.add(prediction);
            
            // Keep only last 10 predictions per session
            if (predictions.size() > 10) {
                predictions.remove(0);
            }
            
            System.out.println("🤖 Prediction added to session " + sessionId + ": " + direction + " (" + confidence + "%)");
        }
    }
    
    /**
     * Update session analytics
     */
    public void updateSessionAnalytics(String sessionId, String orderFlow, String marketPressure, 
                                     String volatility, double sessionScore) {
        SessionAnalytics analytics = new SessionAnalytics(sessionId, orderFlow, marketPressure, volatility, sessionScore);
        sessionAnalytics.put(sessionId, analytics);
    }
    
    /**
     * Remove a trading session
     */
    public void removeSession(String sessionId) {
        activeSessions.remove(sessionId);
        sessionPatterns.remove(sessionId);
        sessionPredictions.remove(sessionId);
        sessionAnalytics.remove(sessionId);
        System.out.println("❌ Trading session removed: " + sessionId);
    }
    
    /**
     * Update Bookmap window data with latest price/volume
     */
    private void updateBookmapWindowData(String symbol, double price, double volume, double change) {
        for (BookmapWindow window : activeBookmapWindows.values()) {
            if (window.getSymbol().equals(symbol)) {
                // Update window with current price data
                window.updateData(price, volume, change);
                break;
            }
        }
    }
    
    // ==================== DATA RETRIEVAL (REAL DATA) ====================
    
    public Map<String, MarketData> getMarketData() {
        return new ConcurrentHashMap<>(marketData);
    }
    
    public Map<String, PatternData> getPatternData() {
        return new ConcurrentHashMap<>(patternData);
    }
    
    public List<TradeEvent> getRecentTrades() {
        return new ArrayList<>(recentTrades);
    }
    
    public Map<String, SystemMetrics> getSystemMetrics() {
        return new ConcurrentHashMap<>(systemMetrics);
    }
    
    public Map<String, Object> getActiveBookmapWindows() {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, BookmapWindow> entry : activeBookmapWindows.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Utility to force-register a window when alias is known but initialize didn't fire yet.
     */
    public void ensureWindowRegistered(String alias) {
        if (!activeBookmapWindows.containsKey(alias)) {
            addActiveWindow(alias, "ACTIVE", alias);
        }
    }

    /**
     * Register a listener for Bookmap window lifecycle events.
     */
    public void registerBookmapWindowListener(BookmapWindowListener listener) {
        if (listener != null) {
            windowListeners.add(listener);
        }
    }

    /**
     * Unregister a previously registered listener.
     */
    public void unregisterBookmapWindowListener(BookmapWindowListener listener) {
        if (listener != null) {
            windowListeners.remove(listener);
        }
    }

    private void notifyWindowOpened(BookmapWindow window) {
        synchronized (windowListeners) {
            for (BookmapWindowListener l : windowListeners) {
                try {
                    l.onWindowOpened(window);
                } catch (Throwable t) {
                    System.err.println("Listener error onWindowOpened: " + t.getMessage());
                }
            }
        }
    }

    private void notifyWindowClosed(String windowId, String symbol) {
        synchronized (windowListeners) {
            for (BookmapWindowListener l : windowListeners) {
                try {
                    l.onWindowClosed(windowId, symbol);
                } catch (Throwable t) {
                    System.err.println("Listener error onWindowClosed: " + t.getMessage());
                }
            }
        }
    }
    
    /**
     * Check if we have real data connections
     */
    public boolean hasRealData() {
        return hasRealDataConnections && !activeBookmapWindows.isEmpty();
    }
    
    // ==================== SESSION DATA RETRIEVAL (REAL DATA) ====================
    
    public Map<String, TradingSession> getActiveSessions() {
        return new ConcurrentHashMap<>(activeSessions);
    }
    
    public List<PatternDetection> getSessionPatterns(String sessionId) {
        List<PatternDetection> patterns = sessionPatterns.get(sessionId);
        return patterns != null ? new ArrayList<>(patterns) : new ArrayList<>();
    }
    
    public List<AIPrediction> getSessionPredictions(String sessionId) {
        List<AIPrediction> predictions = sessionPredictions.get(sessionId);
        return predictions != null ? new ArrayList<>(predictions) : new ArrayList<>();
    }
    
    public SessionAnalytics getSessionAnalytics(String sessionId) {
        return sessionAnalytics.get(sessionId);
    }
    
    /**
     * Check if we have active sessions
     */
    public boolean hasActiveSessions() {
        return !activeSessions.isEmpty();
    }
    
    // ==================== JSON RESPONSES (REAL DATA) ====================
    
    public String getMarketDataJson() {
        if (marketData.isEmpty()) {
            return "{\"status\": \"NO_DATA\", \"message\": \"No active Bookmap windows. Open charts in Bookmap to see real data.\"}";
        }
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"status\": \"REAL_DATA\",");
        json.append("\"last_update\": ").append(lastUpdateTime).append(",");
        json.append("\"active_symbols\": [");
        
        boolean first = true;
        for (MarketData data : marketData.values()) {
            if (!first) json.append(",");
            json.append("{");
            json.append("\"name\": \"").append(data.getSymbol()).append("\",");
            json.append("\"price\": ").append(data.getPrice()).append(",");
            json.append("\"volume\": ").append(data.getVolume()).append(",");
            json.append("\"change\": ").append(data.getChange()).append(",");
            json.append("\"side\": \"").append(data.getSide()).append("\",");
            json.append("\"timestamp\": \"").append(data.getTimestamp()).append("\",");
            json.append("\"data_type\": \"REAL_TRADE\"");
            json.append("}");
            first = false;
        }
        
        json.append("]}");
        return json.toString();
    }
    
    public String getPatternDataJson() {
        if (patternData.isEmpty()) {
            return "{\"status\": \"NO_PATTERNS\", \"message\": \"No patterns detected yet.\"}";
        }
        
        StringBuilder json = new StringBuilder();
        json.append("{\"status\": \"REAL_PATTERNS\", \"patterns\": [");
        
        boolean first = true;
        for (PatternData data : patternData.values()) {
            if (!first) json.append(",");
            json.append("{");
            json.append("\"pattern\": \"").append(data.getPattern()).append("\",");
            json.append("\"confidence\": ").append(data.getConfidence()).append(",");
            json.append("\"symbol\": \"").append(data.getSymbol()).append("\",");
            json.append("\"timestamp\": \"").append(data.getTimestamp()).append("\"");
            json.append("}");
            first = false;
        }
        
        json.append("]}");
        return json.toString();
    }
    
    public String getSystemStatusJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"status\": \"").append(hasRealData() ? "REAL_DATA_ACTIVE" : "WAITING_FOR_DATA").append("\",");
        json.append("\"active_windows\": ").append(activeBookmapWindows.size()).append(",");
        json.append("\"total_symbols\": ").append(marketData.size()).append(",");
        json.append("\"total_trades\": ").append(recentTrades.size()).append(",");
        json.append("\"last_update\": ").append(lastUpdateTime).append(",");
        json.append("\"data_source\": \"").append(hasRealData() ? "REAL_BOOKMAP" : "NO_CONNECTION").append("\"");
        json.append("}");
        return json.toString();
    }
    
    public String getOrderFlowAnalysisJson() {
        if (!hasRealData()) {
            return "{\"status\": \"NO_DATA\", \"message\": \"No real Bookmap data available for analysis.\"}";
        }
        
        // Return real order flow analysis based on actual trade data
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"status\": \"REAL_ANALYSIS\",");
        json.append("\"total_events_processed\": ").append(recentTrades.size()).append(",");
        json.append("\"active_symbols\": ").append(marketData.size()).append(",");
        json.append("\"last_analysis_time\": ").append(System.currentTimeMillis()).append(",");
        json.append("\"data_source\": \"REAL_BOOKMAP_FEEDS\"");
        json.append("}");
        return json.toString();
    }
    
    public String getActiveBookmapWindowsJson() {
        if (activeBookmapWindows.isEmpty()) {
            return "{" +
                "\"status\": \"NO_WINDOWS\"," +
                "\"active_windows_count\": 0," +
                "\"windows_last_update\": " + System.currentTimeMillis() + "," +
                "\"message\": \"No active Bookmap windows. Open charts in Bookmap to see real data.\"," +
                "\"active_windows\": []" +
            "}";
        }
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"status\": \"ACTIVE\",");
        json.append("\"active_windows_count\": ").append(activeBookmapWindows.size()).append(",");
        json.append("\"windows_last_update\": ").append(windowsLastUpdate).append(",");
        json.append("\"active_windows\": [");
        
        boolean first = true;
        for (BookmapWindow window : activeBookmapWindows.values()) {
            if (!first) json.append(",");
            
            json.append("{");
            json.append("\"window_id\": \"").append(window.getWindowId()).append("\",");
            json.append("\"symbol\": \"").append(window.getSymbol()).append("\",");
            json.append("\"market_type\": \"").append(determineMarketType(window.getSymbol())).append("\",");
            json.append("\"price\": \"").append(String.format("%.2f", window.getCurrentPrice())).append("\",");
            json.append("\"change\": \"").append(String.format("%+.2f", window.getPriceChange())).append("\",");
            json.append("\"volume\": \"").append(formatVolume(window.getCurrentVolume())).append("\",");
            json.append("\"last_activity\": \"").append(getTimeAgo(window.getLastUpdate())).append("\",");
            json.append("\"status\": \"").append(window.getStatus()).append("\"");
            json.append("}");
            first = false;
        }
        
        json.append("]}");
        return json.toString();
    }
    
    public void updateActiveBookmapWindows(java.util.Map<String,String> windowData) {
        // This method can be used by external systems to bulk update window data
        for (Map.Entry<String, String> entry : windowData.entrySet()) {
            addActiveWindow(entry.getKey(), entry.getValue(), entry.getKey());
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    private String determineMarketType(String symbol) {
        if (symbol.contains("EUR") || symbol.contains("GBP") || symbol.contains("USD") || symbol.contains("JPY")) {
            return "FOREX";
        } else if (symbol.contains("BTC") || symbol.contains("ETH") || symbol.contains("USDT")) {
            return "CRYPTO";
        } else if (symbol.contains("ES") || symbol.contains("NQ") || symbol.contains("DEC") || symbol.contains("MAR")) {
            return "FUTURES";
        } else {
            return "SPOT";
        }
    }
    
    private String formatVolume(double volume) {
        if (volume >= 1000000) {
            return String.format("%.1fM", volume / 1000000);
        } else if (volume >= 1000) {
            return String.format("%.1fK", volume / 1000);
        } else {
            return String.format("%.0f", volume);
        }
    }
    
    private String getTimeAgo(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < 1000) return "Live";
        if (diff < 60000) return (diff / 1000) + "s ago";
        if (diff < 3600000) return (diff / 60000) + "m ago";
        return (diff / 3600000) + "h ago";
    }
    
    // ==================== DATA CLASSES (REAL DATA STRUCTURES) ====================
    
    public static class MarketData {
        private final String symbol;
        private final double price;
        private final long volume;
        private final String timestamp;
        private final double change;
        private final String side;
        
        public MarketData(String symbol, double price, long volume, String timestamp, double change, String side) {
            this.symbol = symbol;
            this.price = price;
            this.volume = volume;
            this.timestamp = timestamp;
            this.change = change;
            this.side = side;
        }
        
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public long getVolume() { return volume; }
        public String getTimestamp() { return timestamp; }
        public double getChange() { return change; }
        public String getSide() { return side; }
    }
    
    public static class PatternData {
        private final String pattern;
        private final double confidence;
        private final String symbol;
        private final String timestamp;
        
        public PatternData(String pattern, double confidence, String symbol, String timestamp) {
            this.pattern = pattern;
            this.confidence = confidence;
            this.symbol = symbol;
            this.timestamp = timestamp;
        }
        
        public String getPattern() { return pattern; }
        public double getConfidence() { return confidence; }
        public String getSymbol() { return symbol; }
        public String getTimestamp() { return timestamp; }
    }
    
    public static class TradeEvent {
        private final String symbol;
        private final double price;
        private final long volume;
        private final String side;
        private final long timestamp;
        
        public TradeEvent(String symbol, double price, long volume, String side, long timestamp) {
            this.symbol = symbol;
            this.price = price;
            this.volume = volume;
            this.side = side;
            this.timestamp = timestamp;
        }
        
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public long getVolume() { return volume; }
        public String getSide() { return side; }
        public long getTimestamp() { return timestamp; }
    }
    
    public static class SystemMetrics {
        private final double cpuUsage;
        private final double memoryUsage;
        private final long uptime;
        
        public SystemMetrics(double cpuUsage, double memoryUsage, long uptime) {
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
            this.uptime = uptime;
        }
        
        public double getCpuUsage() { return cpuUsage; }
        public double getMemoryUsage() { return memoryUsage; }
        public long getUptime() { return uptime; }
    }
    
    public static class BookmapWindow {
        private final String windowId;
        private final String symbol;
        private final String status;
        private final String timeframe;
        private final long createdTime;
        private volatile double currentPrice = 0.0;
        private volatile double currentVolume = 0.0;
        private volatile double priceChange = 0.0;
        private volatile long lastUpdate;
        
        public BookmapWindow(String windowId, String symbol, String status, String timeframe, long createdTime) {
            this.windowId = windowId;
            this.symbol = symbol;
            this.status = status;
            this.timeframe = timeframe;
            this.createdTime = createdTime;
            this.lastUpdate = createdTime;
        }
        
        public void updateData(double price, double volume, double change) {
            this.currentPrice = price;
            this.currentVolume = volume;
            this.priceChange = change;
            this.lastUpdate = System.currentTimeMillis();
        }
        
        public String getWindowId() { return windowId; }
        public String getSymbol() { return symbol; }
        public String getStatus() { return status; }
        public String getTimeframe() { return timeframe; }
        public double getCurrentPrice() { return currentPrice; }
        public double getCurrentVolume() { return currentVolume; }
        public double getPriceChange() { return priceChange; }
        public long getLastUpdate() { return lastUpdate; }
    }

    /**
     * Listener for Bookmap window lifecycle events.
     */
    public interface BookmapWindowListener {
        void onWindowOpened(BookmapWindow window);
        void onWindowClosed(String windowId, String symbol);
    }
    
    /**
     * Trading Session data class - represents an active Bookmap chart session
     */
    public static class TradingSession {
        private final String sessionId;
        private final String symbol;
        private final String marketType;
        private final double currentPrice;
        private final String priceChange;
        private final String volume;
        private final String volumeLevel;
        private final long lastUpdate;
        
        public TradingSession(String sessionId, String symbol, String marketType, 
                            double currentPrice, String priceChange, String volume, 
                            String volumeLevel, long lastUpdate) {
            this.sessionId = sessionId;
            this.symbol = symbol;
            this.marketType = marketType;
            this.currentPrice = currentPrice;
            this.priceChange = priceChange;
            this.volume = volume;
            this.volumeLevel = volumeLevel;
            this.lastUpdate = lastUpdate;
        }
        
        public String getSessionId() { return sessionId; }
        public String getSymbol() { return symbol; }
        public String getMarketType() { return marketType; }
        public double getCurrentPrice() { return currentPrice; }
        public String getPriceChange() { return priceChange; }
        public String getVolume() { return volume; }
        public String getVolumeLevel() { return volumeLevel; }
        public long getLastUpdate() { return lastUpdate; }
    }
    
    /**
     * Pattern Detection data class - represents a detected pattern in a session
     */
    public static class PatternDetection {
        private final String type;
        private final double confidence;
        private final String description;
        private final long timestamp;
        
        public PatternDetection(String type, double confidence, String description, long timestamp) {
            this.type = type;
            this.confidence = confidence;
            this.description = description;
            this.timestamp = timestamp;
        }
        
        public String getType() { return type; }
        public double getConfidence() { return confidence; }
        public String getDescription() { return description; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * AI Prediction data class - represents an AI-generated prediction for a session
     */
    public static class AIPrediction {
        private final String direction;
        private final double confidence;
        private final String target;
        private final String timeframe;
        private final long timestamp;
        
        public AIPrediction(String direction, double confidence, String target, 
                          String timeframe, long timestamp) {
            this.direction = direction;
            this.confidence = confidence;
            this.target = target;
            this.timeframe = timeframe;
            this.timestamp = timestamp;
        }
        
        public String getDirection() { return direction; }
        public double getConfidence() { return confidence; }
        public String getTarget() { return target; }
        public String getTimeframe() { return timeframe; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Session Analytics data class - represents analytical metrics for a session
     */
    public static class SessionAnalytics {
        private final String sessionId;
        private final String orderFlow;
        private final String marketPressure;
        private final String volatility;
        private final double sessionScore;
        
        public SessionAnalytics(String sessionId, String orderFlow, String marketPressure, 
                              String volatility, double sessionScore) {
            this.sessionId = sessionId;
            this.orderFlow = orderFlow;
            this.marketPressure = marketPressure;
            this.volatility = volatility;
            this.sessionScore = sessionScore;
        }
        
        public String getSessionId() { return sessionId; }
        public String getOrderFlow() { return orderFlow; }
        public String getMarketPressure() { return marketPressure; }
        public String getVolatility() { return volatility; }
        public double getSessionScore() { return sessionScore; }
    }
    
    // ==================== ENHANCED TRACKING CLASSES ====================
    
    /**
     * Comprehensive instrument details
     */
    public static class InstrumentDetails {
        private final String alias;
        private final String symbol;
        private final String exchange;
        private final double pips;
        private final long registrationTime;
        
        public InstrumentDetails(String alias, String symbol, String exchange, double pips, long registrationTime) {
            this.alias = alias;
            this.symbol = symbol;
            this.exchange = exchange;
            this.pips = pips;
            this.registrationTime = registrationTime;
        }
        
        public String getAlias() { return alias; }
        public String getSymbol() { return symbol; }
        public String getExchange() { return exchange; }
        public double getPips() { return pips; }
        public long getRegistrationTime() { return registrationTime; }
    }
    
    /**
     * Health status tracking for instruments
     */
    public static class InstrumentHealthStatus {
        private final String alias;
        private boolean isHealthy;
        private long lastHealthCheck;
        private int dataPointsCount;
        private double lastPrice;
        private int lastSize;
        
        public InstrumentHealthStatus(String alias, boolean isHealthy, long lastHealthCheck) {
            this.alias = alias;
            this.isHealthy = isHealthy;
            this.lastHealthCheck = lastHealthCheck;
            this.dataPointsCount = 0;
        }
        
        public void updateHealth(boolean healthy, long timestamp) {
            this.isHealthy = healthy;
            this.lastHealthCheck = timestamp;
        }
        
        public void recordDataPoint(double price, int size) {
            this.lastPrice = price;
            this.lastSize = size;
            this.dataPointsCount++;
        }
        
        public String getAlias() { return alias; }
        public boolean isHealthy() { return isHealthy; }
        public long getLastHealthCheck() { return lastHealthCheck; }
        public int getDataPointsCount() { return dataPointsCount; }
        public double getLastPrice() { return lastPrice; }
        public int getLastSize() { return lastSize; }
    }
}