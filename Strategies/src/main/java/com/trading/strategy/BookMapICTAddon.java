package com.trading.strategy;

import com.strategies.dom.analysis.strategy.BookmapDataProcessor;
import velox.api.layer1.Layer1ApiFinishable;
import velox.api.layer1.Layer1ApiInstrumentListener;
import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.data.TradeInfo;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.CustomModule;
import velox.api.layer1.simplified.CustomSettingsPanelProvider;
import velox.api.layer1.simplified.InitialState;
import velox.api.layer1.simplified.TradeDataListener;
import velox.api.layer1.simplified.DepthDataListener;
import velox.api.layer1.simplified.Parameter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Layer1StrategyName("ICT Smart Analyzer Addon")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION1)
public class BookMapICTAddon implements CustomModule, TradeDataListener, DepthDataListener, Layer1ApiFinishable, Layer1ApiInstrumentListener {

    private Api api;
    private final List<String> activeAliases = new CopyOnWriteArrayList<>();
    private String currentAliasForTrade;
    private String currentAliasForDepth;
    
    // Real Data Sliding Window for CSV exports
    private static com.bookmaai.core.RealDataSlidingWindow realDataSlidingWindow;
    
    // Dashboard and system initialization tracking
    private static volatile boolean systemInitialized = false;
    private static com.bookmaai.web.StandaloneDashboard dashboardServer;

    public BookMapICTAddon() {
        System.out.println("[BookMapICTAddon] *** CONSTRUCTOR CALLED *** ICT Smart Analyzer Addon is initializing.");
        
        // IMMEDIATELY test CSV export functionality
        try {
            System.out.println("[BookMapICTAddon] *** TESTING CSV EXPORT IMMEDIATELY ***");
            
            // Test DataExportManager directly
            com.bookmaai.core.DataExportManager testExporter = new com.bookmaai.core.DataExportManager();
            System.out.println("[BookMapICTAddon] DataExportManager created successfully");
            
            // Create test data
            java.util.List<com.bookmaai.core.RealDataSlidingWindow.DataPoint> testData = new java.util.ArrayList<>();
            testData.add(new com.bookmaai.core.RealDataSlidingWindow.DataPoint(1.0850, 100.0, "BUY", java.time.LocalDateTime.now()));
            testData.add(new com.bookmaai.core.RealDataSlidingWindow.DataPoint(1.0851, 150.0, "SELL", java.time.LocalDateTime.now()));
            
            // Force export test data
            testExporter.exportDataPointsToCsv("CONSTRUCTOR_TEST", "1m", testData);
            System.out.println("[BookMapICTAddon] *** CSV EXPORT TEST COMPLETED ***");
            
        } catch (Exception e) {
            System.err.println("[BookMapICTAddon] *** CSV EXPORT TEST FAILED ***: " + e.getMessage());
            e.printStackTrace();
        }
        
        // FALLBACK: Direct CSV creation test
        try {
            System.out.println("[BookMapICTAddon] *** RUNNING DIRECT CSV TEST ***");
            com.bookmaai.test.ImmediateCSVTest.createTestCSVFiles();
            System.out.println("[BookMapICTAddon] *** DIRECT CSV TEST COMPLETED ***");
        } catch (Exception e) {
            System.err.println("[BookMapICTAddon] *** DIRECT CSV TEST FAILED ***: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(String alias, InstrumentInfo instrumentInfo, Api api, InitialState initialState) {
        this.api = api;
        this.currentAliasForTrade = alias;
        this.currentAliasForDepth = alias;
        
        System.out.println("[BookMapICTAddon] *** INITIALIZE METHOD CALLED *** for alias: " + alias);
        
        // FORCE CSV EXPORT TEST IN INITIALIZE
        try {
            System.out.println("[BookMapICTAddon] *** FORCING CSV EXPORT IN INITIALIZE ***");
            com.bookmaai.core.DataExportManager testExporter = new com.bookmaai.core.DataExportManager();
            
            java.util.List<com.bookmaai.core.RealDataSlidingWindow.DataPoint> testData = new java.util.ArrayList<>();
            testData.add(new com.bookmaai.core.RealDataSlidingWindow.DataPoint(1.2345, 200.0, "BUY", java.time.LocalDateTime.now()));
            testData.add(new com.bookmaai.core.RealDataSlidingWindow.DataPoint(1.2346, 250.0, "SELL", java.time.LocalDateTime.now()));
            
            String symbolName = (instrumentInfo != null) ? alias : "UNKNOWN_INSTRUMENT";
            testExporter.exportDataPointsToCsv("INITIALIZE_" + symbolName, "1m", testData);
            System.out.println("[BookMapICTAddon] *** CSV EXPORT IN INITIALIZE COMPLETED ***");
            
        } catch (Exception e) {
            System.err.println("[BookMapICTAddon] *** CSV EXPORT IN INITIALIZE FAILED ***: " + e.getMessage());
            e.printStackTrace();
        }
        
        if (instrumentInfo != null) {
            activeAliases.add(alias);
            System.out.println("[BookMapICTAddon] Initialized for alias: " + alias + ", Instrument: " + instrumentInfo.symbol);
            String initialMarketData = "event=initialize,symbol=" + instrumentInfo.symbol + ",name=" + instrumentInfo.symbol + ",alias=" + alias + ",pips=" + instrumentInfo.pips;
            BookmapDataProcessor.processBookmapData(initialMarketData);
            try {
                // Register window as ACTIVE using alias as windowId; use alias as symbol for consistency
                com.bookmaai.core.RealTimeMarketDataStore.getInstance().addActiveWindow(alias, "ACTIVE", alias);
            } catch (Throwable t) {
                System.err.println("[BookMapICTAddon] Failed to register active window on initialize: " + t.getMessage());
            }
        } else {
            System.out.println("[BookMapICTAddon] Initialized (instrumentInfo is null) for alias: " + alias);
        }
    }
    
    @Override
    public void onInstrumentAdded(String alias, InstrumentInfo instrumentInfo) {
        activeAliases.add(alias);
        System.out.println("[BookMapICTAddon] Instrument added: " + alias + ", Symbol: " + instrumentInfo.symbol);
        String instrumentAddedData = "event=instrumentAdded,symbol=" + instrumentInfo.symbol + ",name=" + instrumentInfo.symbol + ",alias=" + alias + ",pips=" + instrumentInfo.pips;
        BookmapDataProcessor.processBookmapData(instrumentAddedData);
        
        // Initialize the complete system when the first instrument is added
        initializeBookmapAISystem();

        // Enhanced registration with comprehensive instrument tracking
        try {
            com.bookmaai.core.RealTimeMarketDataStore dataStore = com.bookmaai.core.RealTimeMarketDataStore.getInstance();
            
            // Register with full instrument details
            dataStore.addActiveWindow(alias, "ACTIVE", alias);
            dataStore.registerInstrumentDetails(alias, instrumentInfo.getSymbol(), instrumentInfo.getExchange(), 0.01);
            
            // Initialize data validation tracking
            dataStore.initializeInstrumentTracking(alias);
            
            System.out.println("✅ [BookMapICTAddon] Enhanced registration complete for: " + instrumentInfo.getSymbol());
            
        } catch (Throwable t) {
            System.err.println("[BookMapICTAddon] Failed to register active window: " + t.getMessage());
        }
    }

    @Override
    public void onInstrumentRemoved(String alias) {
        activeAliases.remove(alias);
        System.out.println("[BookMapICTAddon] Instrument removed: " + alias);
        try {
            // Remove window by alias/windowId for precise lifecycle tracking
            com.bookmaai.core.RealTimeMarketDataStore.getInstance().removeActiveWindowById(alias);
        } catch (Throwable t) {
            System.err.println("[BookMapICTAddon] Failed to remove active window: " + t.getMessage());
        }
    }

    @Override
    public void onInstrumentAlreadySubscribed(String alias, String fullName, String feedName) {
        System.out.println("[BookMapICTAddon] onInstrumentAlreadySubscribed called for alias: " + alias + ", fullName: " + fullName + ", feedName: " + feedName);
        if (!activeAliases.contains(alias)) {
            activeAliases.add(alias);
            if (this.currentAliasForTrade == null) this.currentAliasForTrade = alias;
            if (this.currentAliasForDepth == null) this.currentAliasForDepth = alias;
        }
    }

    @Override
    public void onInstrumentNotFound(String alias, String fullName, String feedName) {
        System.out.println("[BookMapICTAddon] onInstrumentNotFound called for alias: " + alias + ", fullName: " + fullName + ", feedName: " + feedName);
    }

    @Override
    public void onTrade(double price, int size, TradeInfo tradeInfo) {
        boolean isBid = tradeInfo.isBidAggressor;
        String symbolForData = this.currentAliasForTrade != null ? this.currentAliasForTrade : "unknown_symbol_onTrade";
        String tradeDataString = String.format("event=trade,symbol=%s,price=%.2f,volume=%d,isBid=%b,timestamp=%d",
                                symbolForData, price, size, isBid, System.currentTimeMillis());
        BookmapDataProcessor.processBookmapData(tradeDataString);
        
        // Update dashboard with market metrics if available
        updateDashboardMetrics(symbolForData, price, size, isBid);

        // Enhanced data flow validation and tracking
        try {
            com.bookmaai.core.RealTimeMarketDataStore dataStore = com.bookmaai.core.RealTimeMarketDataStore.getInstance();
            
            // Ensure window is registered and validate data flow
            dataStore.ensureWindowRegistered(symbolForData);
            dataStore.validateInstrumentDataFlow(symbolForData, price, size, System.currentTimeMillis());
            
            // Process with enhanced sliding window manager (for internal analytics)
            com.bookmaai.core.sliding.EnhancedSlidingWindowManager.getInstance()
                .processTick(symbolForData, price, size, System.currentTimeMillis(), isBid);
            
            // CRITICAL: Also feed the RealDataSlidingWindow for CSV exports
            try {
                com.bookmaai.core.RealDataSlidingWindow slidingWindow = getRealDataSlidingWindow();
                slidingWindow.addRealData(symbolForData, price, (double) size, isBid ? "BUY" : "SELL");
                System.out.println("📊 [BookMapICTAddon] Trade data sent to RealDataSlidingWindow: " + symbolForData + " @ " + price + " vol:" + size + " side:" + (isBid ? "BUY" : "SELL"));
                
                // Force export every 10 trades for testing
                if (Math.random() < 0.1) { // 10% chance
                    System.out.println("🔄 [BookMapICTAddon] Forcing immediate CSV export for testing...");
                    slidingWindow.forceExport();
                }
            } catch (Throwable swt) {
                System.err.println("[BookMapICTAddon] Failed to feed RealDataSlidingWindow: " + swt.getMessage());
                swt.printStackTrace();
            }
                
            // Update market data with enhanced tracking
            dataStore.updateMarketData(symbolForData, price, size, isBid ? "BID" : "ASK");
            
        } catch (Throwable t) {
            System.err.println("[BookMapICTAddon] Failed to process enhanced trade data: " + t.getMessage());
        }
    }

    @Override
    public void onDepth(boolean isBid, int price, int size) {
        String symbolForData = this.currentAliasForDepth != null ? this.currentAliasForDepth : "unknown_symbol_onDepth";
        String depthDataString = String.format("event=depth,symbol=%s,isBid=%b,price=%d,size=%d,timestamp=%d",
                                symbolForData, isBid, price, size, System.currentTimeMillis());
        BookmapDataProcessor.processBookmapData(depthDataString);

        // Feed sliding window (convert depth price to double for consistency)
        try {
            com.bookmaai.core.sliding.EnhancedSlidingWindowManager.getInstance()
                .processTick(symbolForData, (double) price, size, System.currentTimeMillis(), isBid);
        } catch (Throwable t) {
            System.err.println("[BookMapICTAddon] Failed to process depth tick for sliding window: " + t.getMessage());
        }
    }
    
    @Override
    public void finish() {
        System.out.println("[BookMapICTAddon] finish() called (for Layer1ApiFinishable). Cleaning up.");
        cleanupResources();
    }

    @Override
    public void stop() {
        System.out.println("[BookMapICTAddon] stop() called (for CustomModule). Cleaning up.");
        cleanupResources();
    }

    /**
     * Get or create the RealDataSlidingWindow instance for CSV exports
     */
    private static synchronized com.bookmaai.core.RealDataSlidingWindow getRealDataSlidingWindow() {
        if (realDataSlidingWindow == null) {
            realDataSlidingWindow = new com.bookmaai.core.RealDataSlidingWindow();
            System.out.println("✅ [BookMapICTAddon] RealDataSlidingWindow initialized for CSV exports");
        }
        return realDataSlidingWindow;
    }

    private void cleanupResources() {
        System.out.println("[BookMapICTAddon] cleanupResources called. Active aliases cleared.");
        activeAliases.clear();
        
        // Shutdown the complete BookmapAI system
        shutdownBookmapAISystem();
        
        // Close all market-specific CSV writers
        try {
            // Close pattern recorders first
            com.bookmaai.core.PatternRecorder.getInstance().closeAll();
            // Then any legacy writers (if present)
            BookmapDataProcessor.closeAllWriters();
            
            System.out.println("[BookMapICTAddon] All market CSV writers closed successfully.");
        } catch (Exception e) {
            System.err.println("[BookMapICTAddon] Error closing market CSV writers: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Print final statistics
        try {
            int marketCount = BookmapDataProcessor.getActiveMarketCount();
            String[] markets = BookmapDataProcessor.getActiveMarkets();
            System.out.println("[BookMapICTAddon] Final Statistics:");
            System.out.println("[BookMapICTAddon] Total Markets Processed: " + markets.length);
            for (String market : markets) {
                String marketName = BookmapDataProcessor.getMarketName(market);
                System.out.println("[BookMapICTAddon] - Market: " + market + " (" + marketName + ")");
            }
        } catch (Exception e) {
            System.err.println("[BookMapICTAddon] Error printing final statistics: " + e.getMessage());
        }
    }

    public void unregisterIndicator(String alias, String indicatorName, Api api) {
         System.out.println("[BookMapICTAddon] unregisterIndicator called for " + alias + ", indicator: " + indicatorName);
    }

    public CustomSettingsPanelProvider getCustomSettingsPanelProvider(Api api) {
        return null;
    }

    public Map<String, Parameter> getParameters(Api api) {
        return null;
    }

    public void setParameters(Map<String, Parameter> params, Api api) {
        if (params != null) {
            System.out.println("[BookMapICTAddon] setParameters called with: " + params.size() + " parameters.");
        }
    }

    private void updateDashboardMetrics(String marketId, double price, int volume, boolean isBid) {
        try {
            // Try to update the AdvancedVwapPatternAnalyzer dashboard with market data
            // This creates a basic integration between BookMap data and the multi-market dashboard
            System.out.println("[BookMapICTAddon] Updating dashboard metrics for market: " + marketId + 
                             ", Price: " + price + ", Volume: " + volume + ", IsBid: " + isBid);
            
            // Note: In a full implementation, you would access the AdvancedVwapPatternAnalyzer instance
            // and call its dashboard.updateMarketMetrics() method with proper market metrics
            
        } catch (Exception e) {
            System.err.println("[BookMapICTAddon] Error updating dashboard metrics: " + e.getMessage());
        }
    }
    
    public String[] getActiveMarkets() {
        return activeAliases.toArray(new String[0]);
    }
    
    public void notifyDashboardOfMarkets() {
        System.out.println("[BookMapICTAddon] Notifying dashboard of active markets:");
        for (String alias : activeAliases) {
            System.out.println("[BookMapICTAddon] - Active market: " + alias);
        }
    }
    
    /**
     * Initialize the complete BookmapAI system including dashboard and slidewindowing
     */
    private static synchronized void initializeBookmapAISystem() {
        if (systemInitialized) {
            return; // Already initialized
        }
        
        System.out.println("🚀 [BookMapICTAddon] Initializing Complete BookmapAI System...");
        
        try {
            // 1. Initialize the real data sliding window first
            System.out.println("🔄 [BookMapICTAddon] Creating RealDataSlidingWindow...");
            realDataSlidingWindow = new com.bookmaai.core.RealDataSlidingWindow();
            System.out.println("✅ [BookMapICTAddon] RealDataSlidingWindow initialized - CSV exports ACTIVE");
            
            // Test the sliding window immediately
            System.out.println("🧪 [BookMapICTAddon] Testing CSV export with sample data...");
            realDataSlidingWindow.addRealData("TEST_SYMBOL", 1.2345, 100.0, "BUY");
            System.out.println("🧪 [BookMapICTAddon] Sample data added - check C:\\Bookmap\\exports for test files");
            
            // 2. Initialize and start the standalone dashboard
            System.out.println("🔄 [BookMapICTAddon] Starting StandaloneDashboard...");
            dashboardServer = new com.bookmaai.web.StandaloneDashboard();
            dashboardServer.start();
            System.out.println("✅ [BookMapICTAddon] StandaloneDashboard server started");
            
            // 3. Mark system as initialized
            systemInitialized = true;
            
            System.out.println("🎯 [BookMapICTAddon] ====== SYSTEM READY ======");
            System.out.println("📊 Dashboard: http://localhost:8080 (auto-launching...)");
            System.out.println("💾 CSV Exports: C:\\Bookmap\\exports");
            System.out.println("🔄 Real-time data processing: ACTIVE");
            System.out.println("🧪 Check C:\\Bookmap\\exports for SYSTEM_TEST_*.csv and TEST_SYMBOL_*.csv files");
            
        } catch (Exception e) {
            System.err.println("❌ [BookMapICTAddon] Failed to initialize BookmapAI system: " + e.getMessage());
            e.printStackTrace();
            systemInitialized = false;
        }
    }
    
    /**
     * Shutdown the BookmapAI system
     */
    private static synchronized void shutdownBookmapAISystem() {
        if (!systemInitialized) {
            return;
        }
        
        System.out.println("🛑 [BookMapICTAddon] Shutting down BookmapAI system...");
        
        try {
            // Stop the dashboard server
            if (dashboardServer != null) {
                dashboardServer.stop();
                System.out.println("✅ [BookMapICTAddon] Dashboard server stopped");
            }
            
            // Export any remaining sliding window data
            if (realDataSlidingWindow != null) {
                realDataSlidingWindow.exportData();
                realDataSlidingWindow.shutdown();
                System.out.println("✅ [BookMapICTAddon] Sliding window data exported and shutdown");
            }
            
            systemInitialized = false;
            System.out.println("✅ [BookMapICTAddon] System shutdown completed");
            
        } catch (Exception e) {
            System.err.println("❌ [BookMapICTAddon] Error during system shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 