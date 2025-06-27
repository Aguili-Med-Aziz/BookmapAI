package com.bookmaai.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * 🗂️ Window History Manager - مدير تاريخ النوافذ
 */
public class WindowHistoryManager {
    
    public static class WindowHistoryEntry {
        private final SlidingWindowAggregator.WindowSummary window;
        private final long storageTimestamp;
        private final Map<String, Object> metadata;
        
        public WindowHistoryEntry(SlidingWindowAggregator.WindowSummary window) {
            this.window = window;
            this.storageTimestamp = System.currentTimeMillis();
            this.metadata = new HashMap<>();
        }
        
        public void addMetadata(String key, Object value) {
            metadata.put(key, value);
        }
        
        // Getters
        public SlidingWindowAggregator.WindowSummary getWindow() { return window; }
        public long getStorageTimestamp() { return storageTimestamp; }
        public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    }
    
    public static class HistoryAnalysis {
        private final String symbol;
        private final List<WindowHistoryEntry> entries;
        private final Map<String, Double> analytics;
        
        public HistoryAnalysis(String symbol, List<WindowHistoryEntry> entries) {
            this.symbol = symbol;
            this.entries = new ArrayList<>(entries);
            this.analytics = calculateAnalytics();
        }
        
        private Map<String, Double> calculateAnalytics() {
            Map<String, Double> analytics = new HashMap<>();
            
            if (entries.isEmpty()) return analytics;
            
            // Calculate volume trends
            double totalVolume = entries.stream()
                    .mapToDouble(e -> e.getWindow().getAggregatedIndicators().getOrDefault("total_volume", 0.0))
                    .sum();
            double avgVolume = totalVolume / entries.size();
            
            analytics.put("avg_volume", avgVolume);
            analytics.put("total_volume", totalVolume);
            
            // Calculate price volatility
            double[] prices = entries.stream()
                    .mapToDouble(e -> e.getWindow().getAggregatedIndicators().getOrDefault("close", 0.0))
                    .toArray();
            
            if (prices.length > 1) {
                double priceVariance = calculateVariance(prices);
                analytics.put("price_volatility", Math.sqrt(priceVariance));
            }
            
            // Calculate VWAP trends
            double[] vwaps = entries.stream()
                    .mapToDouble(e -> e.getWindow().getAggregatedIndicators().getOrDefault("vwap", 0.0))
                    .toArray();
            
            if (vwaps.length > 1) {
                double vwapTrend = calculateTrend(vwaps);
                analytics.put("vwap_trend", vwapTrend);
            }
            
            return analytics;
        }
        
        private double calculateVariance(double[] values) {
            double mean = Arrays.stream(values).average().orElse(0.0);
            return Arrays.stream(values)
                    .map(x -> Math.pow(x - mean, 2))
                    .average()
                    .orElse(0.0);
        }
        
        private double calculateTrend(double[] values) {
            if (values.length < 2) return 0.0;
            
            int n = values.length;
            double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
            
            for (int i = 0; i < n; i++) {
                sumX += i;
                sumY += values[i];
                sumXY += i * values[i];
                sumXX += i * i;
            }
            
            double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
            return slope;
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public List<WindowHistoryEntry> getEntries() { return new ArrayList<>(entries); }
        public Map<String, Double> getAnalytics() { return new HashMap<>(analytics); }
        public int getWindowCount() { return entries.size(); }
        
        @Override
        public String toString() {
            return String.format("History[%s]: %d windows, AvgVol=%.0f, Volatility=%.4f, Trend=%.6f",
                    symbol, entries.size(),
                    analytics.getOrDefault("avg_volume", 0.0),
                    analytics.getOrDefault("price_volatility", 0.0),
                    analytics.getOrDefault("vwap_trend", 0.0));
        }
    }
    
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final Map<String, Deque<WindowHistoryEntry>> symbolHistory = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicLong totalWindows = new AtomicLong(0);
    private final AtomicLong historiesGenerated = new AtomicLong(0);
    
    // Configuration
    private final int maxWindowsPerSymbol = 1000; // Keep last 1000 windows per symbol
    private final long cleanupIntervalMs = 60 * 60 * 1000; // 1 hour cleanup interval
    
    // Analysis cache
    private final Map<String, HistoryAnalysis> analysisCache = new ConcurrentHashMap<>();
    private final AtomicLong lastCacheUpdate = new AtomicLong(0);
    private final long cacheValidityMs = 5 * 60 * 1000; // 5 minutes cache validity
    
    public WindowHistoryManager() {
        System.out.println("🗂️ [WindowHistoryManager] Initializing Window History Manager...");
    }
    
    public void initialize() {
        isRunning.set(true);
        startHistoryManagement();
        System.out.println("🗂️ [WindowHistoryManager] Manager initialized and running");
    }
    
    private void startHistoryManagement() {
        // History cleanup task
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupOldHistory();
                updateAnalysisCache();
            } catch (Exception e) {
                System.err.println("🗂️ [WindowHistoryManager] Error in history management: " + e.getMessage());
            }
        }, cleanupIntervalMs, cleanupIntervalMs, TimeUnit.MILLISECONDS);
    }
    
    public void addWindow(SlidingWindowAggregator.WindowSummary window) {
        if (!isRunning.get()) return;
        
        String symbol = window.getSymbol();
        WindowHistoryEntry entry = new WindowHistoryEntry(window);
        
        // Add market context metadata
        addMarketContextMetadata(entry);
        
        Deque<WindowHistoryEntry> history = symbolHistory.computeIfAbsent(symbol, 
                k -> new ConcurrentLinkedDeque<>());
        
        history.addLast(entry);
        totalWindows.incrementAndGet();
        
        // Maintain size limit
        if (history.size() > maxWindowsPerSymbol) {
            history.removeFirst();
        }
        
        // Invalidate analysis cache for this symbol
        analysisCache.remove(symbol);
        
        System.out.println(String.format("🗂️ [WindowHistoryManager] Added window for %s (History size: %d)",
                symbol, history.size()));
    }
    
    private void addMarketContextMetadata(WindowHistoryEntry entry) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        
        // Add session information
        String session = getMarketSession(hour);
        entry.addMetadata("session", session);
        entry.addMetadata("hour", hour);
        entry.addMetadata("day_of_week", cal.get(Calendar.DAY_OF_WEEK));
        
        // Add volatility context
        SlidingWindowAggregator.WindowSummary window = entry.getWindow();
        Map<String, Double> indicators = window.getAggregatedIndicators();
        
        double high = indicators.getOrDefault("high", 0.0);
        double low = indicators.getOrDefault("low", 0.0);
        double close = indicators.getOrDefault("close", 0.0);
        
        if (close > 0) {
            double volatility = (high - low) / close;
            entry.addMetadata("volatility", volatility);
        }
    }
    
    private String getMarketSession(int hour) {
        if (hour >= 23 || hour < 8) return "Asian";
        else if (hour >= 8 && hour < 13) return "London";
        else if (hour >= 13 && hour < 17) return "London-NY";
        else if (hour >= 17 && hour < 22) return "NY";
        else return "After-Hours";
    }
    
    private void cleanupOldHistory() {
        long cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L); // 7 days
        
        for (Deque<WindowHistoryEntry> history : symbolHistory.values()) {
            history.removeIf(entry -> entry.getStorageTimestamp() < cutoffTime);
        }
        
        // Remove empty histories
        symbolHistory.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    private void updateAnalysisCache() {
        for (String symbol : symbolHistory.keySet()) {
            generateAnalysis(symbol);
        }
        
        lastCacheUpdate.set(System.currentTimeMillis());
    }
    
    // Query methods
    public List<WindowHistoryEntry> getHistory(String symbol, int maxWindows) {
        Deque<WindowHistoryEntry> history = symbolHistory.get(symbol);
        if (history == null) return Collections.emptyList();
        
        return history.stream()
                .skip(Math.max(0, history.size() - maxWindows))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    public List<WindowHistoryEntry> getHistoryByTimeRange(String symbol, long startTime, long endTime) {
        Deque<WindowHistoryEntry> history = symbolHistory.get(symbol);
        if (history == null) return Collections.emptyList();
        
        return history.stream()
                .filter(entry -> {
                    long windowTime = entry.getWindow().getWindowStart();
                    return windowTime >= startTime && windowTime <= endTime;
                })
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    public HistoryAnalysis getAnalysis(String symbol) {
        // Check cache validity
        if (isCacheValid() && analysisCache.containsKey(symbol)) {
            return analysisCache.get(symbol);
        }
        
        return generateAnalysis(symbol);
    }
    
    private HistoryAnalysis generateAnalysis(String symbol) {
        List<WindowHistoryEntry> history = getHistory(symbol, maxWindowsPerSymbol);
        HistoryAnalysis analysis = new HistoryAnalysis(symbol, history);
        
        analysisCache.put(symbol, analysis);
        historiesGenerated.incrementAndGet();
        
        return analysis;
    }
    
    private boolean isCacheValid() {
        return (System.currentTimeMillis() - lastCacheUpdate.get()) < cacheValidityMs;
    }
    
    // Analysis methods
    public Map<String, Double> getVolumeProfile(String symbol, int windowCount) {
        List<WindowHistoryEntry> history = getHistory(symbol, windowCount);
        Map<String, Double> profile = new HashMap<>();
        
        if (history.isEmpty()) return profile;
        
        double totalVolume = 0.0;
        double maxVolume = 0.0;
        double minVolume = Double.MAX_VALUE;
        
        for (WindowHistoryEntry entry : history) {
            double volume = entry.getWindow().getAggregatedIndicators().getOrDefault("total_volume", 0.0);
            totalVolume += volume;
            maxVolume = Math.max(maxVolume, volume);
            minVolume = Math.min(minVolume, volume);
        }
        
        profile.put("avg_volume", totalVolume / history.size());
        profile.put("max_volume", maxVolume);
        profile.put("min_volume", minVolume == Double.MAX_VALUE ? 0.0 : minVolume);
        profile.put("volume_volatility", (maxVolume - minVolume) / (totalVolume / history.size()));
        
        return profile;
    }
    
    public List<String> findSimilarPatterns(String symbol, SlidingWindowAggregator.WindowSummary currentWindow) {
        List<WindowHistoryEntry> history = getHistory(symbol, 100); // Check last 100 windows
        List<String> similarPatterns = new ArrayList<>();
        
        Map<String, Double> currentIndicators = currentWindow.getAggregatedIndicators();
        double currentVwap = currentIndicators.getOrDefault("vwap", 0.0);
        double currentVolume = currentIndicators.getOrDefault("total_volume", 0.0);
        
        for (WindowHistoryEntry entry : history) {
            Map<String, Double> historyIndicators = entry.getWindow().getAggregatedIndicators();
            double historyVwap = historyIndicators.getOrDefault("vwap", 0.0);
            double historyVolume = historyIndicators.getOrDefault("total_volume", 0.0);
            
            // Calculate similarity score
            double vwapSimilarity = 1.0 - Math.abs(currentVwap - historyVwap) / Math.max(currentVwap, historyVwap);
            double volumeSimilarity = 1.0 - Math.abs(currentVolume - historyVolume) / Math.max(currentVolume, historyVolume);
            
            double overallSimilarity = (vwapSimilarity + volumeSimilarity) / 2.0;
            
            if (overallSimilarity > 0.8) { // 80% similarity threshold
                similarPatterns.add(String.format("Window at %s (Similarity: %.2f)",
                        new Date(entry.getWindow().getWindowStart()), overallSimilarity));
            }
        }
        
        return similarPatterns;
    }
    
    // Statistics
    public List<String> getActiveSymbols() {
        return new ArrayList<>(symbolHistory.keySet());
    }
    
    public int getHistorySize(String symbol) {
        Deque<WindowHistoryEntry> history = symbolHistory.get(symbol);
        return history != null ? history.size() : 0;
    }
    
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("is_running", isRunning.get());
        stats.put("total_windows", totalWindows.get());
        stats.put("histories_generated", historiesGenerated.get());
        stats.put("active_symbols", symbolHistory.size());
        stats.put("max_windows_per_symbol", maxWindowsPerSymbol);
        stats.put("cache_valid", isCacheValid());
        stats.put("cached_analyses", analysisCache.size());
        
        // Per-symbol stats
        Map<String, Integer> symbolSizes = new HashMap<>();
        for (Map.Entry<String, Deque<WindowHistoryEntry>> entry : symbolHistory.entrySet()) {
            symbolSizes.put(entry.getKey(), entry.getValue().size());
        }
        stats.put("symbol_history_sizes", symbolSizes);
        
        return stats;
    }
    
    public String getSystemStatus() {
        StringBuilder status = new StringBuilder();
        status.append("🗂️ [WindowHistoryManager] === حالة مدير التاريخ ===\n");
        status.append("الحالة: ").append(isRunning.get() ? "يعمل ✅" : "متوقف ❌").append("\n");
        status.append("الرموز النشطة: ").append(symbolHistory.size()).append("\n");
        status.append("إجمالي النوافذ: ").append(totalWindows.get()).append("\n");
        status.append("التحليلات المولدة: ").append(historiesGenerated.get()).append("\n");
        status.append("التحليلات المخزنة: ").append(analysisCache.size()).append("\n");
        status.append("صحة التخزين المؤقت: ").append(isCacheValid() ? "صالح ✅" : "منتهي ❌").append("\n");
        
        if (!symbolHistory.isEmpty()) {
            status.append("\nتفاصيل الرموز:\n");
            for (Map.Entry<String, Deque<WindowHistoryEntry>> entry : symbolHistory.entrySet()) {
                String symbol = entry.getKey();
                int size = entry.getValue().size();
                HistoryAnalysis analysis = analysisCache.get(symbol);
                String analysisInfo = analysis != null ? 
                    String.format("متوسط الحجم=%.0f", analysis.getAnalytics().getOrDefault("avg_volume", 0.0)) :
                    "لا يوجد تحليل";
                
                status.append(String.format("  • %s: %d نافذة (%s)\n", symbol, size, analysisInfo));
            }
        }
        
        return status.toString();
    }
    
    public void shutdown() {
        isRunning.set(false);
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("🗂️ [WindowHistoryManager] Manager shutdown completed");
    }
} 