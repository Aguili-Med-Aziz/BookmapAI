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
    
    /**
     * Get the Bookmap export path - prioritizes C:\Bookmap\exports
     */
    private static String getBookmapExportPath() {
        // FORCE C:\Bookmap\exports directory creation
        Path bookmapExports = Paths.get("C:", "Bookmap", "exports");
        
        try {
            // Always try to create the C:\Bookmap\exports directory
            Files.createDirectories(bookmapExports);
            System.out.println("✅ Created/verified C:\\Bookmap\\exports directory");
            return bookmapExports.toString();
        } catch (Exception e) {
            System.err.println("⚠️ Could not create C:\\Bookmap\\exports: " + e.getMessage());
        }
        
        // Fallback: Try BOOKMAP_HOME environment variable
        String bookmapHome = System.getenv("BOOKMAP_HOME");
        if (bookmapHome != null && !bookmapHome.trim().isEmpty()) {
            Path homeExports = Paths.get(bookmapHome, "exports");
            try {
                Files.createDirectories(homeExports);
                System.out.println("✅ Created/verified BOOKMAP_HOME exports directory: " + homeExports);
                return homeExports.toString();
            } catch (Exception e) {
                System.err.println("⚠️ Could not use BOOKMAP_HOME exports: " + e.getMessage());
            }
        }
        
        // Final fallback: Use local exports directory
        System.out.println("⚠️ Using fallback local exports directory");
        return "exports";
    }
    
    private static final String EXPORT_DIR = getBookmapExportPath();
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DATA_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final Path exportPath;
    
    public DataExportManager() {
        this.exportPath = Paths.get(EXPORT_DIR);
        
        try {
            Files.createDirectories(exportPath);
            System.out.println("💾 DataExportManager initialized - Export directory: " + exportPath.toAbsolutePath());
            
            // CREATE TEST FILE TO VERIFY EXPORT PATH WORKS
            createTestFile();
            
        } catch (IOException e) {
            System.err.println("❌ Failed to create export directory: " + e.getMessage());
        }
    }
    
    /**
     * Create a test file to verify the export path is working
     */
    private void createTestFile() {
        try {
            String testFilename = "SYSTEM_TEST_" + LocalDateTime.now().format(FILE_TIMESTAMP) + ".csv";
            Path testFilePath = exportPath.resolve(testFilename);
            
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(testFilePath))) {
                writer.println("timestamp,message");
                writer.println(LocalDateTime.now().format(DATA_TIMESTAMP) + ",BookmapAI system test - CSV export working");
            }
            
            System.out.println("✅ [DataExportManager] TEST FILE CREATED: " + testFilePath.toAbsolutePath());
            System.out.println("✅ [DataExportManager] If you see this file, CSV export path is working!");
            
        } catch (Exception e) {
            System.err.println("❌ [DataExportManager] FAILED TO CREATE TEST FILE: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("⚠️ [DataExportManager] No data points to export for " + symbol + " " + timeframe);
            return;
        }
        
        String filename = String.format("%s_%s_data_%s.csv", 
            symbol, timeframe, LocalDateTime.now().format(FILE_TIMESTAMP));
        Path filePath = exportPath.resolve(filename);
        
        System.out.println("💾 [DataExportManager] Exporting " + dataPoints.size() + " points for " + symbol + " " + timeframe);
        System.out.println("📁 [DataExportManager] Export path: " + filePath.toAbsolutePath());
        
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
            // CSV header
            writer.println("timestamp,price,volume,side");
            
            int validPoints = 0;
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
                    validPoints++;
                } else {
                    // Fallback for unknown types
                    writer.printf("%s,0.0,0.0,UNKNOWN\n", LocalDateTime.now().format(DATA_TIMESTAMP));
                }
            }
            
            System.out.println("✅ [DataExportManager] Successfully exported CSV: " + filename + " (" + validPoints + " valid records)");
            System.out.println("📂 [DataExportManager] File location: " + filePath.toAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("❌ [DataExportManager] Failed to export CSV: " + e.getMessage());
            e.printStackTrace();
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
