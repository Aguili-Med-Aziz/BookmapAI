package com.bookmaai.core.enhanced;

import java.time.LocalDateTime;

/**
 * MarketData class for GPT4AnalysisEngine compatibility
 */
public class MarketData {
    private String symbol;
    private double price;
    private double volume;
    private long timestamp;
    
    public MarketData(String symbol, double price, double volume) {
        this.symbol = symbol;
        this.price = price;
        this.volume = volume;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public double getVolume() { return volume; }
    public long getTimestamp() { return timestamp; }
} 