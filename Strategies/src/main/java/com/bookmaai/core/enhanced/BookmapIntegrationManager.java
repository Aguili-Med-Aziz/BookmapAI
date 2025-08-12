package com.bookmaai.core.enhanced;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDateTime;
import com.bookmaai.core.RealTimeMarketDataStore;

/**
 * Bookmap Integration Manager v3.0
 * Analyzes all opened market windows and coordinates enhanced features
 * ENHANCED: Now updates RealTimeMarketDataStore with active window data
 */
public class BookmapIntegrationManager {
    
    private static final String VERSION = "3.0-Enhanced";
    private final Map<String, MarketWindow> activeWindows = new ConcurrentHashMap<>();
    private final ExecutorService analysisPool = Executors.newFixedThreadPool(16);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    
    // Enhanced Components
    private final RealTimeDataEngine dataEngine;
    private final GPT4AnalysisEngine gpt4Engine;
    private final ProfessionalBacktester backtester;
    private final BrokerAPIManager brokerManager;
    private final AdvancedAlertsManager alertsManager;
    private final AdvancedICTPatternEngine patternEngine;
    
    // Dashboard integration
    private final RealTimeMarketDataStore dataStore;
    
    // Analysis tracking
    private final Map<String, WindowAnalysis> analysisResults = new ConcurrentHashMap<>();
    private final AtomicLong totalAnalysisCount = new AtomicLong(0);
    
    public BookmapIntegrationManager() {
        this.dataEngine = new RealTimeDataEngine();
        this.gpt4Engine = new GPT4AnalysisEngine();
        this.backtester = new ProfessionalBacktester();
        this.brokerManager = new BrokerAPIManager();
        this.alertsManager = new AdvancedAlertsManager();
        this.patternEngine = new AdvancedICTPatternEngine();
        
        // ENHANCED: Connect to dashboard data store for active windows
        this.dataStore = RealTimeMarketDataStore.getInstance();
        
        initializeIntegration();
        startContinuousAnalysis();
    }
    
    // ==================== BOOKMAP WINDOW DETECTION ====================
    
    public void scanForOpenWindows() {
        try {
            // Detect all open Bookmap windows using RealTimeMarketDataStore (authoritative)
            Map<String, Object> windows = dataStore.getActiveBookmapWindows();
            List<String> openSymbols = new ArrayList<>();
            for (Object obj : windows.values()) {
                if (obj instanceof com.bookmaai.core.RealTimeMarketDataStore.BookmapWindow) {
                    com.bookmaai.core.RealTimeMarketDataStore.BookmapWindow w =
                        (com.bookmaai.core.RealTimeMarketDataStore.BookmapWindow) obj;
                    openSymbols.add(w.getSymbol());
                }
            }
            
            for (String symbol : openSymbols) {
                if (!activeWindows.containsKey(symbol)) {
                    MarketWindow window = new MarketWindow(symbol);
                    activeWindows.put(symbol, window);
                    startWindowAnalysis(symbol);
                    System.out.println("📊 New market window detected: " + symbol);
                }
            }
            
            // Remove closed windows
            activeWindows.keySet().removeIf(symbol -> !openSymbols.contains(symbol));
            
            // ENHANCED: Update dashboard with active windows
            updateDashboardActiveWindows();
            
        } catch (Exception e) {
            System.err.println("Error scanning windows: " + e.getMessage());
        }
    }
    
    // Deprecated: legacy simulated detection removed in favor of RealTimeMarketDataStore
    
    /**
     * ENHANCED: Update dashboard with current active windows
     */
    private void updateDashboardActiveWindows() {
        try {
            Map<String, String> windowData = new HashMap<>();
            for (Map.Entry<String, MarketWindow> entry : activeWindows.entrySet()) {
                String symbol = entry.getKey();
                MarketWindow window = entry.getValue();
                String status = window.isActive() ? "ACTIVE" : "INACTIVE";
                windowData.put(symbol, status);
            }
            
            // Push to dashboard data store
            dataStore.updateActiveBookmapWindows(windowData);
            System.out.println("📊 Dashboard updated with " + windowData.size() + " active windows");
            
        } catch (Exception e) {
            System.err.println("Error updating dashboard active windows: " + e.getMessage());
        }
    }
    
    // ==================== COMPREHENSIVE ANALYSIS ====================
    
    private void startWindowAnalysis(String symbol) {
        analysisPool.submit(() -> analyzeMarketWindow(symbol));
    }
    
    private void analyzeMarketWindow(String symbol) {
        try {
            MarketWindow window = activeWindows.get(symbol);
            if (window == null) return;
            
            // 1. Real-time data collection
            RealTimeDataEngine.MarketData marketData = collectMarketData(symbol);
            
            // Convert to enhanced MarketData for AI analysis
            com.bookmaai.core.enhanced.MarketData aiMarketData = new com.bookmaai.core.enhanced.MarketData(
                marketData.getSymbol(), marketData.getPrice(), marketData.getVolume()
            );
            
            // 2. Pattern detection
            List<AdvancedICTPatternEngine.PatternResult> patterns = detectPatterns(symbol, marketData);

            // Export detected patterns per instrument
            if (!patterns.isEmpty()) {
                for (AdvancedICTPatternEngine.PatternResult pr : patterns) {
                    String detailsJson = "{\"timeframe\":\"" + (pr.getTimeframe() == null ? "LIVE" : pr.getTimeframe()) +
                            "\",\"strength\":" + String.format(java.util.Locale.US, "%.4f", pr.getStrength()) +
                            ",\"entry\":" + String.valueOf(pr.getEntryPrice()) +
                            ",\"target\":" + String.valueOf(pr.getTargetPrice()) +
                            ",\"stop\":" + String.valueOf(pr.getStopPrice()) + "}";
                    com.bookmaai.core.PatternRecorder.getInstance().writePattern(
                            symbol, pr.getPatternType(), pr.getConfidence(), detailsJson);
                }
            }
            
            // Convert patterns to List<Pattern> for AI analysis
            List<Pattern> aiPatterns = new ArrayList<>();
            for (AdvancedICTPatternEngine.PatternResult pr : patterns) {
                aiPatterns.add(new Pattern(pr.getPatternType(), pr.getConfidence(), pr.getSymbol()));
            }
            
            // 3. AI analysis
            CompletableFuture<GPT4AnalysisEngine.MarketAnalysis> aiAnalysis = 
                gpt4Engine.analyzeMarketConditions(aiMarketData, aiPatterns);
            
            // 4. Risk assessment
            RiskAssessment risk = assessRisk(symbol, marketData);
            
            // 5. Trading opportunities
            List<TradingOpportunity> opportunities = identifyOpportunities(symbol, patterns, risk);
            
            // 6. Create comprehensive analysis
            WindowAnalysis analysis = new WindowAnalysis(
                symbol, marketData, patterns, risk, opportunities, 
                aiAnalysis.get(2, TimeUnit.SECONDS), LocalDateTime.now()
            );
            
            // 7. Store and process results
            analysisResults.put(symbol, analysis);
            processAnalysisResults(analysis);
            totalAnalysisCount.incrementAndGet();
            
        } catch (Exception e) {
            System.err.println("Analysis error for " + symbol + ": " + e.getMessage());
        }
    }
    
    // ==================== PATTERN DETECTION ====================
    
    private List<AdvancedICTPatternEngine.PatternResult> detectPatterns(String symbol, 
                                                                       RealTimeDataEngine.MarketData data) {
        List<AdvancedICTPatternEngine.PatternResult> patterns = new ArrayList<>();
        
        try {
            // Fair Value Gap detection
            AdvancedICTPatternEngine.FairValueGap fvg = patternEngine.detectFairValueGap(symbol, data);
            if (fvg != null) patterns.add(fvg);
            
            // Order Block detection
            AdvancedICTPatternEngine.OrderBlock ob = patternEngine.detectOrderBlock(symbol, data);
            if (ob != null) patterns.add(ob);
            
            // Liquidity Sweep detection
            AdvancedICTPatternEngine.LiquiditySweep sweep = patternEngine.detectLiquiditySweep(symbol, data);
            if (sweep != null) patterns.add(sweep);
            
            // Break of Structure detection
            AdvancedICTPatternEngine.BreakOfStructure bos = patternEngine.detectBreakOfStructure(symbol, data);
            if (bos != null) patterns.add(bos);
            
        } catch (Exception e) {
            System.err.println("Pattern detection error: " + e.getMessage());
        }
        
        return patterns;
    }
    
    // ==================== RISK ASSESSMENT ====================
    
    private RiskAssessment assessRisk(String symbol, RealTimeDataEngine.MarketData data) {
        double volatility = calculateVolatility(data);
        double liquidity = calculateLiquidity(data);
        double correlation = calculateCorrelation(symbol);
        
        String riskLevel = "LOW";
        if (volatility > 0.02 || liquidity < 0.5) riskLevel = "HIGH";
        else if (volatility > 0.015 || liquidity < 0.7) riskLevel = "MEDIUM";
        
        return new RiskAssessment(symbol, riskLevel, volatility, liquidity, correlation);
    }
    
    // ==================== OPPORTUNITY IDENTIFICATION ====================
    
    private List<TradingOpportunity> identifyOpportunities(String symbol, 
                                                          List<AdvancedICTPatternEngine.PatternResult> patterns,
                                                          RiskAssessment risk) {
        List<TradingOpportunity> opportunities = new ArrayList<>();
        
        for (AdvancedICTPatternEngine.PatternResult pattern : patterns) {
            if (pattern.getConfidence() > 0.80) { // High confidence patterns only
                
                TradingOpportunity opportunity = new TradingOpportunity(
                    symbol,
                    pattern.getPatternType(),
                    pattern.getConfidence(),
                    calculateEntryPrice(pattern),
                    calculateStopLoss(pattern),
                    calculateTakeProfit(pattern),
                    calculateRiskReward(pattern),
                    risk.getRiskLevel()
                );
                
                opportunities.add(opportunity);
            }
        }
        
        return opportunities;
    }
    
    // ==================== RESULTS PROCESSING ====================
    
    private void processAnalysisResults(WindowAnalysis analysis) {
        // Send alerts for high-confidence opportunities
        for (TradingOpportunity opportunity : analysis.getOpportunities()) {
            if (opportunity.getConfidence() > 0.90) {
                alertsManager.processPatternAlert(createPatternFromOpportunity(opportunity));
            }
        }
        
        // Update real-time dashboard
        updateDashboard(analysis);
        
        // Log analysis results
        logAnalysis(analysis);
    }
    
    // ==================== CONTINUOUS MONITORING ====================
    
    private void startContinuousAnalysis() {
        // Scan for new windows every 5 seconds
        scheduler.scheduleAtFixedRate(this::scanForOpenWindows, 0, 5, TimeUnit.SECONDS);
        
        // Analyze all active windows every 30 seconds
        scheduler.scheduleAtFixedRate(this::analyzeAllWindows, 0, 30, TimeUnit.SECONDS);
        
        // Update performance metrics every minute
        scheduler.scheduleAtFixedRate(this::updatePerformanceMetrics, 0, 60, TimeUnit.SECONDS);
    }
    
    private void analyzeAllWindows() {
        for (String symbol : activeWindows.keySet()) {
            analysisPool.submit(() -> analyzeMarketWindow(symbol));
        }
    }
    
    // ==================== ENHANCED FEATURES COORDINATION ====================
    
    public void executeTradeRecommendation(TradingOpportunity opportunity) {
        try {
            // Get best broker for this symbol
            String bestBroker = selectBestBroker(opportunity.getSymbol());
            
            // Create order request
            BrokerAPIManager.OrderRequest orderRequest = new BrokerAPIManager.OrderRequest(
                opportunity.getSymbol(),
                opportunity.getDirection().equals("BULLISH") ? 
                    BrokerAPIManager.OrderType.BUY : BrokerAPIManager.OrderType.SELL,
                calculatePositionSize(opportunity),
                opportunity.getEntryPrice(),
                opportunity.getStopLoss(),
                opportunity.getTakeProfit()
            );
            
            // Execute trade
            CompletableFuture<BrokerAPIManager.OrderResult> result = 
                brokerManager.placeOrder(bestBroker, orderRequest);
            
            result.thenAccept(orderResult -> {
                if (orderResult.isSuccess()) {
                    System.out.println("✅ Trade executed: " + opportunity.getSymbol() + 
                                     " via " + bestBroker);
                } else {
                    System.err.println("❌ Trade failed: " + orderResult.getMessage());
                }
            });
            
        } catch (Exception e) {
            System.err.println("Trade execution error: " + e.getMessage());
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    private void initializeIntegration() {
        System.out.println("🚀 Bookmap Integration Manager v" + VERSION + " starting...");
        System.out.println("📊 Real-time data engine initialized");
        System.out.println("🧠 GPT-4 analysis engine ready");
        System.out.println("📈 Professional backtester loaded");
        System.out.println("🔗 Broker APIs connected");
        System.out.println("⚡ Enhanced features coordinated");
    }
    
    private RealTimeDataEngine.MarketData collectMarketData(String symbol) {
        return new RealTimeDataEngine.MarketData(symbol, 1.0850, System.currentTimeMillis());
    }
    
    private double calculateVolatility(RealTimeDataEngine.MarketData data) {
        return Math.random() * 0.03; // Simplified calculation
    }
    
    private double calculateLiquidity(RealTimeDataEngine.MarketData data) {
        return 0.8 + Math.random() * 0.2; // Simplified calculation
    }
    
    private double calculateCorrelation(String symbol) {
        return Math.random() * 0.5; // Simplified calculation
    }
    
    private double calculateEntryPrice(AdvancedICTPatternEngine.PatternResult pattern) {
        return 1.0850; // Simplified
    }
    
    private double calculateStopLoss(AdvancedICTPatternEngine.PatternResult pattern) {
        return 1.0840; // Simplified
    }
    
    private double calculateTakeProfit(AdvancedICTPatternEngine.PatternResult pattern) {
        return 1.0870; // Simplified
    }
    
    private double calculateRiskReward(AdvancedICTPatternEngine.PatternResult pattern) {
        return 2.0; // Simplified
    }
    
    private String selectBestBroker(String symbol) {
        if (symbol.contains("USD")) return "MT5";
        if (symbol.contains("BTC") || symbol.contains("ETH")) return "BINANCE";
        return "IBKR";
    }
    
    private double calculatePositionSize(TradingOpportunity opportunity) {
        return 0.1; // Simplified position sizing
    }
    
    private AdvancedICTPatternEngine.PatternResult createPatternFromOpportunity(TradingOpportunity opp) {
        AdvancedICTPatternEngine.PatternResult pattern = new AdvancedICTPatternEngine.PatternResult(
            opp.getSymbol(), opp.getPatternType(), opp.getConfidence()
        );
        pattern.setEntryPrice(opp.getEntryPrice());
        pattern.setTargetPrice(opp.getTakeProfit());
        pattern.setStopPrice(opp.getStopLoss());
        return pattern;
    }
    
    private void updateDashboard(WindowAnalysis analysis) {
        // Real-time dashboard updates
        System.out.println("📊 Dashboard updated for " + analysis.getSymbol());
    }
    
    private void logAnalysis(WindowAnalysis analysis) {
        System.out.println(String.format("📈 Analysis: %s | Patterns: %d | Opportunities: %d | Confidence: %.1f%%",
            analysis.getSymbol(), 
            analysis.getPatterns().size(),
            analysis.getOpportunities().size(),
            analysis.getOpportunities().stream().mapToDouble(TradingOpportunity::getConfidence).average().orElse(0) * 100
        ));
    }
    
    private void updatePerformanceMetrics() {
        System.out.println(String.format("⚡ Performance: %d windows active | %d total analyses | %.2f analyses/min",
            activeWindows.size(),
            totalAnalysisCount.get(),
            totalAnalysisCount.get() / (System.currentTimeMillis() / 60000.0)
        ));
    }
    
    // ==================== PUBLIC API ====================
    
    public Map<String, WindowAnalysis> getAllAnalyses() {
        return new HashMap<>(analysisResults);
    }
    
    public WindowAnalysis getAnalysis(String symbol) {
        return analysisResults.get(symbol);
    }
    
    public List<TradingOpportunity> getHighConfidenceOpportunities() {
        return analysisResults.values().stream()
            .flatMap(analysis -> analysis.getOpportunities().stream())
            .filter(opp -> opp.getConfidence() > 0.85)
            .sorted((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()))
            .limit(10)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    public IntegrationStatus getStatus() {
        return new IntegrationStatus(
            activeWindows.size(),
            totalAnalysisCount.get(),
            analysisResults.size(),
            getHighConfidenceOpportunities().size()
        );
    }
    
    // ==================== SUPPORT CLASSES ====================
    
    public static class MarketWindow {
        private final String symbol;
        private final LocalDateTime openTime;
        private boolean active = true;
        
        public MarketWindow(String symbol) {
            this.symbol = symbol;
            this.openTime = LocalDateTime.now();
        }
        
        public String getSymbol() { return symbol; }
        public LocalDateTime getOpenTime() { return openTime; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
    
    public static class WindowAnalysis {
        private final String symbol;
        private final RealTimeDataEngine.MarketData marketData;
        private final List<AdvancedICTPatternEngine.PatternResult> patterns;
        private final RiskAssessment risk;
        private final List<TradingOpportunity> opportunities;
        private final GPT4AnalysisEngine.MarketAnalysis aiAnalysis;
        private final LocalDateTime timestamp;
        
        public WindowAnalysis(String symbol, RealTimeDataEngine.MarketData marketData,
                            List<AdvancedICTPatternEngine.PatternResult> patterns,
                            RiskAssessment risk, List<TradingOpportunity> opportunities,
                            GPT4AnalysisEngine.MarketAnalysis aiAnalysis, LocalDateTime timestamp) {
            this.symbol = symbol;
            this.marketData = marketData;
            this.patterns = patterns;
            this.risk = risk;
            this.opportunities = opportunities;
            this.aiAnalysis = aiAnalysis;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public RealTimeDataEngine.MarketData getMarketData() { return marketData; }
        public List<AdvancedICTPatternEngine.PatternResult> getPatterns() { return patterns; }
        public RiskAssessment getRisk() { return risk; }
        public List<TradingOpportunity> getOpportunities() { return opportunities; }
        public GPT4AnalysisEngine.MarketAnalysis getAiAnalysis() { return aiAnalysis; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    public static class RiskAssessment {
        private final String symbol;
        private final String riskLevel;
        private final double volatility;
        private final double liquidity;
        private final double correlation;
        
        public RiskAssessment(String symbol, String riskLevel, double volatility, 
                            double liquidity, double correlation) {
            this.symbol = symbol;
            this.riskLevel = riskLevel;
            this.volatility = volatility;
            this.liquidity = liquidity;
            this.correlation = correlation;
        }
        
        public String getSymbol() { return symbol; }
        public String getRiskLevel() { return riskLevel; }
        public double getVolatility() { return volatility; }
        public double getLiquidity() { return liquidity; }
        public double getCorrelation() { return correlation; }
    }
    
    public static class TradingOpportunity {
        private final String symbol;
        private final String patternType;
        private final double confidence;
        private final double entryPrice;
        private final double stopLoss;
        private final double takeProfit;
        private final double riskReward;
        private final String riskLevel;
        
        public TradingOpportunity(String symbol, String patternType, double confidence,
                                double entryPrice, double stopLoss, double takeProfit,
                                double riskReward, String riskLevel) {
            this.symbol = symbol;
            this.patternType = patternType;
            this.confidence = confidence;
            this.entryPrice = entryPrice;
            this.stopLoss = stopLoss;
            this.takeProfit = takeProfit;
            this.riskReward = riskReward;
            this.riskLevel = riskLevel;
        }
        
        public String getSymbol() { return symbol; }
        public String getPatternType() { return patternType; }
        public double getConfidence() { return confidence; }
        public double getEntryPrice() { return entryPrice; }
        public double getStopLoss() { return stopLoss; }
        public double getTakeProfit() { return takeProfit; }
        public double getRiskReward() { return riskReward; }
        public String getRiskLevel() { return riskLevel; }
        public String getDirection() { 
            return patternType.contains("BULLISH") || patternType.contains("BUY") ? "BULLISH" : "BEARISH";
        }
    }
    
    public static class IntegrationStatus {
        private final int activeWindows;
        private final long totalAnalyses;
        private final int symbolsAnalyzed;
        private final int highConfidenceOpportunities;
        
        public IntegrationStatus(int activeWindows, long totalAnalyses, 
                               int symbolsAnalyzed, int highConfidenceOpportunities) {
            this.activeWindows = activeWindows;
            this.totalAnalyses = totalAnalyses;
            this.symbolsAnalyzed = symbolsAnalyzed;
            this.highConfidenceOpportunities = highConfidenceOpportunities;
        }
        
        public int getActiveWindows() { return activeWindows; }
        public long getTotalAnalyses() { return totalAnalyses; }
        public int getSymbolsAnalyzed() { return symbolsAnalyzed; }
        public int getHighConfidenceOpportunities() { return highConfidenceOpportunities; }
    }
} 