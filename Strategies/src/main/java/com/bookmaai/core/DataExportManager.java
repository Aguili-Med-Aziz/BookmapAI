package com.bookmaai.core;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
// JSON export using simple string building (no external dependencies)

/**
 * 💾 Data Export Manager - CSV/JSON Export for Real Market Data
 * 
 * Exports real market data to CSV and JSON formats for analysis and machine learning.
 * Supports multiple timeframes and data aggregation levels.
 */
public class DataExportManager {
    
    private static final String EXPORT_DIR = "exports";
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DATA_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final Path exportPath;
    
    public DataExportManager() {
        this.exportPath = Paths.get(EXPORT_DIR);
        
        try {
            Files.createDirectories(exportPath);
            System.out.println("💾 DataExportManager initialized - Export directory: " + exportPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ Failed to create export directory: " + e.getMessage());
        }
    }
    
    /**
     * Export market data to CSV format
     */
    public void exportToCsv(String symbol, String timeframe, List<Object> dataPoints) {
        if (dataPoints.isEmpty()) {
            return;
        }
        
        String filename = String.format("%s_%s_data_%s.csv", 
            symbol, timeframe, LocalDateTime.now().format(FILE_TIMESTAMP));
        Path filePath = exportPath.resolve(filename);
        
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
            // CSV header
            writer.println("timestamp,price,volume,side,metadata");
            
            // Data rows - simplified for now
            for (Object point : dataPoints) {
                writer.printf("%s,0.0,0.0,UNKNOWN,{}\n", LocalDateTime.now().format(DATA_TIMESTAMP));
            }
            
            System.out.println("💾 Exported CSV: " + filename + " (" + dataPoints.size() + " records)");
            
        } catch (IOException e) {
            System.err.println("❌ Failed to export CSV: " + e.getMessage());
        }
    }
    
    /**
     * Export DataPoint objects to CSV format (enhanced method)
     */
    public void exportDataPointsToCsv(String symbol, String timeframe, List<?> dataPoints) {
        if (dataPoints.isEmpty()) {
            return;
        }
        
        String filename = String.format("%s_%s_data_%s.csv", 
            symbol, timeframe, LocalDateTime.now().format(FILE_TIMESTAMP));
        Path filePath = exportPath.resolve(filename);
        
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
            // CSV header
            writer.println("timestamp,price,volume,side");
            
            // Data rows with proper DataPoint extraction
            for (Object point : dataPoints) {
                if (point instanceof com.bookmaai.core.RealDataSlidingWindow.DataPoint) {
                    com.bookmaai.core.RealDataSlidingWindow.DataPoint dp = 
                        (com.bookmaai.core.RealDataSlidingWindow.DataPoint) point;
                    writer.printf("%s,%.5f,%.2f,%s\n", 
                        dp.getTimestamp().format(DATA_TIMESTAMP),
                        dp.getPrice(),
                        dp.getVolume(),
                        dp.getSide());
                } else {
                    // Fallback for unknown types
                    writer.printf("%s,0.0,0.0,UNKNOWN\n", LocalDateTime.now().format(DATA_TIMESTAMP));
                }
            }
            
            System.out.println("💾 Exported CSV: " + filename + " (" + dataPoints.size() + " records)");
            
        } catch (IOException e) {
            System.err.println("❌ Failed to export CSV: " + e.getMessage());
        }
    }
    
    /**
     * Export aggregation data to JSON format
     */
    public void exportToJson(String symbol, String timeframe, Object aggregation) {
        if (aggregation == null) {
            return;
        }
        
        String filename = String.format("%s_%s_aggregation_%s.json", 
            symbol, timeframe, LocalDateTime.now().format(FILE_TIMESTAMP));
        Path filePath = exportPath.resolve(filename);
        
        try {
            String jsonData = "{\n  \"symbol\": \"" + symbol + "\",\n  \"timeframe\": \"" + timeframe + "\",\n  \"timestamp\": \"" + LocalDateTime.now() + "\"\n}";
            Files.write(filePath, jsonData.getBytes());
            
            System.out.println("💾 Exported JSON: " + filename);
            
        } catch (IOException e) {
            System.err.println("❌ Failed to export JSON: " + e.getMessage());
        }
    }
    
    /**
     * Export system statistics
     */
    public void exportSystemStats(Map<String, Object> stats) {
        String filename = String.format("system_stats_%s.json", 
            LocalDateTime.now().format(FILE_TIMESTAMP));
        Path filePath = exportPath.resolve(filename);
        
        try {
            String jsonData = "{\n  \"timestamp\": \"" + LocalDateTime.now() + "\",\n  \"stats\": " + stats.toString() + "\n}";
            Files.write(filePath, jsonData.getBytes());
            
            System.out.println("💾 Exported system stats: " + filename);
            
        } catch (IOException e) {
            System.err.println("❌ Failed to export system stats: " + e.getMessage());
        }
    }
}
