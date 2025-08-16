package com.bookmaai.core;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 🔄 Real Data Sliding Window - REAL DATA ONLY
 * 
 * Processes real market data in sliding time windows.
 * NO SIMULATION - Only real Bookmap data.
 */
public class RealDataSlidingWindow {
    
    private final Map<String, Map<String, WindowData>> windows = new ConcurrentHashMap<>();
    private final DataExportManager exportManager = new DataExportManager();
    private final ScheduledExecutorService exportScheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean autoExportEnabled = true;
    
    // Auto-export settings
    private static final int EXPORT_INTERVAL_MINUTES = 1; // Export every 1 minute
    private static final int MIN_DATA_POINTS_FOR_EXPORT = 10; // Minimum data points to trigger export
    
    public RealDataSlidingWindow() {
        // Start automatic periodic exports
        startAutoExport();
    }
    
    /**
     * Add real market data point
     */
    public void addRealData(String symbol, double price, double volume, String side) {
        LocalDateTime now = LocalDateTime.now();
        DataPoint point = new DataPoint(price, volume, side, now);
        
        Map<String, WindowData> symbolWindows = windows.computeIfAbsent(symbol, 
            k -> new ConcurrentHashMap<>());
        
        // Add to multiple timeframes
        addToWindow(symbolWindows, "1m", point, 60);
        addToWindow(symbolWindows, "5m", point, 300);
        addToWindow(symbolWindows, "15m", point, 900);
        addToWindow(symbolWindows, "1h", point, 3600);
        
        // Trigger export if enough data accumulated
        checkAndTriggerExport(symbol);
    }
    
    private void addToWindow(Map<String, WindowData> windows, String timeframe, DataPoint point, int windowSeconds) {
        WindowData window = windows.computeIfAbsent(timeframe, 
            k -> new WindowData(timeframe, windowSeconds));
        window.addPoint(point);
    }
    
    /**
     * Get aggregated data for symbol/timeframe
     */
    public WindowSummary getSummary(String symbol, String timeframe) {
        Map<String, WindowData> symbolWindows = windows.get(symbol);
        if (symbolWindows == null) return null;
        
        WindowData window = symbolWindows.get(timeframe);
        return window != null ? window.getSummary(symbol) : null;
    }
    
    /**
     * Export data to CSV/JSON
     */
    public void exportData() {
        for (String symbol : windows.keySet()) {
            for (String timeframe : windows.get(symbol).keySet()) {
                WindowData window = windows.get(symbol).get(timeframe);
                List<DataPoint> points = window.getPoints();
                
                if (!points.isEmpty()) {
                    // Export to CSV using the enhanced method
                    exportManager.exportDataPointsToCsv(symbol, timeframe, points);
                    System.out.println("💾 Exported " + symbol + " " + timeframe + ": " + points.size() + " points");
                }
            }
        }
    }
    
    /**
     * Export data for specific symbol and timeframe
     */
    public void exportSymbolData(String symbol, String timeframe) {
        Map<String, WindowData> symbolWindows = windows.get(symbol);
        if (symbolWindows != null) {
            WindowData window = symbolWindows.get(timeframe);
            if (window != null) {
                List<DataPoint> points = window.getPoints();
                if (!points.isEmpty()) {
                    exportManager.exportDataPointsToCsv(symbol, timeframe, points);
                    System.out.println("💾 Exported " + symbol + " " + timeframe + ": " + points.size() + " points");
                }
            }
        }
    }
    
    /**
     * Single data point
     */
    public static class DataPoint {
        private final double price;
        private final double volume;
        private final String side;
        private final LocalDateTime timestamp;
        
        public DataPoint(double price, double volume, String side, LocalDateTime timestamp) {
            this.price = price;
            this.volume = volume;
            this.side = side;
            this.timestamp = timestamp;
        }
        
        public double getPrice() { return price; }
        public double getVolume() { return volume; }
        public String getSide() { return side; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    /**
     * Window data storage
     */
    public static class WindowData {
        private final String timeframe;
        private final int windowSeconds;
        private final List<DataPoint> points = new CopyOnWriteArrayList<>();
        
        public WindowData(String timeframe, int windowSeconds) {
            this.timeframe = timeframe;
            this.windowSeconds = windowSeconds;
        }
        
        public void addPoint(DataPoint point) {
            points.add(point);
            cleanOldData();
        }
        
        private void cleanOldData() {
            LocalDateTime cutoff = LocalDateTime.now().minusSeconds(windowSeconds);
            points.removeIf(p -> p.getTimestamp().isBefore(cutoff));
        }
        
        public WindowSummary getSummary(String symbol) {
            if (points.isEmpty()) return null;
            
            double open = points.get(0).getPrice();
            double close = points.get(points.size() - 1).getPrice();
            double high = points.stream().mapToDouble(DataPoint::getPrice).max().orElse(0);
            double low = points.stream().mapToDouble(DataPoint::getPrice).min().orElse(0);
            double volume = points.stream().mapToDouble(DataPoint::getVolume).sum();
            
            return new WindowSummary(symbol, timeframe, open, high, low, close, volume, points.size());
        }
        
        public List<DataPoint> getPoints() { return new ArrayList<>(points); }
    }
    
    /**
     * Window summary (OHLCV)
     */
    public static class WindowSummary {
        private final String symbol;
        private final String timeframe;
        private final double open, high, low, close, volume;
        private final int count;
        
        public WindowSummary(String symbol, String timeframe, double open, double high, double low, double close, double volume, int count) {
            this.symbol = symbol;
            this.timeframe = timeframe;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.volume = volume;
            this.count = count;
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public String getTimeframe() { return timeframe; }
        public double getOpen() { return open; }
        public double getHigh() { return high; }
        public double getLow() { return low; }
        public double getClose() { return close; }
        public double getVolume() { return volume; }
        public int getCount() { return count; }
    }
    
    // ==================== AUTO-EXPORT FUNCTIONALITY ====================
    
    /**
     * Start automatic periodic exports
     */
    private void startAutoExport() {
        if (autoExportEnabled) {
            exportScheduler.scheduleAtFixedRate(this::exportData, 
                EXPORT_INTERVAL_MINUTES, EXPORT_INTERVAL_MINUTES, TimeUnit.MINUTES);
            System.out.println("💾 Auto-export scheduled every " + EXPORT_INTERVAL_MINUTES + " minutes");
        }
    }
    
    /**
     * Check if symbol has enough data to trigger export
     */
    private void checkAndTriggerExport(String symbol) {
        if (!autoExportEnabled) return;
        
        Map<String, WindowData> symbolWindows = windows.get(symbol);
        if (symbolWindows != null) {
            // Check 1-minute window for immediate exports
            WindowData oneMinWindow = symbolWindows.get("1m");
            if (oneMinWindow != null && oneMinWindow.getPoints().size() >= MIN_DATA_POINTS_FOR_EXPORT) {
                // Export 1m data immediately when enough points accumulated
                exportSymbolData(symbol, "1m");
            }
        }
    }
    
    /**
     * Enable or disable auto-export
     */
    public void setAutoExportEnabled(boolean enabled) {
        this.autoExportEnabled = enabled;
        if (enabled) {
            startAutoExport();
        }
    }
    
    /**
     * Shutdown export scheduler
     */
    public void shutdown() {
        exportScheduler.shutdown();
        try {
            if (!exportScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                exportScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            exportScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}





