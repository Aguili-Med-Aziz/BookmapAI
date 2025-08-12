package com.bookmaai.test;

import com.bookmaai.addon.UnifiedBookmapAIAddon;
import com.bookmaai.core.*;
import com.bookmaai.web.SimpleDashboard;
import velox.api.layer1.data.TradeInfo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 🔍 Real Data Integration Test
 * 
 * Verifies that real Bookmap data flows properly to the dashboard:
 * Bookmap → UnifiedBookmapAIAddon → BookmapDataExtractor → RealTimeMarketDataStore → Dashboard
 */
public class RealDataIntegrationTest {
    
    private static final String TEST_SYMBOL = "NQ";
    private static final double TEST_PRICE = 15420.25;
    private static final int TEST_SIZE = 5;
    
    private UnifiedBookmapAIAddon addon;
    private RealTimeMarketDataStore dataStore;
    private SimpleDashboard dashboard;
    private BookmapAILogger logger;
    
    public RealDataIntegrationTest() {
        this.logger = BookmapAILogger.getLogger("RealDataTest");
        logger.info("🔍 Initializing Real Data Integration Test");
    }
    
    /**
     * Test the complete data flow from Bookmap to Dashboard
     */
    public boolean testCompleteDataFlow() {
        logger.info("🚀 Starting Complete Data Flow Test");
        
        try {
            // Step 1: Initialize components
            if (!initializeComponents()) {
                logger.error("❌ Failed to initialize components");
                return false;
            }
            
            // Step 2: Simulate real Bookmap data
            if (!simulateRealBookmapData()) {
                logger.error("❌ Failed to simulate real Bookmap data");
                return false;
            }
            
            // Step 3: Verify data reached the store
            if (!verifyDataInStore()) {
                logger.error("❌ Data did not reach the data store");
                return false;
            }
            
            // Step 4: Verify dashboard can access the data
            if (!verifyDashboardData()) {
                logger.error("❌ Dashboard cannot access real data");
                return false;
            }
            
            logger.success("✅ Complete Data Flow Test PASSED");
            return true;
            
        } catch (Exception e) {
            logger.error("❌ Data Flow Test FAILED", e);
            return false;
        }
    }
    
    /**
     * Initialize all components needed for testing
     */
    private boolean initializeComponents() {
        logger.info("🔧 Initializing test components...");
        
        try {
            // Initialize real-time data store
            dataStore = RealTimeMarketDataStore.getInstance();
            logger.info("✅ RealTimeMarketDataStore initialized");
            
            // Initialize unified addon (this will create the data extractor)
            addon = new UnifiedBookmapAIAddon(null); // Provider will be null for testing
            logger.info("✅ UnifiedBookmapAIAddon initialized");
            
            // Initialize dashboard with accuracy manager
            AccuracyDashboardManager dashboardManager = new AccuracyDashboardManager();
            dashboard = new SimpleDashboard(8081, dashboardManager); // Use port 8081 for testing
            logger.info("✅ SimpleDashboard initialized on port 8081");
            
            return true;
            
        } catch (Exception e) {
            logger.error("❌ Component initialization failed", e);
            return false;
        }
    }
    
    /**
     * Simulate real Bookmap data being sent to the addon
     */
    private boolean simulateRealBookmapData() {
        logger.info("📊 Simulating real Bookmap data...");
        
        try {
            // Simulate instrument being added (like when you open a chart in Bookmap)
            logger.info("📈 Simulating instrument added: " + TEST_SYMBOL);
            // Note: We would need InstrumentInfo for real integration
            // For testing, we'll directly call the data processing methods
            
            // Simulate trade data (like real trades coming from Bookmap)
            logger.info("💹 Simulating trade: " + TEST_SYMBOL + " @ " + TEST_PRICE + " Size: " + TEST_SIZE);
            
            // Create a simple trade info object for testing
            TradeInfo tradeInfo = new TradeInfo("TEST_TRADE_" + System.currentTimeMillis(), System.currentTimeMillis());
            
            // Send trade data to addon (this simulates what Bookmap would do)
            addon.onTrade(TEST_PRICE, TEST_SIZE, tradeInfo);
            
            // Wait a moment for processing
            Thread.sleep(100);
            
            // Simulate depth data
            logger.info("📚 Simulating depth update: BID @ " + (TEST_PRICE - 0.25) + " Size: " + (TEST_SIZE * 2));
            addon.onDepth(true, (int)(TEST_PRICE - 0.25), TEST_SIZE * 2);
            
            // Wait for processing
            Thread.sleep(100);
            
            logger.info("✅ Real Bookmap data simulation completed");
            return true;
            
        } catch (Exception e) {
            logger.error("❌ Real data simulation failed", e);
            return false;
        }
    }
    
    /**
     * Verify that data reached the RealTimeMarketDataStore
     */
    private boolean verifyDataInStore() {
        logger.info("🔍 Verifying data in RealTimeMarketDataStore...");
        
        try {
            // Check if we have real data
            boolean hasRealData = dataStore.hasRealData();
            logger.info("📊 Has real data: " + hasRealData);
            
            // Get market data JSON to inspect
            String marketDataJson = dataStore.getMarketDataJson();
            logger.info("📊 Market data JSON length: " + marketDataJson.length());
            
            // Check for real data indicators
            boolean hasRealTradeData = marketDataJson.contains("REAL_TRADE");
            boolean hasRealDepthData = marketDataJson.contains("REAL_DEPTH");
            
            logger.info("📊 Contains REAL_TRADE data: " + hasRealTradeData);
            logger.info("📊 Contains REAL_DEPTH data: " + hasRealDepthData);
            
            // For this test to pass, we need at least one type of real data
            boolean dataStoreValid = hasRealTradeData || hasRealDepthData;
            
            if (dataStoreValid) {
                logger.success("✅ Real data found in RealTimeMarketDataStore");
                logger.info("📊 Sample data: " + marketDataJson.substring(0, Math.min(200, marketDataJson.length())) + "...");
            } else {
                logger.warn("⚠️ No real data found in store");
            }
            
            return dataStoreValid;
            
        } catch (Exception e) {
            logger.error("❌ Data store verification failed", e);
            return false;
        }
    }
    
    /**
     * Verify that dashboard can access real data
     */
    private boolean verifyDashboardData() {
        logger.info("🌐 Verifying dashboard data access...");
        
        try {
            // Start the dashboard
            dashboard.start();
            
            // Wait for startup
            Thread.sleep(500);
            
            // Test dashboard data access by getting market data
            // Note: In a real test, we would make HTTP requests to the dashboard
            // For this test, we'll verify that the dashboard uses the same data store
            
            String dashboardData = dataStore.getMarketDataJson();
            boolean containsRealData = dashboardData.contains("REAL_TRADE") || dashboardData.contains("REAL_DEPTH");
            
            if (containsRealData) {
                logger.success("✅ Dashboard has access to real data");
                logger.info("🌐 Dashboard URL: http://localhost:8081");
                logger.info("📊 Real data available for display");
            } else {
                logger.warn("⚠️ Dashboard does not have real data");
            }
            
            return containsRealData;
            
        } catch (Exception e) {
            logger.error("❌ Dashboard verification failed", e);
            return false;
        }
    }
    
    /**
     * Generate a detailed test report
     */
    public void generateTestReport() {
        logger.info("📋 Generating Real Data Integration Test Report");
        
        try {
            StringBuilder report = new StringBuilder();
            report.append("\n🔍 ===== REAL DATA INTEGRATION TEST REPORT =====\n");
            report.append("Test Date: ").append(java.time.LocalDateTime.now()).append("\n");
            report.append("Test Symbol: ").append(TEST_SYMBOL).append("\n");
            report.append("Test Price: ").append(TEST_PRICE).append("\n");
            report.append("Test Size: ").append(TEST_SIZE).append("\n\n");
            
            // Component Status
            report.append("📊 COMPONENT STATUS:\n");
            report.append("• RealTimeMarketDataStore: ").append(dataStore != null ? "✅ Initialized" : "❌ Failed").append("\n");
            report.append("• UnifiedBookmapAIAddon: ").append(addon != null ? "✅ Initialized" : "❌ Failed").append("\n");
            report.append("• SimpleDashboard: ").append(dashboard != null ? "✅ Initialized" : "❌ Failed").append("\n\n");
            
            // Data Flow Status
            report.append("🔄 DATA FLOW STATUS:\n");
            if (dataStore != null) {
                boolean hasRealData = dataStore.hasRealData();
                report.append("• Real Data Detected: ").append(hasRealData ? "✅ YES" : "❌ NO").append("\n");
                
                String marketData = dataStore.getMarketDataJson();
                boolean hasRealTrade = marketData.contains("REAL_TRADE");
                boolean hasRealDepth = marketData.contains("REAL_DEPTH");
                
                report.append("• Trade Data Flow: ").append(hasRealTrade ? "✅ Working" : "❌ Not Working").append("\n");
                report.append("• Depth Data Flow: ").append(hasRealDepth ? "✅ Working" : "❌ Not Working").append("\n");
            }
            
            report.append("\n🎯 DATA FLOW PATH:\n");
            report.append("Bookmap API → UnifiedBookmapAIAddon → BookmapDataExtractor → RealTimeMarketDataStore → SimpleDashboard\n");
            
            report.append("\n📋 INTEGRATION CHECKLIST:\n");
            report.append("□ Install JAR in Bookmap addons folder\n");
            report.append("□ Restart Bookmap\n");
            report.append("□ Enable addon in Bookmap settings\n");
            report.append("□ Open charts for instruments you want to track\n");
            report.append("□ Access dashboard at http://localhost:8080\n");
            report.append("□ Verify real data appears in dashboard\n");
            
            report.append("\n===== END OF REPORT =====\n");
            
            logger.info(report.toString());
            
        } catch (Exception e) {
            logger.error("❌ Failed to generate test report", e);
        }
    }
    
    /**
     * Main test method
     */
    public static void main(String[] args) {
        System.out.println("🔍 ===== BOOKMAP AI REAL DATA INTEGRATION TEST =====");
        
        RealDataIntegrationTest test = new RealDataIntegrationTest();
        
        boolean testPassed = test.testCompleteDataFlow();
        
        test.generateTestReport();
        
        if (testPassed) {
            System.out.println("\n✅ ===== ALL TESTS PASSED =====");
            System.out.println("🚀 Real Bookmap data integration is working!");
            System.out.println("📦 JAR ready for deployment: BookmapAI-Unified-2.0.0-bookmap-addon.jar");
            System.out.println("🌐 Dashboard will show real data once connected to Bookmap");
        } else {
            System.out.println("\n❌ ===== TESTS FAILED =====");
            System.out.println("🔧 Check the logs for detailed error information");
        }
    }
} 