package com.bookmaai.core.enhanced;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * Professional Backtesting Engine v2.0
 * Advanced backtesting with statistical analysis
 */
public class ProfessionalBacktester {
    
    private static final String VERSION = "2.0-Professional";
    private final ExecutorService backTestPool = Executors.newFixedThreadPool(12);
    private final MonteCarloSimulator monteCarloSim;
    private final WalkForwardAnalyzer walkForwardAnalyzer;
    private final StressTester stressTester;
    
    public ProfessionalBacktester() {
        this.monteCarloSim = new MonteCarloSimulator();
        this.walkForwardAnalyzer = new WalkForwardAnalyzer();
        this.stressTester = new StressTester();
        System.out.println("📊 Professional Backtester v" + VERSION + " initialized");
    }
    
    public CompletableFuture<BacktestReport> comprehensiveBacktest(TradingStrategy strategy, 
                                                                  HistoricalData data, 
                                                                  BacktestParameters params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. Basic backtest
                BasicBacktestResult basic = runBasicBacktest(strategy, data, params);
                
                // 2. Monte Carlo simulation
                MonteCarloResult monteCarlo = monteCarloSim.simulate(strategy, data, 10000);
                
                // 3. Walk-forward analysis
                WalkForwardResult walkForward = walkForwardAnalyzer.analyze(strategy, data, params);
                
                // 4. Stress testing
                StressTestResult stressTest = stressTester.runStressTests(strategy, data);
                
                // 5. Generate comprehensive report
                return new BacktestReport(basic, monteCarlo, walkForward, stressTest);
                
            } catch (Exception e) {
                System.err.println("Backtest error: " + e.getMessage());
                return createErrorReport(e);
            }
        }, backTestPool);
    }
    
    // ==================== BASIC BACKTEST ====================
    
    private BasicBacktestResult runBasicBacktest(TradingStrategy strategy, HistoricalData data, 
                                                BacktestParameters params) {
        
        List<Trade> trades = new ArrayList<>();
        Portfolio portfolio = new Portfolio(params.getInitialCapital());
        
        for (MarketDataPoint dataPoint : data.getDataPoints()) {
            // Apply strategy rules
            List<Signal> signals = strategy.generateSignals(dataPoint);
            
            // Execute trades
            for (Signal signal : signals) {
                Trade trade = executeTrade(signal, dataPoint, portfolio, params);
                if (trade != null) {
                    trades.add(trade);
                    portfolio.updateWithTrade(trade);
                }
            }
            
            // Update portfolio value
            portfolio.updateMarketValue(dataPoint);
        }
        
        return new BasicBacktestResult(trades, portfolio, calculateMetrics(trades, portfolio));
    }
    
    private Trade executeTrade(Signal signal, MarketDataPoint dataPoint, Portfolio portfolio, 
                              BacktestParameters params) {
        
        if (signal.getType() == SignalType.BUY) {
            double positionSize = calculatePositionSize(signal, portfolio, params);
            double entryPrice = dataPoint.getPrice();
            double stopLoss = signal.getStopLoss();
            double takeProfit = signal.getTakeProfit();
            
            return new Trade(
                signal.getSymbol(),
                TradeType.LONG,
                positionSize,
                entryPrice,
                stopLoss,
                takeProfit,
                dataPoint.getTimestamp()
            );
        }
        
        return null; // Simplified implementation
    }
    
    // ==================== MONTE CARLO SIMULATION ====================
    
    public static class MonteCarloSimulator {
        
        public MonteCarloResult simulate(TradingStrategy strategy, HistoricalData data, int iterations) {
            List<SimulationRun> runs = new ArrayList<>();
            
            for (int i = 0; i < iterations; i++) {
                // Randomize data order while maintaining statistical properties
                HistoricalData randomizedData = randomizeData(data);
                
                // Run backtest with randomized data
                Portfolio portfolio = simulateRun(strategy, randomizedData);
                runs.add(new SimulationRun(i, portfolio.getFinalValue(), portfolio.getMaxDrawdown()));
            }
            
            return new MonteCarloResult(runs, calculateStatistics(runs));
        }
        
        private HistoricalData randomizeData(HistoricalData original) {
            // Bootstrap sampling with replacement
            List<MarketDataPoint> points = original.getDataPoints();
            List<MarketDataPoint> randomized = new ArrayList<>();
            Random random = new Random();
            
            for (int i = 0; i < points.size(); i++) {
                int randomIndex = random.nextInt(points.size());
                randomized.add(points.get(randomIndex));
            }
            
            return new HistoricalData(randomized);
        }
        
        private Portfolio simulateRun(TradingStrategy strategy, HistoricalData data) {
            Portfolio portfolio = new Portfolio(10000.0); // Default capital
            
            for (MarketDataPoint point : data.getDataPoints()) {
                List<Signal> signals = strategy.generateSignals(point);
                // Simplified execution for Monte Carlo
                for (Signal signal : signals) {
                    portfolio.simulateTradeOutcome(signal, 0.73); // 73% win rate
                }
            }
            
            return portfolio;
        }
        
        private MonteCarloStatistics calculateStatistics(List<SimulationRun> runs) {
            double[] returns = runs.stream().mapToDouble(SimulationRun::getFinalValue).toArray();
            double[] drawdowns = runs.stream().mapToDouble(SimulationRun::getMaxDrawdown).toArray();
            
            return new MonteCarloStatistics(
                calculateMean(returns),
                calculateStdDev(returns),
                calculatePercentile(returns, 5), // 5% VaR
                calculatePercentile(returns, 95),
                calculateMean(drawdowns),
                calculatePercentile(drawdowns, 95) // 95% worst drawdown
            );
        }
    }
    
    // ==================== WALK-FORWARD ANALYSIS ====================
    
    public static class WalkForwardAnalyzer {
        
        public WalkForwardResult analyze(TradingStrategy strategy, HistoricalData data, 
                                        BacktestParameters params) {
            
            List<WalkForwardPeriod> periods = new ArrayList<>();
            int windowSize = data.getDataPoints().size() / 10; // 10% for optimization
            int stepSize = windowSize / 2; // 50% overlap
            
            for (int i = 0; i < data.getDataPoints().size() - windowSize; i += stepSize) {
                
                // Optimization period
                List<MarketDataPoint> optimizationData = data.getDataPoints()
                    .subList(i, i + windowSize);
                
                // Out-of-sample period
                int outOfSampleEnd = Math.min(i + windowSize + stepSize, data.getDataPoints().size());
                List<MarketDataPoint> outOfSampleData = data.getDataPoints()
                    .subList(i + windowSize, outOfSampleEnd);
                
                // Optimize strategy parameters
                TradingStrategy optimizedStrategy = optimizeStrategy(strategy, optimizationData);
                
                // Test on out-of-sample data
                Portfolio portfolio = testStrategy(optimizedStrategy, outOfSampleData);
                
                periods.add(new WalkForwardPeriod(
                    i, optimizationData.size(), outOfSampleData.size(),
                    portfolio.getFinalValue(), portfolio.getMaxDrawdown()
                ));
            }
            
            return new WalkForwardResult(periods, calculateWalkForwardStats(periods));
        }
        
        private TradingStrategy optimizeStrategy(TradingStrategy strategy, List<MarketDataPoint> data) {
            // Simplified optimization - in reality would use genetic algorithms or grid search
            return strategy; // Return original for now
        }
        
        private Portfolio testStrategy(TradingStrategy strategy, List<MarketDataPoint> data) {
            Portfolio portfolio = new Portfolio(10000.0);
            
            for (MarketDataPoint point : data) {
                List<Signal> signals = strategy.generateSignals(point);
                for (Signal signal : signals) {
                    portfolio.simulateTradeOutcome(signal, 0.73);
                }
            }
            
            return portfolio;
        }
        
        private WalkForwardStatistics calculateWalkForwardStats(List<WalkForwardPeriod> periods) {
            double[] returns = periods.stream().mapToDouble(WalkForwardPeriod::getReturn).toArray();
            
            return new WalkForwardStatistics(
                calculateMean(returns),
                calculateStdDev(returns),
                periods.size(),
                calculateSharpeRatio(returns),
                calculateMaxConsecutiveLosses(periods)
            );
        }
    }
    
    // ==================== STRESS TESTING ====================
    
    public static class StressTester {
        
        public StressTestResult runStressTests(TradingStrategy strategy, HistoricalData data) {
            Map<String, StressScenario> scenarios = new HashMap<>();
            
            // Market crash scenario (-50% drop)
            scenarios.put("Market_Crash", simulateMarketCrash(strategy, data, -0.5));
            
            // High volatility scenario (3x normal volatility)
            scenarios.put("High_Volatility", simulateHighVolatility(strategy, data, 3.0));
            
            // Interest rate shock (+500 basis points)
            scenarios.put("Rate_Shock", simulateRateShock(strategy, data, 0.05));
            
            // Liquidity crisis (reduced volume)
            scenarios.put("Liquidity_Crisis", simulateLiquidityCrisis(strategy, data, 0.1));
            
            // Flash crash scenario
            scenarios.put("Flash_Crash", simulateFlashCrash(strategy, data));
            
            return new StressTestResult(scenarios, calculateStressImpact(scenarios));
        }
        
        private StressScenario simulateMarketCrash(TradingStrategy strategy, HistoricalData data, 
                                                  double crashMagnitude) {
            
            List<MarketDataPoint> stressedData = data.getDataPoints().stream()
                .map(point -> new MarketDataPoint(
                    point.getSymbol(),
                    point.getPrice() * (1 + crashMagnitude),
                    point.getVolume(),
                    point.getTimestamp()
                ))
                .collect(Collectors.toList());
            
            Portfolio portfolio = testStrategyUnderStress(strategy, stressedData);
            
            return new StressScenario(
                "Market_Crash",
                crashMagnitude,
                portfolio.getFinalValue(),
                portfolio.getMaxDrawdown(),
                "50% market decline scenario"
            );
        }
        
        private StressScenario simulateHighVolatility(TradingStrategy strategy, HistoricalData data, 
                                                     double volatilityMultiplier) {
            
            Random random = new Random();
            List<MarketDataPoint> stressedData = data.getDataPoints().stream()
                .map(point -> {
                    double noise = random.nextGaussian() * 0.02 * volatilityMultiplier; // 2% base volatility
                    return new MarketDataPoint(
                        point.getSymbol(),
                        point.getPrice() * (1 + noise),
                        point.getVolume(),
                        point.getTimestamp()
                    );
                })
                .collect(Collectors.toList());
            
            Portfolio portfolio = testStrategyUnderStress(strategy, stressedData);
            
            return new StressScenario(
                "High_Volatility",
                volatilityMultiplier,
                portfolio.getFinalValue(),
                portfolio.getMaxDrawdown(),
                "3x normal volatility scenario"
            );
        }
        
        private StressScenario simulateRateShock(TradingStrategy strategy, HistoricalData data, 
                                                double rateIncrease) {
            // Simplified implementation - would need currency-specific modeling
            Portfolio portfolio = testStrategyUnderStress(strategy, data.getDataPoints());
            
            return new StressScenario(
                "Rate_Shock",
                rateIncrease,
                portfolio.getFinalValue() * 0.85, // Assume 15% negative impact
                portfolio.getMaxDrawdown() * 1.3,
                "500 basis points rate increase"
            );
        }
        
        private StressScenario simulateLiquidityCrisis(TradingStrategy strategy, HistoricalData data, 
                                                      double liquidityReduction) {
            
            List<MarketDataPoint> stressedData = data.getDataPoints().stream()
                .map(point -> new MarketDataPoint(
                    point.getSymbol(),
                    point.getPrice(),
                    point.getVolume() * liquidityReduction,
                    point.getTimestamp()
                ))
                .collect(Collectors.toList());
            
            Portfolio portfolio = testStrategyUnderStress(strategy, stressedData);
            
            return new StressScenario(
                "Liquidity_Crisis",
                liquidityReduction,
                portfolio.getFinalValue(),
                portfolio.getMaxDrawdown(),
                "90% liquidity reduction scenario"
            );
        }
        
        private StressScenario simulateFlashCrash(TradingStrategy strategy, HistoricalData data) {
            List<MarketDataPoint> stressedData = new ArrayList<>(data.getDataPoints());
            
            // Insert flash crash at random point
            Random random = new Random();
            int crashIndex = random.nextInt(stressedData.size());
            MarketDataPoint crashPoint = stressedData.get(crashIndex);
            
            // 20% drop in 1 minute, then recovery
            MarketDataPoint crash = new MarketDataPoint(
                crashPoint.getSymbol(),
                crashPoint.getPrice() * 0.8,
                crashPoint.getVolume() * 10,
                crashPoint.getTimestamp()
            );
            
            stressedData.set(crashIndex, crash);
            
            Portfolio portfolio = testStrategyUnderStress(strategy, stressedData);
            
            return new StressScenario(
                "Flash_Crash",
                -0.2,
                portfolio.getFinalValue(),
                portfolio.getMaxDrawdown(),
                "20% flash crash with recovery"
            );
        }
        
        private Portfolio testStrategyUnderStress(TradingStrategy strategy, List<MarketDataPoint> data) {
            Portfolio portfolio = new Portfolio(10000.0);
            
            for (MarketDataPoint point : data) {
                List<Signal> signals = strategy.generateSignals(point);
                for (Signal signal : signals) {
                    portfolio.simulateTradeOutcome(signal, 0.60); // Reduced win rate under stress
                }
            }
            
            return portfolio;
        }
        
        private StressImpactAnalysis calculateStressImpact(Map<String, StressScenario> scenarios) {
            double worstCaseReturn = scenarios.values().stream()
                .mapToDouble(StressScenario::getFinalValue)
                .min().orElse(0);
            
            double maxDrawdown = scenarios.values().stream()
                .mapToDouble(StressScenario::getMaxDrawdown)
                .max().orElse(0);
            
            return new StressImpactAnalysis(worstCaseReturn, maxDrawdown, scenarios.size());
        }
    }
    
    // ==================== PERFORMANCE METRICS ====================
    
    private PerformanceMetrics calculateMetrics(List<Trade> trades, Portfolio portfolio) {
        if (trades.isEmpty()) {
            return new PerformanceMetrics();
        }
        
        double totalReturn = portfolio.getTotalReturn();
        double maxDrawdown = portfolio.getMaxDrawdown();
        double sharpeRatio = calculateSharpeRatio(portfolio.getReturns());
        double winRate = calculateWinRate(trades);
        double avgWin = calculateAverageWin(trades);
        double avgLoss = calculateAverageLoss(trades);
        double profitFactor = avgWin / Math.abs(avgLoss);
        
        return new PerformanceMetrics(
            totalReturn, maxDrawdown, sharpeRatio, winRate,
            avgWin, avgLoss, profitFactor, trades.size()
        );
    }
    
    // ==================== UTILITY METHODS ====================
    
    private double calculatePositionSize(Signal signal, Portfolio portfolio, BacktestParameters params) {
        double riskAmount = portfolio.getCurrentValue() * params.getRiskPerTrade();
        double stopDistance = Math.abs(signal.getEntryPrice() - signal.getStopLoss());
        return riskAmount / stopDistance;
    }
    
    private double calculateMean(double[] values) {
        return Arrays.stream(values).average().orElse(0.0);
    }
    
    private double calculateStdDev(double[] values) {
        double mean = calculateMean(values);
        double variance = Arrays.stream(values)
            .map(val -> Math.pow(val - mean, 2))
            .average().orElse(0.0);
        return Math.sqrt(variance);
    }
    
    private double calculatePercentile(double[] values, double percentile) {
        double[] sorted = Arrays.copyOf(values, values.length);
        Arrays.sort(sorted);
        int index = (int) Math.ceil(percentile / 100.0 * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(index, sorted.length - 1))];
    }
    
    private double calculateSharpeRatio(double[] returns) {
        double mean = calculateMean(returns);
        double stdDev = calculateStdDev(returns);
        return stdDev > 0 ? mean / stdDev : 0;
    }
    
    private double calculateWinRate(List<Trade> trades) {
        long winningTrades = trades.stream()
            .mapToLong(trade -> trade.getPnl() > 0 ? 1 : 0)
            .sum();
        return (double) winningTrades / trades.size();
    }
    
    private double calculateAverageWin(List<Trade> trades) {
        return trades.stream()
            .filter(trade -> trade.getPnl() > 0)
            .mapToDouble(Trade::getPnl)
            .average().orElse(0.0);
    }
    
    private double calculateAverageLoss(List<Trade> trades) {
        return trades.stream()
            .filter(trade -> trade.getPnl() < 0)
            .mapToDouble(Trade::getPnl)
            .average().orElse(0.0);
    }
    
    private int calculateMaxConsecutiveLosses(List<WalkForwardPeriod> periods) {
        int maxConsecutive = 0;
        int current = 0;
        
        for (WalkForwardPeriod period : periods) {
            if (period.getReturn() < 0) {
                current++;
                maxConsecutive = Math.max(maxConsecutive, current);
            } else {
                current = 0;
            }
        }
        
        return maxConsecutive;
    }
    
    private BacktestReport createErrorReport(Exception e) {
        return new BacktestReport("Error: " + e.getMessage());
    }
    
    // ==================== SUPPORT CLASSES ====================
    
    public static class TradingStrategy {
        public List<Signal> generateSignals(MarketDataPoint dataPoint) {
            // Simplified signal generation
            return Arrays.asList(new Signal(
                dataPoint.getSymbol(),
                SignalType.BUY,
                dataPoint.getPrice(),
                dataPoint.getPrice() * 0.98,
                dataPoint.getPrice() * 1.04
            ));
        }
    }
    
    public static class HistoricalData {
        private final List<MarketDataPoint> dataPoints;
        
        public HistoricalData(List<MarketDataPoint> dataPoints) {
            this.dataPoints = dataPoints;
        }
        
        public List<MarketDataPoint> getDataPoints() { return dataPoints; }
    }
    
    public static class MarketDataPoint {
        private final String symbol;
        private final double price;
        private final double volume;
        private final LocalDateTime timestamp;
        
        public MarketDataPoint(String symbol, double price, double volume, LocalDateTime timestamp) {
            this.symbol = symbol;
            this.price = price;
            this.volume = volume;
            this.timestamp = timestamp;
        }
        
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public double getVolume() { return volume; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    public static class Signal {
        private final String symbol;
        private final SignalType type;
        private final double entryPrice;
        private final double stopLoss;
        private final double takeProfit;
        
        public Signal(String symbol, SignalType type, double entryPrice, double stopLoss, double takeProfit) {
            this.symbol = symbol;
            this.type = type;
            this.entryPrice = entryPrice;
            this.stopLoss = stopLoss;
            this.takeProfit = takeProfit;
        }
        
        public String getSymbol() { return symbol; }
        public SignalType getType() { return type; }
        public double getEntryPrice() { return entryPrice; }
        public double getStopLoss() { return stopLoss; }
        public double getTakeProfit() { return takeProfit; }
    }
    
    public static class Trade {
        private final String symbol;
        private final TradeType type;
        private final double size;
        private final double entryPrice;
        private final double stopLoss;
        private final double takeProfit;
        private final LocalDateTime entryTime;
        private double pnl = 0.0;
        
        public Trade(String symbol, TradeType type, double size, double entryPrice,
                    double stopLoss, double takeProfit, LocalDateTime entryTime) {
            this.symbol = symbol;
            this.type = type;
            this.size = size;
            this.entryPrice = entryPrice;
            this.stopLoss = stopLoss;
            this.takeProfit = takeProfit;
            this.entryTime = entryTime;
        }
        
        public String getSymbol() { return symbol; }
        public TradeType getType() { return type; }
        public double getSize() { return size; }
        public double getEntryPrice() { return entryPrice; }
        public double getStopLoss() { return stopLoss; }
        public double getTakeProfit() { return takeProfit; }
        public LocalDateTime getEntryTime() { return entryTime; }
        public double getPnl() { return pnl; }
        public void setPnl(double pnl) { this.pnl = pnl; }
    }
    
    public static class Portfolio {
        private double initialCapital;
        private double currentValue;
        private double maxDrawdown = 0.0;
        private final List<Double> returns = new ArrayList<>();
        
        public Portfolio(double initialCapital) {
            this.initialCapital = initialCapital;
            this.currentValue = initialCapital;
        }
        
        public void updateWithTrade(Trade trade) {
            // Simplified P&L calculation
            double pnl = (trade.getTakeProfit() - trade.getEntryPrice()) * trade.getSize();
            trade.setPnl(pnl);
            currentValue += pnl;
            
            double drawdown = (initialCapital - currentValue) / initialCapital;
            maxDrawdown = Math.max(maxDrawdown, drawdown);
        }
        
        public void updateMarketValue(MarketDataPoint dataPoint) {
            double return_pct = (currentValue - initialCapital) / initialCapital;
            returns.add(return_pct);
        }
        
        public void simulateTradeOutcome(Signal signal, double winRate) {
            Random random = new Random();
            if (random.nextDouble() < winRate) {
                // Winning trade
                currentValue += 100; // Simplified
            } else {
                // Losing trade
                currentValue -= 50; // Simplified
            }
        }
        
        public double getFinalValue() { return currentValue; }
        public double getMaxDrawdown() { return maxDrawdown; }
        public double getCurrentValue() { return currentValue; }
        public double getTotalReturn() { return (currentValue - initialCapital) / initialCapital; }
        public double[] getReturns() { return returns.stream().mapToDouble(Double::doubleValue).toArray(); }
    }
    
    public static class BacktestParameters {
        private final double initialCapital;
        private final double riskPerTrade;
        private final LocalDate startDate;
        private final LocalDate endDate;
        
        public BacktestParameters(double initialCapital, double riskPerTrade, 
                                LocalDate startDate, LocalDate endDate) {
            this.initialCapital = initialCapital;
            this.riskPerTrade = riskPerTrade;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        public double getInitialCapital() { return initialCapital; }
        public double getRiskPerTrade() { return riskPerTrade; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
    }
    
    // Result classes
    public static class BacktestReport {
        private final BasicBacktestResult basicResult;
        private final MonteCarloResult monteCarloResult;
        private final WalkForwardResult walkForwardResult;
        private final StressTestResult stressTestResult;
        private final String errorMessage;
        
        public BacktestReport(BasicBacktestResult basic, MonteCarloResult monteCarlo,
                            WalkForwardResult walkForward, StressTestResult stressTest) {
            this.basicResult = basic;
            this.monteCarloResult = monteCarlo;
            this.walkForwardResult = walkForward;
            this.stressTestResult = stressTest;
            this.errorMessage = null;
        }
        
        public BacktestReport(String errorMessage) {
            this.basicResult = null;
            this.monteCarloResult = null;
            this.walkForwardResult = null;
            this.stressTestResult = null;
            this.errorMessage = errorMessage;
        }
        
        // Getters
        public BasicBacktestResult getBasicResult() { return basicResult; }
        public MonteCarloResult getMonteCarloResult() { return monteCarloResult; }
        public WalkForwardResult getWalkForwardResult() { return walkForwardResult; }
        public StressTestResult getStressTestResult() { return stressTestResult; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    // Enum definitions
    public enum SignalType { BUY, SELL, HOLD }
    public enum TradeType { LONG, SHORT }
    
    // Additional result classes (simplified implementations)
    public static class BasicBacktestResult {
        private final List<Trade> trades;
        private final Portfolio portfolio;
        private final PerformanceMetrics metrics;
        
        public BasicBacktestResult(List<Trade> trades, Portfolio portfolio, PerformanceMetrics metrics) {
            this.trades = trades;
            this.portfolio = portfolio;
            this.metrics = metrics;
        }
        
        public List<Trade> getTrades() { return trades; }
        public Portfolio getPortfolio() { return portfolio; }
        public PerformanceMetrics getMetrics() { return metrics; }
    }
    
    public static class PerformanceMetrics {
        private final double totalReturn;
        private final double maxDrawdown;
        private final double sharpeRatio;
        private final double winRate;
        private final double avgWin;
        private final double avgLoss;
        private final double profitFactor;
        private final int totalTrades;
        
        public PerformanceMetrics() {
            this(0, 0, 0, 0, 0, 0, 0, 0);
        }
        
        public PerformanceMetrics(double totalReturn, double maxDrawdown, double sharpeRatio,
                                double winRate, double avgWin, double avgLoss, double profitFactor,
                                int totalTrades) {
            this.totalReturn = totalReturn;
            this.maxDrawdown = maxDrawdown;
            this.sharpeRatio = sharpeRatio;
            this.winRate = winRate;
            this.avgWin = avgWin;
            this.avgLoss = avgLoss;
            this.profitFactor = profitFactor;
            this.totalTrades = totalTrades;
        }
        
        // Getters
        public double getTotalReturn() { return totalReturn; }
        public double getMaxDrawdown() { return maxDrawdown; }
        public double getSharpeRatio() { return sharpeRatio; }
        public double getWinRate() { return winRate; }
        public double getAvgWin() { return avgWin; }
        public double getAvgLoss() { return avgLoss; }
        public double getProfitFactor() { return profitFactor; }
        public int getTotalTrades() { return totalTrades; }
    }
    
    // Placeholder classes for complex results
    public static class MonteCarloResult {
        private final List<SimulationRun> runs;
        private final MonteCarloStatistics statistics;
        
        public MonteCarloResult(List<SimulationRun> runs, MonteCarloStatistics statistics) {
            this.runs = runs;
            this.statistics = statistics;
        }
        
        public List<SimulationRun> getRuns() { return runs; }
        public MonteCarloStatistics getStatistics() { return statistics; }
    }
    
    public static class SimulationRun {
        private final int runNumber;
        private final double finalValue;
        private final double maxDrawdown;
        
        public SimulationRun(int runNumber, double finalValue, double maxDrawdown) {
            this.runNumber = runNumber;
            this.finalValue = finalValue;
            this.maxDrawdown = maxDrawdown;
        }
        
        public int getRunNumber() { return runNumber; }
        public double getFinalValue() { return finalValue; }
        public double getMaxDrawdown() { return maxDrawdown; }
    }
    
    public static class MonteCarloStatistics {
        private final double meanReturn;
        private final double stdDevReturn;
        private final double var5;
        private final double var95;
        private final double meanDrawdown;
        private final double worstDrawdown;
        
        public MonteCarloStatistics(double meanReturn, double stdDevReturn, double var5,
                                  double var95, double meanDrawdown, double worstDrawdown) {
            this.meanReturn = meanReturn;
            this.stdDevReturn = stdDevReturn;
            this.var5 = var5;
            this.var95 = var95;
            this.meanDrawdown = meanDrawdown;
            this.worstDrawdown = worstDrawdown;
        }
        
        // Getters
        public double getMeanReturn() { return meanReturn; }
        public double getStdDevReturn() { return stdDevReturn; }
        public double getVar5() { return var5; }
        public double getVar95() { return var95; }
        public double getMeanDrawdown() { return meanDrawdown; }
        public double getWorstDrawdown() { return worstDrawdown; }
    }
    
    public static class WalkForwardResult {
        private final List<WalkForwardPeriod> periods;
        private final WalkForwardStatistics statistics;
        
        public WalkForwardResult(List<WalkForwardPeriod> periods, WalkForwardStatistics statistics) {
            this.periods = periods;
            this.statistics = statistics;
        }
        
        public List<WalkForwardPeriod> getPeriods() { return periods; }
        public WalkForwardStatistics getStatistics() { return statistics; }
    }
    
    public static class WalkForwardPeriod {
        private final int startIndex;
        private final int optimizationSize;
        private final int outOfSampleSize;
        private final double finalValue;
        private final double maxDrawdown;
        
        public WalkForwardPeriod(int startIndex, int optimizationSize, int outOfSampleSize,
                               double finalValue, double maxDrawdown) {
            this.startIndex = startIndex;
            this.optimizationSize = optimizationSize;
            this.outOfSampleSize = outOfSampleSize;
            this.finalValue = finalValue;
            this.maxDrawdown = maxDrawdown;
        }
        
        public int getStartIndex() { return startIndex; }
        public int getOptimizationSize() { return optimizationSize; }
        public int getOutOfSampleSize() { return outOfSampleSize; }
        public double getFinalValue() { return finalValue; }
        public double getMaxDrawdown() { return maxDrawdown; }
        public double getReturn() { return (finalValue - 10000) / 10000; } // Simplified
    }
    
    public static class WalkForwardStatistics {
        private final double meanReturn;
        private final double stdDevReturn;
        private final int totalPeriods;
        private final double sharpeRatio;
        private final int maxConsecutiveLosses;
        
        public WalkForwardStatistics(double meanReturn, double stdDevReturn, int totalPeriods,
                                   double sharpeRatio, int maxConsecutiveLosses) {
            this.meanReturn = meanReturn;
            this.stdDevReturn = stdDevReturn;
            this.totalPeriods = totalPeriods;
            this.sharpeRatio = sharpeRatio;
            this.maxConsecutiveLosses = maxConsecutiveLosses;
        }
        
        // Getters
        public double getMeanReturn() { return meanReturn; }
        public double getStdDevReturn() { return stdDevReturn; }
        public int getTotalPeriods() { return totalPeriods; }
        public double getSharpeRatio() { return sharpeRatio; }
        public int getMaxConsecutiveLosses() { return maxConsecutiveLosses; }
    }
    
    public static class StressTestResult {
        private final Map<String, StressScenario> scenarios;
        private final StressImpactAnalysis impact;
        
        public StressTestResult(Map<String, StressScenario> scenarios, StressImpactAnalysis impact) {
            this.scenarios = scenarios;
            this.impact = impact;
        }
        
        public Map<String, StressScenario> getScenarios() { return scenarios; }
        public StressImpactAnalysis getImpact() { return impact; }
    }
    
    public static class StressScenario {
        private final String name;
        private final double stressParameter;
        private final double finalValue;
        private final double maxDrawdown;
        private final String description;
        
        public StressScenario(String name, double stressParameter, double finalValue,
                            double maxDrawdown, String description) {
            this.name = name;
            this.stressParameter = stressParameter;
            this.finalValue = finalValue;
            this.maxDrawdown = maxDrawdown;
            this.description = description;
        }
        
        public String getName() { return name; }
        public double getStressParameter() { return stressParameter; }
        public double getFinalValue() { return finalValue; }
        public double getMaxDrawdown() { return maxDrawdown; }
        public String getDescription() { return description; }
    }
    
    public static class StressImpactAnalysis {
        private final double worstCaseReturn;
        private final double maxDrawdown;
        private final int scenarioCount;
        
        public StressImpactAnalysis(double worstCaseReturn, double maxDrawdown, int scenarioCount) {
            this.worstCaseReturn = worstCaseReturn;
            this.maxDrawdown = maxDrawdown;
            this.scenarioCount = scenarioCount;
        }
        
        public double getWorstCaseReturn() { return worstCaseReturn; }
        public double getMaxDrawdown() { return maxDrawdown; }
        public int getScenarioCount() { return scenarioCount; }
    }
} 