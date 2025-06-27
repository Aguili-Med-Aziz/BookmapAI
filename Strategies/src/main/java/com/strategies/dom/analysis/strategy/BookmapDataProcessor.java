package com.strategies.dom.analysis.strategy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class BookmapDataProcessor {
    private static final String CSV_DIRECTORY = "bookmap_data";
    private static final ConcurrentMap<String, FileWriter> marketFiles = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, String> marketNames = new ConcurrentHashMap<>();
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("symbol=([^,\\s]+)");
    
    static {
        // Create directory if it doesn't exist
        File directory = new File(CSV_DIRECTORY);
        System.out.println("[BookmapDataProcessor] Attempting to create/verify CSV directory at: " + directory.getAbsolutePath());
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("[BookmapDataProcessor] CSV directory created successfully: " + directory.getAbsolutePath());
            } else {
                System.err.println("[BookmapDataProcessor] Failed to create CSV directory: " + directory.getAbsolutePath() + ". Check permissions and path validity.");
            }
        } else {
            System.out.println("[BookmapDataProcessor] CSV directory already exists: " + directory.getAbsolutePath());
        }
    }

    public static void processBookmapData(String data) {
        try {
            if (data == null || data.trim().isEmpty()) {
                System.out.println("[BookmapDataProcessor] Warning: Received null or empty data");
                return;
            }
            
            // Extract market ID from data
            String marketId = extractMarketId(data);
            String marketName = extractMarketName(data);
            
            System.out.println("[BookmapDataProcessor] Processing data for market: " + marketId + " (" + marketName + ")");
            
            // Get or create file writer for this market
            FileWriter writer = getOrCreateMarketWriter(marketId, marketName);
            
            if (writer != null) {
                // Write data with timestamp
                long timestamp = System.currentTimeMillis();
                String formattedData = String.format("%d,%s,%s%n", timestamp, marketId, data);
                writer.write(formattedData);
                writer.flush();
                
                System.out.println("[BookmapDataProcessor] Successfully wrote data for market: " + marketId);
            } else {
                System.err.println("[BookmapDataProcessor] Failed to get writer for market: " + marketId);
            }
            
        } catch (Exception e) {
            System.err.println("[BookmapDataProcessor] Error processing data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String extractMarketId(String data) {
        try {
            // Try to extract symbol from data
            Matcher matcher = SYMBOL_PATTERN.matcher(data);
            if (matcher.find()) {
                String symbol = matcher.group(1);
                if (symbol != null && !symbol.trim().isEmpty()) {
                    // Clean the symbol
                    symbol = symbol.replaceAll("[^a-zA-Z0-9_-]", "").toLowerCase();
                    if (!symbol.isEmpty()) {
                        return symbol;
                    }
                }
            }
            
            // Try alternative extraction methods
            if (data.contains("name=")) {
                String[] parts = data.split("name=");
                if (parts.length > 1) {
                    String name = parts[1].split(",")[0].trim();
                    if (!name.isEmpty()) {
                        return name.replaceAll("[^a-zA-Z0-9_-]", "").toLowerCase();
                    }
                }
            }
            
            // If no symbol found, create a unique ID based on data hash
            String hashId = "market_" + Math.abs(data.hashCode()) % 10000;
            System.out.println("[BookmapDataProcessor] No symbol found, using hash-based ID: " + hashId);
            return hashId;
            
        } catch (Exception e) {
            System.err.println("[BookmapDataProcessor] Error extracting market ID: " + e.getMessage());
            return "unknown_market_" + System.currentTimeMillis() % 10000;
        }
    }
    
    private static String extractMarketName(String data) {
        try {
            // Try to extract a human-readable name
            Matcher matcher = SYMBOL_PATTERN.matcher(data);
            if (matcher.find()) {
                return matcher.group(1);
            }
            
            if (data.contains("name=")) {
                String[] parts = data.split("name=");
                if (parts.length > 1) {
                    return parts[1].split(",")[0].trim();
                }
            }
            
            return "Unknown Market";
        } catch (Exception e) {
            return "Unknown Market";
        }
    }
    
    private static FileWriter getOrCreateMarketWriter(String marketId, String marketName) {
        try {
            // Check if we already have a writer for this market
            FileWriter existingWriter = marketFiles.get(marketId);
            if (existingWriter != null) {
                return existingWriter;
            }
            
            // Create new file for this market
            File marketFile = new File(CSV_DIRECTORY, "market_" + marketId + ".csv");
            System.out.println("[BookmapDataProcessor] Creating new CSV file: " + marketFile.getAbsolutePath());
            
            FileWriter newWriter = new FileWriter(marketFile, true); // Append mode
            
            // Write header if file is new
            if (marketFile.length() == 0) {
                String header = "timestamp,market_id,event_type,symbol,price,volume,side,additional_data\n";
                newWriter.write(header);
                newWriter.flush();
                System.out.println("[BookmapDataProcessor] Header written for new market file: " + marketId);
            }
            
            // Store the writer and market info
            marketFiles.put(marketId, newWriter);
            marketNames.put(marketId, marketName);
            
            System.out.println("[BookmapDataProcessor] Successfully created writer for market: " + marketId + " (" + marketName + ")");
            return newWriter;
            
        } catch (IOException e) {
            System.err.println("[BookmapDataProcessor] Failed to create file writer for market " + marketId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public static void closeAllWriters() {
        System.out.println("[BookmapDataProcessor] Closing all market file writers...");
        
        for (String marketId : marketFiles.keySet()) {
            try {
                FileWriter writer = marketFiles.get(marketId);
                if (writer != null) {
                    writer.flush();
                    writer.close();
                    System.out.println("[BookmapDataProcessor] Closed writer for market: " + marketId);
                }
            } catch (IOException e) {
                System.err.println("[BookmapDataProcessor] Error closing writer for market " + marketId + ": " + e.getMessage());
            }
        }
        
        marketFiles.clear();
        marketNames.clear();
        System.out.println("[BookmapDataProcessor] All market writers closed and cleared.");
    }
    
    public static void flushAllWriters() {
        for (String marketId : marketFiles.keySet()) {
            try {
                FileWriter writer = marketFiles.get(marketId);
                if (writer != null) {
                    writer.flush();
                }
            } catch (IOException e) {
                System.err.println("[BookmapDataProcessor] Error flushing writer for market " + marketId + ": " + e.getMessage());
            }
        }
    }
    
    public static int getActiveMarketCount() {
        return marketFiles.size();
    }
    
    public static String[] getActiveMarkets() {
        return marketFiles.keySet().toArray(new String[0]);
    }
    
    public static String getMarketName(String marketId) {
        return marketNames.getOrDefault(marketId, "Unknown Market");
    }
} 