package com.bookmaai.core;

import com.bookmaai.web.SimpleDashboard;
import java.util.*;
import java.util.concurrent.*;
import java.awt.Desktop;
import java.net.URI;

/**
 * 🔌 Bookmap Addon Integration - تكامل إضافة Bookmap
 * 
 * Main integration class for BookmapAI as Bookmap addon:
 * - Auto-launches web dashboard on localhost
 * - Coordinates all system components
 * - Handles real-time data from Bookmap
 * - Manages accuracy tracking and reporting
 * - Opens browser to dashboard automatically
 */
public class BookmapAddonIntegration {
    
    // Core system components
    private MarketDataExtractor dataExtractor;
    private EnhancedSlidingWindowManager windowManager;
    private AccuracyDashboardManager dashboardManager;
    private SimpleDashboard webServer;
    private EnhancedBookmapAICore aiCore;
    
    // Configuration
    private static final int DASHBOARD_PORT = 8080;
    private static final boolean AUTO_OPEN_BROWSER = true;
    
    // State tracking
    private volatile boolean isInitialized = false;
    private volatile boolean isRunning = false;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    /**
     * Initialize the BookmapAI addon system
     */
    public void initialize() {
        if (isInitialized) {
            System.out.println("🔌 [BookmapAddon] System already initialized");
            return;
        }
        
        System.out.println("🔌 [BookmapAddon] Initializing BookmapAI Addon System...");
        System.out.println("🔌 Version: Enhanced Research Edition");
        System.out.println("🔌 Integration: Official Bookmap Addon");
        
        try {
            // Initialize core components in order
            initializeDataExtractor();
            initializeSlidingWindowManager();
            initializeDashboardManager();
            initializeAICore();
            initializeWebDashboard();
            
            // Start system monitoring
            startSystemMonitoring();
            
            // Auto-open browser to dashboard
            if (AUTO_OPEN_BROWSER) {
                openDashboardInBrowser();
            }
            
            isInitialized = true;
            isRunning = true;
            
            System.out.println("🔌 [BookmapAddon] ✅ System initialization completed successfully!");
            System.out.println("🔌 [BookmapAddon] 🌐 Web Dashboard: http://localhost:" + DASHBOARD_PORT);
            System.out.println("🔌 [BookmapAddon] 📊 Real-time accuracy monitoring active");
            
        } catch (Exception e) {
            System.err.println("❌ [BookmapAddon] Failed to initialize: " + e.getMessage());
            e.printStackTrace();
            shutdown();
        }
    }
    
    /**
     * Process market data from Bookmap
     */
    public void onMarketData(String symbol, double price, double volume, long timestamp, 
                           Map<String, Double> bookData) {
        if (!isRunning) return;
        
        try {
            // Extract and process market data
            MarketDataExtractor.TickData tick = dataExtractor.extractTickData(symbol, price, volume, timestamp, bookData);
            
            // Update sliding windows
            windowManager.addDataPoint(symbol, price, volume, timestamp, bookData);
            
            // Process with AI core
            double vwap = calculateVWAP(symbol);
            aiCore.processMarketData(symbol, price, volume, vwap, bookData);
            
            // Record accuracy metrics
            recordAccuracyMetrics(symbol, tick, bookData);
            
        } catch (Exception e) {
            System.err.println("Error processing market data: " + e.getMessage());
        }
    }
    
    /**
     * Process real-time trade data from Bookmap
     */
    public void processTradeData(double price, int size, Object tradeInfo) {
        if (!isRunning) return;
        
        try {
            // Convert trade info to market data format
            Map<String, Double> tradeData = new HashMap<>();
            tradeData.put("price", price);
            tradeData.put("volume", (double) size);
            tradeData.put("timestamp", (double) System.currentTimeMillis());
            
            // Process through existing market data pipeline
            String symbol = "ACTIVE"; // Default symbol for active instrument
            onMarketData(symbol, price, size, System.currentTimeMillis(), tradeData);
            
        } catch (Exception e) {
            System.err.println("Error processing trade data: " + e.getMessage());
        }
    }
    
    /**
     * Process real-time depth data from Bookmap
     */
    public void processDepthData(boolean isBid, int price, int size) {
        if (!isRunning) return;
        
        try {
            // Convert depth data to market data format
            Map<String, Double> depthData = new HashMap<>();
            depthData.put("price", (double) price);
            depthData.put("size", (double) size);
            depthData.put("is_bid", isBid ? 1.0 : 0.0);
            depthData.put("timestamp", (double) System.currentTimeMillis());
            
            // Process depth data through order book analysis
            if (aiCore != null) {
                String symbol = "ACTIVE"; // Default symbol for active instrument
                aiCore.processMarketData(symbol, price, size, price, depthData);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing depth data: " + e.getMessage());
        }
    }
    
    /**
     * Add new instrument to the system
     */
    public void addInstrument(String alias, Object instrumentInfo) {
        try {
            System.out.println("📈 Adding instrument: " + alias);
            
            // Initialize sliding windows for this instrument
            if (windowManager != null) {
                Map<String, Double> initialData = new HashMap<>();
                initialData.put("timestamp", (double) System.currentTimeMillis());
                windowManager.addDataPoint(alias, 0.0, 0.0, System.currentTimeMillis(), initialData);
            }
            
        } catch (Exception e) {
            System.err.println("Error adding instrument " + alias + ": " + e.getMessage());
        }
    }
    
    /**
     * Remove instrument from the system
     */
    public void removeInstrument(String alias) {
        try {
            System.out.println("📉 Removing instrument: " + alias);
            
            // Clean up any resources for this instrument
            // Additional cleanup can be added here as needed
            
        } catch (Exception e) {
            System.err.println("Error removing instrument " + alias + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle existing instrument subscription
     */
    public void handleExistingInstrument(String alias, String fullName, String feedName) {
        try {
            System.out.println("📊 Handling existing instrument: " + alias + " (" + fullName + ")");
            
            // Treat as new instrument if not already tracked
            addInstrument(alias, null);
            
        } catch (Exception e) {
            System.err.println("Error handling existing instrument " + alias + ": " + e.getMessage());
        }
    }
    
    /**
     * Get system status for Bookmap
     */
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("initialized", isInitialized);
        status.put("running", isRunning);
        status.put("dashboard_url", "http://localhost:" + DASHBOARD_PORT);
        status.put("components_active", countActiveComponents());
        
        if (dashboardManager != null) {
            status.put("system_overview", dashboardManager.getSystemOverview());
        }
        
        return status;
    }
    
    /**
     * Shutdown the addon system
     */
    public void shutdown() {
        System.out.println("🔌 [BookmapAddon] Shutting down system...");
        
        isRunning = false;
        
        // Shutdown components in reverse order
        if (webServer != null) {
            webServer.stop();
        }
        
        if (aiCore != null) {
            aiCore.shutdown();
        }
        
        if (dashboardManager != null) {
            dashboardManager.shutdown();
        }
        
        if (windowManager != null) {
            windowManager.shutdown();
        }
        
        if (dataExtractor != null) {
            dataExtractor.shutdown();
        }
        
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        
        System.out.println("🔌 [BookmapAddon] System shutdown completed");
    }
    
    private void initializeDataExtractor() {
        System.out.println("🔌 [BookmapAddon] Initializing Market Data Extractor...");
        dataExtractor = new MarketDataExtractor();
        dataExtractor.initialize();
    }
    
    private void initializeSlidingWindowManager() {
        System.out.println("🔌 [BookmapAddon] Initializing Enhanced Sliding Window Manager...");
        windowManager = new EnhancedSlidingWindowManager();
        windowManager.initialize();
    }
    
    private void initializeDashboardManager() {
        System.out.println("🔌 [BookmapAddon] Initializing Accuracy Dashboard Manager...");
        dashboardManager = new AccuracyDashboardManager();
        dashboardManager.initialize();
    }
    
    private void initializeAICore() {
        System.out.println("🔌 [BookmapAddon] Initializing Enhanced AI Core...");
        aiCore = new EnhancedBookmapAICore();
        aiCore.initialize();
    }
    
    private void initializeWebDashboard() {
        System.out.println("🔌 [BookmapAddon] Starting Web Dashboard Server...");
        webServer = new SimpleDashboard(DASHBOARD_PORT, dashboardManager);
        webServer.start();
        
        // Wait a moment for server to start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void openDashboardInBrowser() {
        String dashboardUrl = "http://localhost:" + DASHBOARD_PORT;
        
        System.out.println("🔌 [BookmapAddon] 🌐 Opening dashboard in Microsoft Edge...");
        
        try {
            // Try to open Microsoft Edge specifically
            if (openInMicrosoftEdge(dashboardUrl)) {
                System.out.println("🔌 [BookmapAddon] ✅ Dashboard opened in Microsoft Edge");
                return;
            }
            
            // Fallback to default browser if Edge not available
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(dashboardUrl));
                    System.out.println("🔌 [BookmapAddon] 🌐 Dashboard opened in default browser");
                    return;
                }
            }
            
        } catch (Exception e) {
            System.out.println("🔌 [BookmapAddon] Could not auto-open browser. Please visit: " + dashboardUrl);
        }
    }
    
    /**
     * Attempts to open URL in Microsoft Edge specifically
     * @param url The URL to open
     * @return true if successfully launched Edge, false otherwise
     */
    private boolean openInMicrosoftEdge(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("windows")) {
                // Try multiple Edge executable paths/commands for Windows
                String[] edgeCommands = {
                    "msedge.exe",                                    // Modern Edge command
                    "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",  // Standard 32-bit path
                    "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",        // Standard 64-bit path
                    "cmd /c start msedge",                           // Through cmd
                    "powershell -command \"Start-Process msedge -ArgumentList '" + url + "'\""  // Through PowerShell
                };
                
                for (String command : edgeCommands) {
                    try {
                        ProcessBuilder pb;
                        if (command.contains("powershell")) {
                            pb = new ProcessBuilder("powershell", "-command", "Start-Process", "msedge", "-ArgumentList", url);
                        } else if (command.contains("cmd")) {
                            pb = new ProcessBuilder("cmd", "/c", "start", "msedge", url);
                        } else {
                            pb = new ProcessBuilder(command, url);
                        }
                        
                        Process process = pb.start();
                        
                        // Wait a short time to see if the process started successfully
                        Thread.sleep(1500);
                        
                        // If process is still alive or exited normally, consider it successful
                        if (process.isAlive() || process.exitValue() == 0) {
                            System.out.println("🔌 [BookmapAddon] ✅ Successfully launched Microsoft Edge with: " + command);
                            return true;
                        }
                    } catch (Exception e) {
                        // Continue to next command
                        continue;
                    }
                }
            } else if (os.contains("mac")) {
                // macOS Edge launch
                ProcessBuilder pb = new ProcessBuilder("open", "-a", "Microsoft Edge", url);
                Process process = pb.start();
                Thread.sleep(1000);
                return process.isAlive() || process.exitValue() == 0;
                
            } else if (os.contains("linux")) {
                // Linux Edge launch
                String[] commands = {"microsoft-edge", "microsoft-edge-stable", "microsoft-edge-dev"};
                for (String command : commands) {
                    try {
                        ProcessBuilder pb = new ProcessBuilder(command, url);
                        Process process = pb.start();
                        Thread.sleep(1000);
                        if (process.isAlive() || process.exitValue() == 0) {
                            return true;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
            
        } catch (Exception e) {
            System.out.println("🔌 [BookmapAddon] Edge launch attempt failed: " + e.getMessage());
        }
        
        return false;
    }
    
    private void startSystemMonitoring() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                // Monitor system health
                monitorSystemHealth();
                
                // Detect open markets
                Set<String> openMarkets = dataExtractor.detectOpenMarkets();
                if (!openMarkets.isEmpty()) {
                    System.out.println("🔌 [BookmapAddon] Open Markets: " + openMarkets);
                }
                
            } catch (Exception e) {
                System.err.println("Error in system monitoring: " + e.getMessage());
            }
        }, 60, 60, TimeUnit.SECONDS); // Every minute
    }
    
    private void monitorSystemHealth() {
        // Check component health
        boolean allHealthy = true;
        
        if (webServer == null || !webServer.isRunning()) {
            System.err.println("⚠️ [BookmapAddon] Web dashboard server not running");
            allHealthy = false;
        }
        
        if (!allHealthy) {
            System.err.println("⚠️ [BookmapAddon] System health check failed");
        }
    }
    
    private double calculateVWAP(String symbol) {
        EnhancedSlidingWindowManager.SlidingWindow window = windowManager.getWindow(symbol, "15M");
        return window != null ? window.getCurrentVWAP() : 0.0;
    }
    
    private void recordAccuracyMetrics(String symbol, MarketDataExtractor.TickData tick, 
                                     Map<String, Double> bookData) {
        // Real accuracy recording based on actual performance - NO SIMULATION
        
        // Only record accuracy when we have real pattern detection results
        // Component accuracy will be calculated from actual trading performance
        
        System.out.println("📊 [BookmapAddon] Real data processed for: " + symbol + " - No simulated accuracy");
    }
    
    private int countActiveComponents() {
        int count = 0;
        if (dataExtractor != null) count++;
        if (windowManager != null) count++;
        if (dashboardManager != null) count++;
        if (aiCore != null) count++;
        if (webServer != null && webServer.isRunning()) count++;
        return count;
    }
    
    // Static factory method for Bookmap addon loading
    public static BookmapAddonIntegration createAddon() {
        return new BookmapAddonIntegration();
    }
    
    // Main method for testing
    public static void main(String[] args) {
        System.out.println("🔌 [BookmapAddon] Starting BookmapAI Addon Test...");
        
        BookmapAddonIntegration addon = createAddon();
        addon.initialize();
        
        // Simulate market data for testing
        simulateMarketData(addon);
        
        // Keep running for demo
        try {
            System.out.println("🔌 [BookmapAddon] Demo running... Press Ctrl+C to stop");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            addon.shutdown();
        }
    }
    
    private static void simulateMarketData(BookmapAddonIntegration addon) {
        ScheduledExecutorService simulator = Executors.newSingleThreadScheduledExecutor();
        
        simulator.scheduleWithFixedDelay(() -> {
            try {
                // Simulate market data for different symbols
                // NO SIMULATION - Only process real Bookmap data
                // Data will come from actual Bookmap Layer1 API when charts are opened
                System.out.println("📊 [BookmapAddon] Waiting for real Bookmap data - no simulation running");
                
            } catch (Exception e) {
                System.err.println("Error in market data simulation: " + e.getMessage());
            }
        }, 1, 2, TimeUnit.SECONDS); // Every 2 seconds
    }
} 