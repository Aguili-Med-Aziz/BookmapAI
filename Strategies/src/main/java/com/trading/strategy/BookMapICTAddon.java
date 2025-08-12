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

    public BookMapICTAddon() {
        System.out.println("[BookMapICTAddon] Constructor called. ICT Smart Analyzer Addon is initializing.");
    }

    @Override
    public void initialize(String alias, InstrumentInfo instrumentInfo, Api api, InitialState initialState) {
        this.api = api;
        this.currentAliasForTrade = alias;
        this.currentAliasForDepth = alias;
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

        // Register active window in RealTimeMarketDataStore to make detection authoritative
        try {
            com.bookmaai.core.RealTimeMarketDataStore.getInstance().addActiveWindow(alias, "ACTIVE", alias);
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

        // Feed sliding window manager and real-time store for CSV snapshots and charts
        try {
            com.bookmaai.core.RealTimeMarketDataStore.getInstance().ensureWindowRegistered(symbolForData);
            com.bookmaai.core.sliding.EnhancedSlidingWindowManager.getInstance()
                .processTick(symbolForData, price, size, System.currentTimeMillis(), isBid);
        } catch (Throwable t) {
            System.err.println("[BookMapICTAddon] Failed to process tick for sliding window: " + t.getMessage());
        }
        try {
            com.bookmaai.core.RealTimeMarketDataStore.getInstance()
                .updateMarketData(symbolForData, price, size, isBid ? "BID" : "ASK");
        } catch (Throwable t) {
            System.err.println("[BookMapICTAddon] Failed to update market data: " + t.getMessage());
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

    private void cleanupResources() {
        System.out.println("[BookMapICTAddon] cleanupResources called. Active aliases cleared.");
        activeAliases.clear();
        
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
} 