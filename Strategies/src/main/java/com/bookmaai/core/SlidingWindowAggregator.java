package com.bookmaai.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * 📈 Sliding Window Aggregator - مجمع البيانات المتحرك
 */
public class SlidingWindowAggregator {
    
    public static class WindowData {
        private final long timestamp;
        private final String symbol;
        private final double price;
        private final double volume;
        private final Map<String, Double> indicators;
        
        public WindowData(String symbol, double price, double volume, Map<String, Double> indicators) {
            this.timestamp = System.currentTimeMillis();
            this.symbol = symbol;
            this.price = price;
            this.volume = volume;
            this.indicators = new HashMap<>(indicators);
        }
        
        // Getters
        public long getTimestamp() { return timestamp; }
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public double getVolume() { return volume; }
        public Map<String, Double> getIndicators() { return new HashMap<>(indicators); }
    }
    
    public static class WindowSummary {
        private final long windowStart;
        private final long windowEnd;
        private final String symbol;
        private final List<WindowData> data;
        private final Map<String, Double> aggregatedIndicators;
        
        public WindowSummary(long windowStart, long windowEnd, String symbol, List<WindowData> data) {
            this.windowStart = windowStart;
            this.windowEnd = windowEnd;
            this.symbol = symbol;
            this.data = new ArrayList<>(data);
            this.aggregatedIndicators = calculateAggregatedIndicators();
        }
        
        private Map<String, Double> calculateAggregatedIndicators() {
            Map<String, Double> aggregated = new HashMap<>();
            
            if (data.isEmpty()) return aggregated;
            
            // Calculate OHLC
            double open = data.get(0).getPrice();
            double high = data.stream().mapToDouble(WindowData::getPrice).max().orElse(0.0);
            double low = data.stream().mapToDouble(WindowData::getPrice).min().orElse(0.0);
            double close = data.get(data.size() - 1).getPrice();
            
            aggregated.put("open", open);
            aggregated.put("high", high);
            aggregated.put("low", low);
            aggregated.put("close", close);
            
            // Calculate volume metrics
            double totalVolume = data.stream().mapToDouble(WindowData::getVolume).sum();
            double avgVolume = totalVolume / data.size();
            double maxVolume = data.stream().mapToDouble(WindowData::getVolume).max().orElse(0.0);
            
            aggregated.put("total_volume", totalVolume);
            aggregated.put("avg_volume", avgVolume);
            aggregated.put("max_volume", maxVolume);
            
            // Calculate VWAP
            double vwapSum = 0.0;
            double volumeSum = 0.0;
            for (WindowData wd : data) {
                vwapSum += wd.getPrice() * wd.getVolume();
                volumeSum += wd.getVolume();
            }
            double vwap = volumeSum > 0 ? vwapSum / volumeSum : close;
            aggregated.put("vwap", vwap);
            
            // Aggregate other indicators
            Set<String> indicatorKeys = new HashSet<>();
            for (WindowData wd : data) {
                indicatorKeys.addAll(wd.getIndicators().keySet());
            }
            
            for (String key : indicatorKeys) {
                double avg = data.stream()
                    .mapToDouble(wd -> wd.getIndicators().getOrDefault(key, 0.0))
                    .average()
                    .orElse(0.0);
                aggregated.put("avg_" + key, avg);
            }
            
            return aggregated;
        }
        
        // Getters
        public long getWindowStart() { return windowStart; }
        public long getWindowEnd() { return windowEnd; }
        public String getSymbol() { return symbol; }
        public List<WindowData> getData() { return new ArrayList<>(data); }
        public Map<String, Double> getAggregatedIndicators() { return new HashMap<>(aggregatedIndicators); }
        public int getDataCount() { return data.size(); }
        
        @Override
        public String toString() {
            return String.format("Window[%s]: %d samples, VWAP=%.2f, Volume=%.0f",
                    symbol, data.size(), 
                    aggregatedIndicators.getOrDefault("vwap", 0.0),
                    aggregatedIndicators.getOrDefault("total_volume", 0.0));
        }
    }
    
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final Map<String, Deque<WindowData>> symbolWindows = new ConcurrentHashMap<>();
    private final long windowSizeMs = 15 * 60 * 1000; // 15 minutes
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final AtomicLong totalDataPoints = new AtomicLong(0);
    private final AtomicLong windowsGenerated = new AtomicLong(0);
    
    // Window management
    private final Map<String, WindowSummary> latestWindows = new ConcurrentHashMap<>();
    private final Queue<WindowSummary> recentWindows = new ConcurrentLinkedQueue<>();
    private final int maxRecentWindows = 100;
    
    public SlidingWindowAggregator() {
        System.out.println("📈 [SlidingWindowAggregator] Initializing Sliding Window Aggregator...");
    }
    
    public void initialize() {
        isRunning.set(true);
        startWindowManagement();
        System.out.println("📈 [SlidingWindowAggregator] Aggregator initialized and running");
    }
    
    private void startWindowManagement() {
        // Window cleanup task
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupOldData();
            } catch (Exception e) {
                System.err.println("📈 [SlidingWindowAggregator] Error in cleanup: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.MINUTES);
        
        // Window generation task
        scheduler.scheduleAtFixedRate(() -> {
            try {
                generateWindows();
            } catch (Exception e) {
                System.err.println("📈 [SlidingWindowAggregator] Error generating windows: " + e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    public void addData(String symbol, double price, double volume, Map<String, Double> indicators) {
        if (!isRunning.get()) return;
        
        WindowData data = new WindowData(symbol, price, volume, indicators);
        
        symbolWindows.computeIfAbsent(symbol, k -> new ConcurrentLinkedDeque<>()).add(data);
        totalDataPoints.incrementAndGet();
        
        // Trigger immediate window generation for this symbol
        generateWindowForSymbol(symbol);
    }
    
    private void generateWindowForSymbol(String symbol) {
        Deque<WindowData> symbolData = symbolWindows.get(symbol);
        if (symbolData == null || symbolData.isEmpty()) return;
        
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - windowSizeMs;
        
        // Collect data within the window
        List<WindowData> windowData = new ArrayList<>();
        for (WindowData data : symbolData) {
            if (data.getTimestamp() >= windowStart && data.getTimestamp() <= currentTime) {
                windowData.add(data);
            }
        }
        
        if (!windowData.isEmpty()) {
            WindowSummary summary = new WindowSummary(windowStart, currentTime, symbol, windowData);
            latestWindows.put(symbol, summary);
            
            // Add to recent windows queue
            recentWindows.offer(summary);
            if (recentWindows.size() > maxRecentWindows) {
                recentWindows.poll();
            }
            
            windowsGenerated.incrementAndGet();
            
            System.out.println("📈 [SlidingWindowAggregator] Generated window: " + summary);
        }
    }
    
    private void generateWindows() {
        for (String symbol : symbolWindows.keySet()) {
            generateWindowForSymbol(symbol);
        }
    }
    
    private void cleanupOldData() {
        long cutoffTime = System.currentTimeMillis() - (2 * windowSizeMs); // Keep 30 minutes of data
        
        for (Deque<WindowData> symbolData : symbolWindows.values()) {
            symbolData.removeIf(data -> data.getTimestamp() < cutoffTime);
        }
        
        // Remove empty symbol windows
        symbolWindows.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    // Query methods
    public WindowSummary getLatestWindow(String symbol) {
        return latestWindows.get(symbol);
    }
    
    public List<WindowSummary> getRecentWindows() {
        return new ArrayList<>(recentWindows);
    }
    
    public List<WindowSummary> getRecentWindowsForSymbol(String symbol, int count) {
        return recentWindows.stream()
                .filter(w -> w.getSymbol().equals(symbol))
                .limit(count)
                .collect(ArrayList::new, (list, item) -> list.add(0, item), (list1, list2) -> {
                    list2.addAll(list1);
                });
    }
    
    public List<String> getActiveSymbols() {
        return new ArrayList<>(symbolWindows.keySet());
    }
    
    public boolean hasDataForSymbol(String symbol) {
        Deque<WindowData> data = symbolWindows.get(symbol);
        return data != null && !data.isEmpty();
    }
    
    public int getDataPointCount(String symbol) {
        Deque<WindowData> data = symbolWindows.get(symbol);
        return data != null ? data.size() : 0;
    }
    
    // Analysis methods
    public double calculateVWAP(String symbol) {
        WindowSummary window = getLatestWindow(symbol);
        return window != null ? window.getAggregatedIndicators().getOrDefault("vwap", 0.0) : 0.0;
    }
    
    public double calculateVolumeAverage(String symbol, int windowCount) {
        List<WindowSummary> windows = getRecentWindowsForSymbol(symbol, windowCount);
        return windows.stream()
                .mapToDouble(w -> w.getAggregatedIndicators().getOrDefault("total_volume", 0.0))
                .average()
                .orElse(0.0);
    }
    
    public Map<String, Double> getLatestIndicators(String symbol) {
        WindowSummary window = getLatestWindow(symbol);
        return window != null ? window.getAggregatedIndicators() : new HashMap<>();
    }
    
    // Statistics
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("is_running", isRunning.get());
        stats.put("total_data_points", totalDataPoints.get());
        stats.put("windows_generated", windowsGenerated.get());
        stats.put("active_symbols", symbolWindows.size());
        stats.put("recent_windows_count", recentWindows.size());
        stats.put("window_size_minutes", windowSizeMs / (60 * 1000));
        
        // Per-symbol stats
        Map<String, Integer> symbolDataCounts = new HashMap<>();
        for (Map.Entry<String, Deque<WindowData>> entry : symbolWindows.entrySet()) {
            symbolDataCounts.put(entry.getKey(), entry.getValue().size());
        }
        stats.put("symbol_data_counts", symbolDataCounts);
        
        return stats;
    }
    
    public String getSystemStatus() {
        StringBuilder status = new StringBuilder();
        status.append("📈 [SlidingWindowAggregator] === حالة مجمع النوافذ ===\n");
        status.append("الحالة: ").append(isRunning.get() ? "يعمل ✅" : "متوقف ❌").append("\n");
        status.append("الرموز النشطة: ").append(symbolWindows.size()).append("\n");
        status.append("نقاط البيانات الكلية: ").append(totalDataPoints.get()).append("\n");
        status.append("النوافذ المولدة: ").append(windowsGenerated.get()).append("\n");
        status.append("حجم النافذة: ").append(windowSizeMs / (60 * 1000)).append(" دقيقة\n");
        
        if (!symbolWindows.isEmpty()) {
            status.append("\nالرموز النشطة:\n");
            for (Map.Entry<String, Deque<WindowData>> entry : symbolWindows.entrySet()) {
                String symbol = entry.getKey();
                int dataCount = entry.getValue().size();
                WindowSummary latest = latestWindows.get(symbol);
                String latestInfo = latest != null ? 
                    String.format("VWAP=%.2f", latest.getAggregatedIndicators().getOrDefault("vwap", 0.0)) : 
                    "لا توجد نافذة";
                
                status.append(String.format("  • %s: %d نقطة بيانات (%s)\n", 
                        symbol, dataCount, latestInfo));
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
        
        System.out.println("📈 [SlidingWindowAggregator] Aggregator shutdown completed");
    }
} 