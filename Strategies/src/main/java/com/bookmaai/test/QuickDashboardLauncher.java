package com.bookmaai.test;

import com.bookmaai.core.AccuracyDashboardManager;
import com.bookmaai.web.SimpleDashboard;
import com.bookmaai.core.ComprehensiveBookmapAIManager;
import com.bookmaai.core.enhanced.BookmapIntegrationManager;
import com.bookmaai.core.RealTimeMarketDataStore;

/**
 * Quick Dashboard Launcher with Active Bookmap Windows Enhancement
 * Demonstrates the new Active Bookmap Windows feature with real-time updates
 */
public class QuickDashboardLauncher {
    
    public static void main(String[] args) {
        System.out.println("🚀 Starting Enhanced BookmapAI Dashboard with Active Windows...");
        
        try {
            // Initialize core components
            AccuracyDashboardManager dashboardManager = new AccuracyDashboardManager();
            dashboardManager.initialize();
            
            // ENHANCED: Initialize BookmapIntegrationManager for active windows
            BookmapIntegrationManager bookmapManager = new BookmapIntegrationManager();
            
            // ENHANCED: Initialize real-time data store
            RealTimeMarketDataStore dataStore = RealTimeMarketDataStore.getInstance();
            
            // Start scanning for active windows
            System.out.println("📊 Scanning for active Bookmap windows...");
            bookmapManager.scanForOpenWindows();
            
            // Populate demo data in data store for immediate display
            populateActiveWindowsDemo(dataStore);
            
            // Create and start dashboard
            SimpleDashboard dashboard = new SimpleDashboard(8080, dashboardManager);
            dashboard.start();
            
            System.out.println("✅ Enhanced Dashboard launched successfully!");
            System.out.println("🌐 Access the dashboard at: http://localhost:8080");
            System.out.println("📊 Active Bookmap Windows feature is live!");
            System.out.println("🔄 Real-time updates every 1 second with colorization");
            System.out.println("📈 Navigate to Markets section to see Active Bookmap Windows");
            System.out.println();
            System.out.println("🎯 ENHANCEMENT FEATURES:");
            System.out.println("  • Active Bookmap Windows display with real-time data");
            System.out.println("  • Color-coded market types (FOREX=Blue, CRYPTO=Orange, FUTURES=Green)");
            System.out.println("  • Live price updates with colorized change indicators");
            System.out.println("  • Window status tracking and connection monitoring");
            System.out.println("  • 1-second refresh rate with smooth animations");
            System.out.println();
            System.out.println("Press Ctrl+C to stop the dashboard...");
            
            // Setup shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🛑 Shutting down enhanced dashboard...");
                dashboard.stop();
                System.out.println("👋 Dashboard stopped. Goodbye!");
            }));
            
            // Keep running with periodic updates
            while (dashboard.isRunning()) {
                Thread.sleep(5000); // Update every 5 seconds
                
                // Update active windows data
                bookmapManager.scanForOpenWindows();
                
                // Update demo data for realistic simulation
                updateActiveWindowsDemo(dataStore);
                
                // Show periodic status
                if (System.currentTimeMillis() % 30000 < 5000) { // Every 30 seconds
                    System.out.println("⏰ Enhanced Dashboard running with Active Windows... (" + new java.util.Date() + ")");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Enhanced Dashboard launcher failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Populate demo active windows data for immediate display
     */
    private static void populateActiveWindowsDemo(RealTimeMarketDataStore dataStore) {
        System.out.println("📊 Populating Active Bookmap Windows demo data...");
        
        // Simulate active windows detection
        java.util.Map<String, String> demoWindows = new java.util.HashMap<>();
        demoWindows.put("EURUSD", "ACTIVE");
        demoWindows.put("GBPUSD", "ACTIVE");
        demoWindows.put("USDJPY", "ACTIVE");
        demoWindows.put("BTCUSDT", "ACTIVE");
        demoWindows.put("ETHUSDT", "ACTIVE");
        demoWindows.put("ES", "ACTIVE");
        demoWindows.put("NQ", "ACTIVE");
        demoWindows.put("GC", "ACTIVE");
        
        dataStore.updateActiveBookmapWindows(demoWindows);
        
        // Add market data for each symbol
        dataStore.updateMarketData("EURUSD", 1.0847, 1250000, "LIVE_DEMO");
        dataStore.updateMarketData("GBPUSD", 1.2743, 982000, "LIVE_DEMO");
        dataStore.updateMarketData("USDJPY", 149.85, 1500000, "LIVE_DEMO");
        dataStore.updateMarketData("BTCUSDT", 42150.0, 850000, "LIVE_DEMO");
        dataStore.updateMarketData("ETHUSDT", 2580.5, 1200000, "LIVE_DEMO");
        dataStore.updateMarketData("ES", 4785.50, 2100000, "LIVE_DEMO");
        dataStore.updateMarketData("NQ", 16890.25, 1800000, "LIVE_DEMO");
        dataStore.updateMarketData("GC", 2045.80, 450000, "LIVE_DEMO");
        
        System.out.println("✅ Active Windows demo data populated: " + demoWindows.size() + " windows");
    }
    
    /**
     * Update active windows demo data for realistic simulation
     */
    private static void updateActiveWindowsDemo(RealTimeMarketDataStore dataStore) {
        java.util.Random random = new java.util.Random();
        
        // Simulate price updates with small variations
        String[] symbols = {"EURUSD", "GBPUSD", "USDJPY", "BTCUSDT", "ETHUSDT", "ES", "NQ", "GC"};
        double[] basePrices = {1.0847, 1.2743, 149.85, 42150.0, 2580.5, 4785.50, 16890.25, 2045.80};
        
        for (int i = 0; i < symbols.length; i++) {
            // Add small random variations to prices
            double variation = (random.nextDouble() - 0.5) * 0.002; // ±0.1% variation
            double newPrice = basePrices[i] * (1 + variation);
            double newVolume = 500000 + random.nextInt(2000000);
            
            dataStore.updateMarketData(symbols[i], newPrice, newVolume, "REAL_TIME_DEMO");
        }
    }
} 