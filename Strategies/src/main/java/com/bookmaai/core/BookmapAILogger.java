package com.bookmaai.core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * BookmapAI Unified Logging System with Dashboard Monitor
 * 
 * Features:
 * - SINGLE unified log file for all components
 * - Dashboard data monitoring every 2 seconds
 * - Dual output (console + file)
 * - Timestamped entries
 * - Automatic directory creation
 * - File rotation based on size
 * - Thread-safe operations
 */
public class BookmapAILogger {
    
    // Configuration
    private static final String LOG_DIRECTORY = "C:\\Bookmap\\Logs";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String FILE_DATE_FORMAT = "yyyy-MM-dd_HHmm";
    
    // SINGLE unified logger instance
    private static BookmapAILogger unifiedLogger;
    private static final ReentrantLock initLock = new ReentrantLock();
    
    // Dashboard monitoring
    private static Thread dashboardMonitorThread;
    private static volatile boolean monitoringActive = false;
    private static volatile long lastDashboardDataUpdate = 0;
    private static volatile int dashboardCheckCount = 0;
    private static volatile long systemStartTime = System.currentTimeMillis(); // Track actual start time
    
    // Instance variables
    private String logFileName;
    private final DateTimeFormatter timestampFormatter;
    private final DateTimeFormatter fileFormatter;
    private PrintWriter fileWriter;
    private String currentLogFilePath;
    private final ReentrantLock writeLock = new ReentrantLock();
    
    // Static initialization
    static {
        try {
            initializeUnifiedLogSystem();
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize BookmapAI unified logging system: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private BookmapAILogger() {
        this.logFileName = "BookmapAI_Unified_Dashboard_Monitor_" + 
                          LocalDateTime.now().format(DateTimeFormatter.ofPattern(FILE_DATE_FORMAT)) + ".log";
        this.timestampFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        this.fileFormatter = DateTimeFormatter.ofPattern(FILE_DATE_FORMAT);
        
        initializeFileWriter();
        
        // Log the unified logger creation
        info("System", "Unified BookmapAI Logger initialized");
        info("System", "Single log file: " + currentLogFilePath);
        info("System", "Dashboard monitoring: READY");
    }
    
    /**
     * Get the unified logger instance
     */
    public static BookmapAILogger getLogger(String componentName) {
        return getUnifiedLogger();
    }
    
    /**
     * Get unified logger instance
     */
    public static synchronized BookmapAILogger getUnifiedLogger() {
        if (unifiedLogger == null) {
            unifiedLogger = new BookmapAILogger();
        }
        return unifiedLogger;
    }
    
    /**
     * Get main system logger (same as unified)
     */
    public static BookmapAILogger getSystemLogger() {
        return getUnifiedLogger();
    }
    
    /**
     * Initialize the unified log system with dashboard monitoring
     */
    private static void initializeUnifiedLogSystem() throws IOException {
        initLock.lock();
        try {
            // Create log directory if it doesn't exist
            Path logDir = Paths.get(LOG_DIRECTORY);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
                System.out.println("📁 Created BookmapAI log directory: " + LOG_DIRECTORY);
            }
            
            // Start dashboard monitoring
            startDashboardMonitoring();
            
        } finally {
            initLock.unlock();
        }
    }
    
    /**
     * Start dashboard monitoring thread (checks every 1 second)
     */
    private static void startDashboardMonitoring() {
        if (dashboardMonitorThread == null || !dashboardMonitorThread.isAlive()) {
            monitoringActive = true;
            dashboardMonitorThread = new Thread(() -> {
                BookmapAILogger logger = getUnifiedLogger();
                logger.info("Monitor", "=== Dashboard Monitoring Started ===");
                logger.info("Monitor", "Checking dashboard data updates every 1 second");
                logger.info("Monitor", "Target: Bookmap → Dashboard real-time data flow");
                logger.info("Monitor", "=======================================");
                
                while (monitoringActive) {
                    try {
                        Thread.sleep(1000); // Check every 1 second
                        checkDashboardDataUpdate();
                    } catch (InterruptedException e) {
                        logger.info("Monitor", "Dashboard monitoring stopped");
                        break;
                    } catch (Exception e) {
                        logger.error("Monitor", "Dashboard monitoring error", e);
                    }
                }
            });
            dashboardMonitorThread.setDaemon(true);
            dashboardMonitorThread.setName("BookmapAI-Dashboard-Monitor");
            dashboardMonitorThread.start();
        }
    }
    
    /**
     * Check dashboard data update status every 2 seconds
     */
    private static void checkDashboardDataUpdate() {
        dashboardCheckCount++;
        BookmapAILogger logger = getUnifiedLogger();
        
        try {
            // Get current data from RealTimeMarketDataStore
            RealTimeMarketDataStore dataStore = RealTimeMarketDataStore.getInstance();
            long currentTime = System.currentTimeMillis();
            
            // Check if dashboard is running
            boolean dashboardRunning = DashboardPortManager.isDashboardRunning();
            String dashboardStatus = dashboardRunning ? "RUNNING" : "NOT RUNNING";
            
            // Check data age
            String marketDataJson = dataStore.getMarketDataJson();
            boolean dataIsRecent = marketDataJson.contains("\"is_live\": true");
            
            // Calculate time since last update
            long timeSinceLastUpdate = currentTime - lastDashboardDataUpdate;
            boolean dataFlowActive = timeSinceLastUpdate < 5000; // Within 5 seconds
            
            // Log monitoring status
            String status = String.format(
                "Check #%d - Dashboard: %s | Data Flow: %s | Data Recent: %s | Port: %s",
                dashboardCheckCount,
                dashboardStatus,
                dataFlowActive ? "ACTIVE" : "INACTIVE", 
                dataIsRecent ? "YES" : "NO",
                DashboardPortManager.getActiveDashboardPort()
            );
            
            if (dashboardRunning && dataIsRecent && dataFlowActive) {
                logger.success("Monitor", "✅ " + status + " | Status: HEALTHY");
            } else {
                logger.warn("Monitor", "⚠️ " + status + " | Status: ISSUE DETECTED");
                
                // Detail the issues
                if (!dashboardRunning) {
                    logger.error("Monitor", "❌ Dashboard not running - Check port " + DashboardPortManager.getActiveDashboardPort());
                }
                if (!dataIsRecent) {
                    logger.error("Monitor", "❌ Data not recent - Bookmap data flow may be interrupted");
                }
                if (!dataFlowActive) {
                    logger.error("Monitor", "❌ Data flow inactive - Last update: " + (timeSinceLastUpdate/1000) + " seconds ago");
                }
            }
            
            // Log detailed data every 10 checks (10 seconds)
            if (dashboardCheckCount % 10 == 0) {
                logger.info("Monitor", "=== Detailed Dashboard Data Check ===");
                logger.info("Monitor", "Active Symbols: " + countActiveSymbols(marketDataJson));
                logger.info("Monitor", "Dashboard URL: " + DashboardPortManager.getDashboardURL());
                logger.info("Monitor", "System Uptime: " + getSystemUptime() + " seconds");
                logger.info("Monitor", "Total Checks: " + dashboardCheckCount);
                logger.info("Monitor", "Real-time Data Status: " + (dataIsRecent ? "LIVE" : "STALE"));
                logger.info("Monitor", "Bookmap Integration: " + (dataFlowActive ? "ACTIVE" : "INACTIVE"));
                logger.info("Monitor", "===================================");
            }
            
        } catch (Exception e) {
            logger.error("Monitor", "Dashboard monitoring check failed", e);
        }
    }
    
    /**
     * Count active symbols in market data
     */
    private static int countActiveSymbols(String marketDataJson) {
        try {
            return (int) marketDataJson.chars().filter(ch -> ch == '{').count() - 1; // Rough count
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Get system uptime in seconds
     */
    private static long getSystemUptime() {
        return (System.currentTimeMillis() - systemStartTime) / 1000; // Accurate uptime since system start
    }
    
    /**
     * Update last dashboard data timestamp (called from data updates)
     */
    public static void updateDashboardDataTimestamp() {
        lastDashboardDataUpdate = System.currentTimeMillis();
    }
    
    /**
     * Initialize the file writer for unified logging
     */
    private void initializeFileWriter() {
        writeLock.lock();
        try {
            currentLogFilePath = LOG_DIRECTORY + File.separator + logFileName;
            
            // Create file if it doesn't exist
            File logFile = new File(currentLogFilePath);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            
            // Initialize PrintWriter with auto-flush
            fileWriter = new PrintWriter(new FileWriter(logFile, true));
            
        } catch (IOException e) {
            System.err.println("❌ Failed to initialize unified file writer: " + e.getMessage());
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Log an INFO level message
     */
    public void info(String component, String message) {
        log("INFO", component, message, null);
    }
    
    /**
     * Log an INFO level message (backwards compatibility)
     */
    public void info(String message) {
        log("INFO", "System", message, null);
    }
    
    /**
     * Log a WARNING level message
     */
    public void warn(String component, String message) {
        log("WARN", component, message, null);
    }
    
    /**
     * Log a WARNING level message (backwards compatibility)
     */
    public void warn(String message) {
        log("WARN", "System", message, null);
    }
    
    /**
     * Log an ERROR level message
     */
    public void error(String component, String message) {
        log("ERROR", component, message, null);
    }
    
    /**
     * Log an ERROR level message (backwards compatibility)
     */
    public void error(String message) {
        log("ERROR", "System", message, null);
    }
    
    /**
     * Log an ERROR level message with exception
     */
    public void error(String component, String message, Throwable throwable) {
        log("ERROR", component, message, throwable);
    }
    
    /**
     * Log an ERROR level message with exception (backwards compatibility)
     */
    public void error(String message, Throwable throwable) {
        log("ERROR", "System", message, throwable);
    }
    
    /**
     * Log a DEBUG level message
     */
    public void debug(String component, String message) {
        log("DEBUG", component, message, null);
    }
    
    /**
     * Log a DEBUG level message (backwards compatibility)
     */
    public void debug(String message) {
        log("DEBUG", "System", message, null);
    }
    
    /**
     * Log a SUCCESS level message
     */
    public void success(String component, String message) {
        log("SUCCESS", component, message, null);
    }
    
    /**
     * Log a SUCCESS level message (backwards compatibility)
     */
    public void success(String message) {
        log("SUCCESS", "System", message, null);
    }
    
    /**
     * Core logging method for unified logging
     */
    private void log(String level, String component, String message, Throwable throwable) {
        writeLock.lock();
        try {
            String timestamp = LocalDateTime.now().format(timestampFormatter);
            String logEntry = String.format("[%s] [%s] [%s] %s", 
                                           timestamp, level, component, message);
            
            // Write to console with emoji for better visibility
            String consoleEntry = formatForConsole(level, component, message);
            System.out.println(consoleEntry);
            
            // Write to unified file
            if (fileWriter != null) {
                fileWriter.println(logEntry);
                
                // Include exception stack trace if provided
                if (throwable != null) {
                    fileWriter.println("Exception: " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
                    throwable.printStackTrace(fileWriter);
                }
                
                fileWriter.flush();
                
                // Update dashboard data timestamp for monitoring
                updateDashboardDataTimestamp();
                
                // Check file size and rotate if necessary
                checkAndRotateLog();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Unified logging error: " + e.getMessage());
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Format message for console display with emojis
     */
    private String formatForConsole(String level, String component, String message) {
        String emoji;
        switch (level) {
            case "INFO":    emoji = "📋"; break;
            case "SUCCESS": emoji = "✅"; break;
            case "WARN":    emoji = "⚠️"; break;
            case "ERROR":   emoji = "❌"; break;
            case "DEBUG":   emoji = "🔍"; break;
            default:        emoji = "📝"; break;
        }
        
        return String.format("%s [%s] %s", emoji, component, message);
    }
    
    /**
     * Check file size and rotate log if necessary
     */
    private void checkAndRotateLog() {
        try {
            File currentFile = new File(currentLogFilePath);
            if (currentFile.length() > MAX_FILE_SIZE) {
                rotateLogFile();
            }
        } catch (Exception e) {
            System.err.println("❌ Error checking unified log file size: " + e.getMessage());
        }
    }
    
    /**
     * Rotate the current log file
     */
    private void rotateLogFile() {
        try {
            // Close current writer
            if (fileWriter != null) {
                fileWriter.close();
            }
            
            // Create new log file name with timestamp
            String newFileName = "BookmapAI_Unified_Dashboard_Monitor_" + 
                               LocalDateTime.now().format(fileFormatter) + ".log";
            
            // Initialize new file writer
            this.logFileName = newFileName;
            initializeFileWriter();
            
            info("System", "Unified log file rotated. New file: " + newFileName);
            
        } catch (Exception e) {
            System.err.println("❌ Error rotating unified log file: " + e.getMessage());
        }
    }
    
    /**
     * Convenience methods for common logging patterns
     */
    public void systemStartup(String systemName) {
        info("System", "========================================");
        info("System", systemName + " Starting...");
        info("System", "========================================");
    }
    
    public void systemReady(String systemName) {
        success("System", "✅ " + systemName + " Ready!");
    }
    
    public void componentInitialized(String componentName) {
        success("System", "Component initialized: " + componentName);
    }
    
    public void marketData(String symbol, double price, double volume) {
        debug("MarketData", String.format("Market Data - %s: Price=%.5f, Volume=%.0f", symbol, price, volume));
        updateDashboardDataTimestamp(); // Update monitoring timestamp
    }
    
    public void patternDetected(String pattern, String symbol, double confidence) {
        info("Patterns", String.format("🎯 Pattern Detected: %s on %s (%.1f%% confidence)", pattern, symbol, confidence * 100));
    }
    
    public void telegramNotification(String message) {
        info("Telegram", "📱 Telegram: " + message);
    }
    
    public void dashboardAccess(String endpoint, String clientInfo) {
        info("Dashboard", String.format("🌐 Dashboard Access: %s from %s", endpoint, clientInfo));
        updateDashboardDataTimestamp(); // Update monitoring timestamp
    }
    
    public void performanceMetrics(String component, String metrics) {
        debug("Performance", String.format("📊 Performance [%s]: %s", component, metrics));
    }
    
    /**
     * Stop dashboard monitoring and close unified logger
     */
    public static void shutdown() {
        initLock.lock();
        try {
            // Stop dashboard monitoring
            monitoringActive = false;
            if (dashboardMonitorThread != null) {
                dashboardMonitorThread.interrupt();
            }
            
            BookmapAILogger logger = getUnifiedLogger();
            logger.info("System", "=== BookmapAI Unified Logging System Shutdown ===");
            logger.info("Monitor", "Dashboard monitoring stopped. Total checks: " + dashboardCheckCount);
            
            if (unifiedLogger != null) {
                unifiedLogger.close();
                unifiedLogger = null;
            }
            
        } finally {
            initLock.unlock();
        }
    }
    
    /**
     * Close unified logger's file writer
     */
    private void close() {
        writeLock.lock();
        try {
            if (fileWriter != null) {
                fileWriter.close();
                fileWriter = null;
            }
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Get current unified log file path
     */
    public String getLogFilePath() {
        return currentLogFilePath;
    }
    
    /**
     * Get component name (always "Unified" now)
     */
    public String getComponentName() {
        return "Unified";
    }
    
    /**
     * List active logger (only one now)
     */
    public static String[] getActiveLoggers() {
        return new String[]{"Unified"};
    }
    
    /**
     * Get unified log statistics with dashboard monitoring info
     */
    public static String getLogStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("📊 BookmapAI Unified Logging Statistics:\n");
        stats.append("   Logger Type: SINGLE UNIFIED LOG\n");
        stats.append("   Log Directory: ").append(LOG_DIRECTORY).append("\n");
        stats.append("   Max File Size: ").append(MAX_FILE_SIZE / 1024 / 1024).append(" MB\n");
        stats.append("   Dashboard Monitoring: ").append(monitoringActive ? "ACTIVE" : "INACTIVE").append("\n");
        stats.append("   Monitor Checks: ").append(dashboardCheckCount).append("\n");
        stats.append("   Check Interval: 1 second (Real-time)\n");
        stats.append("   Data Source: Bookmap Live Feed\n");
        
        if (unifiedLogger != null) {
            stats.append("   Log File: ").append(unifiedLogger.getLogFilePath()).append("\n");
        }
        
        return stats.toString();
    }
} 