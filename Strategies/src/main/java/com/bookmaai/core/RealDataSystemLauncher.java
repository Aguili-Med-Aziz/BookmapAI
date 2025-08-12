package com.bookmaai.core;

import com.bookmaai.web.SimpleRealDataServer;

/**
 * 🚀 Real Data System Launcher - 100% REAL DATA ONLY
 * 
 * Launches the complete BookmapAI system with ZERO simulation.
 * Only processes real market data from Bookmap.
 */
public class RealDataSystemLauncher {
    
    private static final String VERSION = "5.4.0-REAL-DATA-ONLY";
    private static final int DASHBOARD_PORT = 8080;
    
    // Core components
    private SimpleRealDataServer dashboardServer;
    private RealDataOnlyManager realDataManager;
    private RealTimeMarketDataStore dataStore;
    private ActiveSessionDetector sessionDetector;
    private RealDataSlidingWindow slidingWindow;
    
    // System state
    private volatile boolean isRunning = false;
    
    public static void main(String[] args) {
        System.out.println("🚀 ===== BookmapAI Real Data System =====");
        System.out.println("📊 Version: " + VERSION);
        System.out.println("🔥 Data Source: 100% REAL Bookmap feeds");
        System.out.println("❌ Simulation: COMPLETELY DISABLED");
        System.out.println("✅ Math.random(): ELIMINATED");
        System.out.println("========================================\n");
        
        RealDataSystemLauncher launcher = new RealDataSystemLauncher();
        launcher.startSystem();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(launcher::stopSystem));
        
        // Keep running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            launcher.stopSystem();
        }
    }
    
    /**
     * Start the complete real data system
     */
    public void startSystem() {
        if (isRunning) {
            System.out.println("⚠️ System already running");
            return;
        }
        
        System.out.println("🚀 Starting Real Data System Components...");
        System.out.println("==========================================");
        
        try {
            // 1. Initialize core data store
            dataStore = RealTimeMarketDataStore.getInstance();
            System.out.println("✅ Real-time data store initialized");
            
            // 2. Start real data manager
            realDataManager = new RealDataOnlyManager();
            realDataManager.startRealDataSystem();
            System.out.println("✅ Real data manager started");
            
            // 3. Start session detector (real sessions only)
            sessionDetector = new ActiveSessionDetector();
            sessionDetector.startDetection();
            System.out.println("✅ Active session detector started");
            
            // 4. Start sliding window aggregator
            slidingWindow = new RealDataSlidingWindow();
            System.out.println("✅ Sliding window aggregator initialized");
            
            // 5. Start dashboard server
            dashboardServer = new SimpleRealDataServer(DASHBOARD_PORT);
            dashboardServer.start();
            System.out.println("✅ Dashboard server started on port " + DASHBOARD_PORT);
            
            isRunning = true;
            
            System.out.println("\n🎯 ===== SYSTEM READY =====");
            System.out.println("📊 Dashboard: http://localhost:" + DASHBOARD_PORT);
            System.out.println("🔗 Waiting for Bookmap connections...");
            System.out.println("📋 Instructions:");
            System.out.println("   1. Open Bookmap trading platform");
            System.out.println("   2. Load the UnifiedBookmapAIAddon");
            System.out.println("   3. Open trading charts for any instruments");
            System.out.println("   4. Real data will automatically flow to the dashboard");
            System.out.println("==========================\n");
            
            // Start monitoring
            startSystemMonitoring();
            
        } catch (Exception e) {
            System.err.println("❌ Failed to start real data system: " + e.getMessage());
            e.printStackTrace();
            stopSystem();
        }
    }
    
    /**
     * Stop the real data system
     */
    public void stopSystem() {
        if (!isRunning) {
            return;
        }
        
        System.out.println("\n🛑 Stopping Real Data System...");
        System.out.println("=================================");
        
        try {
            // Stop components in reverse order
            if (dashboardServer != null) {
                dashboardServer.stop();
                System.out.println("✅ Dashboard server stopped");
            }
            
            if (sessionDetector != null) {
                sessionDetector.stopDetection();
                System.out.println("✅ Session detector stopped");
            }
            
            if (realDataManager != null) {
                realDataManager.stopRealDataSystem();
                System.out.println("✅ Real data manager stopped");
            }
            
            // Export final data
            if (slidingWindow != null) {
                slidingWindow.exportData();
                System.out.println("✅ Final data export completed");
            }
            
            isRunning = false;
            
            System.out.println("✅ Real Data System stopped successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error stopping system: " + e.getMessage());
        }
    }
    
    /**
     * Start system monitoring
     */
    private void startSystemMonitoring() {
        Thread monitorThread = new Thread(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(30000); // Monitor every 30 seconds
                    printSystemStatus();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        
        monitorThread.setDaemon(true);
        monitorThread.setName("SystemMonitor");
        monitorThread.start();
        
        System.out.println("✅ System monitoring started");
    }
    
    /**
     * Print system status
     */
    private void printSystemStatus() {
        try {
            boolean hasRealData = dataStore.hasRealDataConnections();
            int activeWindows = dataStore.getActiveBookmapWindows().size();
            
            System.out.println("📊 [Monitor] Real Data: " + (hasRealData ? "✅ Connected" : "⚠️ Waiting"));
            System.out.println("📊 [Monitor] Active Windows: " + activeWindows);
            System.out.println("📊 [Monitor] Dashboard: http://localhost:" + DASHBOARD_PORT);
            
            if (!hasRealData) {
                System.out.println("📋 [Monitor] Waiting for Bookmap connections...");
            }
            
        } catch (Exception e) {
            System.err.println("❌ [Monitor] Error: " + e.getMessage());
        }
    }
    
    /**
     * Process real market data from Bookmap addon
     */
    public void processRealMarketData(String symbol, double price, double volume, String side) {
        if (!isRunning) {
            return;
        }
        
        try {
            // Send to real data manager
            if (realDataManager != null) {
                realDataManager.processRealMarketData(symbol, price, volume, null);
            }
            
            // Send to sliding window
            if (slidingWindow != null) {
                slidingWindow.addRealData(symbol, price, volume, side);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error processing real market data: " + e.getMessage());
        }
    }
    
    /**
     * Get system status for external monitoring
     */
    public boolean isSystemRunning() {
        return isRunning;
    }
    
    /**
     * Get system version
     */
    public String getVersion() {
        return VERSION;
    }
}
