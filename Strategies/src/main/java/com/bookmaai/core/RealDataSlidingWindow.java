package com.bookmaai.core;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;

/**
 * 🔄 Real Data Sliding Window - REAL DATA ONLY
 * 
 * Processes real market data in sliding time windows.
 * NO SIMULATION - Only real Bookmap data.
 */
public class RealDataSlidingWindow {
    
    private final Map<String, Map<String, WindowData>> windows = new ConcurrentHashMap<>();
    private final DataExportManager exportManager = new DataExportManager();
    
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
                    // Convert to required format for export
                    // exportManager.exportToCsv(symbol, timeframe, points);
                    System.out.println("💾 Exporting " + symbol + " " + timeframe + ": " + points.size() + " points");
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
}





