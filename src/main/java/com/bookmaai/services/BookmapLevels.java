package com.bookmaai.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Container class for managing Bookmap levels (support/resistance levels)
 * extracted from various Bookmap tools
 */
public class BookmapLevels {
    private final double currentPrice;
    private final List<BookmapLevel> allLevels = new ArrayList<>();
    private final List<BookmapLevel> targetLevels = new ArrayList<>();
    private final List<BookmapLevel> supportLevels = new ArrayList<>();

    public BookmapLevels(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void addLevel(BookmapLevel level) {
        if (level != null) {
            allLevels.add(level);
        }
    }

    public void addLevel(double price, BookmapLevel level) {
        addLevel(level);
    }

    public void addTargetLevel(BookmapLevel level) {
        if (level != null) {
            targetLevels.add(level);
            allLevels.add(level);
        }
    }

    public void addSupportLevel(BookmapLevel level) {
        if (level != null) {
            supportLevels.add(level);
            allLevels.add(level);
        }
    }

    public void evaluateAndRank(String direction) {
        // Sort all levels by strength and distance
        sortByStrengthAndDistance();
        
        // Re-categorize based on direction and current price
        targetLevels.clear();
        supportLevels.clear();
        
        for (BookmapLevel level : allLevels) {
            if (level.getPrice() > currentPrice) {
                targetLevels.add(level);
            } else {
                supportLevels.add(level);
            }
        }
    }

    public List<BookmapLevel> getAllLevels() { 
        return new ArrayList<>(allLevels); 
    }

    public List<BookmapLevel> getTargetLevels() {
        if (!targetLevels.isEmpty()) {
            return new ArrayList<>(targetLevels);
        }
        return allLevels.stream()
            .filter(BookmapLevel::isResistance)
            .collect(Collectors.toList());
    }

    public List<BookmapLevel> getSupportLevels() {
        if (!supportLevels.isEmpty()) {
            return new ArrayList<>(supportLevels);
        }
        return allLevels.stream()
            .filter(BookmapLevel::isSupport)
            .collect(Collectors.toList());
    }

    public void sortByStrengthAndDistance() {
        allLevels.sort((a, b) -> {
            // First by priority (higher is better)
            int priorityCompare = Integer.compare(b.getPriority(), a.getPriority());
            if (priorityCompare != 0) return priorityCompare;
            
            // Then by strength (higher is better)
            int strengthCompare = Double.compare(b.getStrength(), a.getStrength());
            if (strengthCompare != 0) return strengthCompare;
            
            // Finally by distance from current price (closer is better)
            return Double.compare(a.getDistanceFrom(currentPrice), b.getDistanceFrom(currentPrice));
        });
    }

    public double getCurrentPrice() { 
        return currentPrice; 
    }

    public int size() {
        return allLevels.size();
    }

    public boolean isEmpty() {
        return allLevels.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("BookmapLevels{currentPrice=%.5f, levels=%d}", 
            currentPrice, allLevels.size());
    }
} 