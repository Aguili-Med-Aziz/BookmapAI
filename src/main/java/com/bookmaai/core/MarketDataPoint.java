package com.bookmaai.core;

import java.time.LocalDateTime;

/**
 * 📊 Market Data Point
 * 
 * يمثل نقطة بيانات واحدة من السوق تتضمن السعر والحجم والـ VWAP والوقت
 */
public class MarketDataPoint {
    
    private final double price;
    private final double volume;
    private final double vwap;
    private final LocalDateTime timestamp;
    
    public MarketDataPoint(double price, double volume, double vwap, LocalDateTime timestamp) {
        this.price = price;
        this.volume = volume;
        this.vwap = vwap;
        this.timestamp = timestamp;
    }
    
    public double getPrice() {
        return price;
    }
    
    public double getVolume() {
        return volume;
    }
    
    public double getVwap() {
        return vwap;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return String.format("MarketDataPoint{price=%.5f, volume=%.2f, vwap=%.5f, timestamp=%s}", 
                           price, volume, vwap, timestamp);
    }
} 