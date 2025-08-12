package com.bookmaai.core.enhanced;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * 🎲 Advanced Monte Carlo Engine - Complete Implementation
 * 
 * Professional-grade Monte Carlo simulation for trading strategy analysis
 * NO PLACEHOLDERS - Fully functional implementation
 */
public class AdvancedMonteCarloEngine {
    
    public static class MonteCarloResults {
        private final int iterations;
        private final double avgReturn;
        private final double stdReturn;
        private final double maxDrawdown;
        private final double avgSharpe;
        private final double avgWinRate;
        private final double percentile5;
        private final double percentile95;
        private final double probabilityOfLoss;
        private final List<Double> allReturns;
        private final Map<String, Double> riskMetrics;
        
        public MonteCarloResults(int iterations, double avgReturn, double stdReturn, double maxDrawdown,
                               double avgSharpe, double avgWinRate, double percentile5, double percentile95,
                               double probabilityOfLoss, List<Double> allReturns) {
            this.iterations = iterations;
            this.avgReturn = avgReturn;
            this.stdReturn = stdReturn;
            this.maxDrawdown = maxDrawdown;
            this.avgSharpe = avgSharpe;
            this.avgWinRate = avgWinRate;
            this.percentile5 = percentile5;
            this.percentile95 = percentile95;
            this.probabilityOfLoss = probabilityOfLoss;
            this.allReturns = new ArrayList<>(allReturns);
            this.riskMetrics = calculateRiskMetrics();
        }
        
        private Map<String, Double> calculateRiskMetrics() {
            Map<String, Double> metrics = new HashMap<>();
            
            // Value at Risk (VaR)
            Collections.sort(allReturns);
            metrics.put("var_5", getPercentile(allReturns, 5));
            metrics.put("var_1", getPercentile(allReturns, 1));
            
            // Conditional Value at Risk (CVaR)
            double var5 = metrics.get("var_5");
            double cvar5 = allReturns.stream()
                    .filter(r -> r <= var5)
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            metrics.put("cvar_5", cvar5);
            
            // Maximum consecutive losses
            metrics.put("max_consecutive_losses", calculateMaxConsecutiveLosses());
            
            // Profit factor
            double grossProfit = allReturns.stream().filter(r -> r > 0).mapToDouble(Double::doubleValue).sum();
            double grossLoss = Math.abs(allReturns.stream().filter(r -> r < 0).mapToDouble(Double::doubleValue).sum());
            metrics.put("profit_factor", grossLoss > 0 ? grossProfit / grossLoss : 0.0);
            
            return metrics;
        }
        
        private double getPercentile(List<Double> sortedValues, int percentile) {
            if (sortedValues.isEmpty()) return 0.0;
            int index = (int) Math.ceil((percentile / 100.0) * sortedValues.size()) - 1;
            index = Math.max(0, Math.min(index, sortedValues.size() - 1));
            return sortedValues.get(index);
        }
        
        private double calculateMaxConsecutiveLosses() {
            int maxConsecutive = 0;
            int currentConsecutive = 0;
            
            for (double ret : allReturns) {
                if (ret < 0) {
                    currentConsecutive++;
                    maxConsecutive = Math.max(maxConsecutive, currentConsecutive);
                } else {
                    currentConsecutive = 0;
                }
            }
            
            return maxConsecutive;
        }
        
        // Getters
        public int getIterations() { return iterations; }
        public double getAvgReturn() { return avgReturn; }
        public double getStdReturn() { return stdReturn; }
        public double getMaxDrawdown() { return maxDrawdown; }
        public double getAvgSharpe() { return avgSharpe; }
        public double getAvgWinRate() { return avgWinRate; }
        public double getPercentile5() { return percentile5; }
        public double getPercentile95() { return percentile95; }
        public double getProbabilityOfLoss() { return probabilityOfLoss; }
        public List<Double> getAllReturns() { return new ArrayList<>(allReturns); }
        public Map<String, Double> getRiskMetrics() { return new HashMap<>(riskMetrics); }
        
        @Override
        public String toString() {
            return String.format(
                "MonteCarloResults{iterations=%d, avgReturn=%.2f%%, maxDD=%.2f%%, sharpe=%.2f, winRate=%.1f%%, probLoss=%.1f%%}",
                iterations, avgReturn * 100, maxDrawdown * 100, avgSharpe, avgWinRate * 100, probabilityOfLoss * 100
            );
        }
    }
    
    public static class SimulationParameters {
        private final int iterations;
        private final double initialCapital;
        private final double riskPerTrade;
        private final int tradingDays;
        private final double winRate;
        private final double avgWin;
        private final double avgLoss;
        private final double volatility;
        
        public SimulationParameters(int iterations, double initialCapital, double riskPerTrade,
                                  int tradingDays, double winRate, double avgWin, double avgLoss, double volatility) {
            this.iterations = iterations;
            this.initialCapital = initialCapital;
            this.riskPerTrade = riskPerTrade;
            this.tradingDays = tradingDays;
            this.winRate = winRate;
            this.avgWin = avgWin;
            this.avgLoss = avgLoss;
            this.volatility = volatility;
        }
        
        // Getters
        public int getIterations() { return iterations; }
        public double getInitialCapital() { return initialCapital; }
        public double getRiskPerTrade() { return riskPerTrade; }
        public int getTradingDays() { return tradingDays; }
        public double getWinRate() { return winRate; }
        public double getAvgWin() { return avgWin; }
        public double getAvgLoss() { return avgLoss; }
        public double getVolatility() { return volatility; }
    }
    
    private final ExecutorService executorService;
    private final AtomicLong simulationsRun = new AtomicLong(0);
    
    public AdvancedMonteCarloEngine() {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        System.out.println("🎲 [AdvancedMonteCarloEngine] Engine initialized with " + 
                         Runtime.getRuntime().availableProcessors() + " threads");
    }
    
    /**
     * Run comprehensive Monte Carlo simulation
     */
    public CompletableFuture<MonteCarloResults> runSimulation(SimulationParameters params) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("🎲 [MonteCarloEngine] Starting simulation with " + params.getIterations() + " iterations");
            
            List<CompletableFuture<SimulationResult>> futures = new ArrayList<>();
            int batchSize = Math.max(1, params.getIterations() / Runtime.getRuntime().availableProcessors());
            
            // Split work across threads
            for (int i = 0; i < params.getIterations(); i += batchSize) {
                int startIdx = i;
                int endIdx = Math.min(i + batchSize, params.getIterations());
                
                CompletableFuture<SimulationResult> future = CompletableFuture.supplyAsync(() -> 
                    runBatchSimulation(params, startIdx, endIdx), executorService);
                futures.add(future);
            }
            
            // Collect all results
            List<IndividualResult> allResults = new ArrayList<>();
            for (CompletableFuture<SimulationResult> future : futures) {
                SimulationResult batchResult = future.join();
                allResults.addAll(batchResult.individualResults);
            }
            
            // Calculate final statistics
            MonteCarloResults results = calculateFinalStatistics(allResults, params);
            
            simulationsRun.addAndGet(params.getIterations());
            System.out.println("🎲 [MonteCarloEngine] Simulation completed: " + results);
            
            return results;
        }, executorService);
    }
    
    private SimulationResult runBatchSimulation(SimulationParameters params, int startIdx, int endIdx) {
        List<IndividualResult> results = new ArrayList<>();
        Random random = new Random(System.currentTimeMillis() + startIdx);
        
        for (int i = startIdx; i < endIdx; i++) {
            IndividualResult result = runSingleIteration(params, random);
            results.add(result);
        }
        
        return new SimulationResult(results);
    }
    
    private IndividualResult runSingleIteration(SimulationParameters params, Random random) {
        double capital = params.getInitialCapital();
        double peakCapital = capital;
        double maxDrawdown = 0.0;
        
        List<Double> trades = new ArrayList<>();
        int wins = 0;
        int totalTrades = 0;
        
        // Simulate trading over specified period
        for (int day = 0; day < params.getTradingDays(); day++) {
            // Generate random number of trades per day (0-3)
            int tradesPerDay = random.nextInt(4);
            
            for (int trade = 0; trade < tradesPerDay; trade++) {
                totalTrades++;
                
                // Generate trade outcome
                double tradeReturn;
                if (random.nextDouble() < params.getWinRate()) {
                    // Winning trade
                    tradeReturn = params.getAvgWin() * (1 + random.nextGaussian() * params.getVolatility());
                    wins++;
                } else {
                    // Losing trade
                    tradeReturn = -params.getAvgLoss() * (1 + Math.abs(random.nextGaussian() * params.getVolatility()));
                }
                
                // Apply position sizing
                double riskAmount = capital * params.getRiskPerTrade();
                double tradePnL = riskAmount * tradeReturn;
                
                capital += tradePnL;
                trades.add(tradeReturn);
                
                // Update peak and drawdown
                if (capital > peakCapital) {
                    peakCapital = capital;
                }
                
                double currentDrawdown = (peakCapital - capital) / peakCapital;
                maxDrawdown = Math.max(maxDrawdown, currentDrawdown);
                
                // Prevent complete ruin
                if (capital <= 0) {
                    capital = 0.01; // Keep minimal capital to continue simulation
                    break;
                }
            }
        }
        
        double totalReturn = (capital - params.getInitialCapital()) / params.getInitialCapital();
        double winRate = totalTrades > 0 ? (double) wins / totalTrades : 0.0;
        double sharpeRatio = calculateSharpeRatio(trades);
        
        return new IndividualResult(totalReturn, maxDrawdown, sharpeRatio, winRate, trades);
    }
    
    private double calculateSharpeRatio(List<Double> returns) {
        if (returns.size() < 2) return 0.0;
        
        double avgReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double stdDev = calculateStandardDeviation(returns);
        
        if (stdDev == 0) return 0.0;
        
        // Annualized Sharpe ratio
        return (avgReturn * Math.sqrt(252)) / (stdDev * Math.sqrt(252));
    }
    
    private double calculateStandardDeviation(List<Double> values) {
        if (values.size() < 2) return 0.0;
        
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream()
                .mapToDouble(x -> Math.pow(x - mean, 2))
                .average()
                .orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    private MonteCarloResults calculateFinalStatistics(List<IndividualResult> results, SimulationParameters params) {
        List<Double> returns = new ArrayList<>();
        List<Double> drawdowns = new ArrayList<>();
        List<Double> sharpes = new ArrayList<>();
        List<Double> winRates = new ArrayList<>();
        
        for (IndividualResult result : results) {
            returns.add(result.totalReturn);
            drawdowns.add(result.maxDrawdown);
            sharpes.add(result.sharpeRatio);
            winRates.add(result.winRate);
        }
        
        // Calculate statistics
        double avgReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double stdReturn = calculateStandardDeviation(returns);
        double maxDrawdown = drawdowns.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double avgSharpe = sharpes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double avgWinRate = winRates.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        // Calculate percentiles
        Collections.sort(returns);
        double percentile5 = getPercentile(returns, 5);
        double percentile95 = getPercentile(returns, 95);
        
        // Calculate probability of loss
        long lossCount = returns.stream().mapToLong(r -> r < 0 ? 1 : 0).sum();
        double probabilityOfLoss = (double) lossCount / results.size();
        
        return new MonteCarloResults(params.getIterations(), avgReturn, stdReturn, maxDrawdown,
                                   avgSharpe, avgWinRate, percentile5, percentile95, probabilityOfLoss, returns);
    }
    
    private double getPercentile(List<Double> sortedValues, int percentile) {
        if (sortedValues.isEmpty()) return 0.0;
        int index = (int) Math.ceil((percentile / 100.0) * sortedValues.size()) - 1;
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));
        return sortedValues.get(index);
    }
    
    public long getSimulationsRun() {
        return simulationsRun.get();
    }
    
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("🎲 [AdvancedMonteCarloEngine] Engine shutdown completed");
    }
    
    // Helper classes
    private static class IndividualResult {
        final double totalReturn;
        final double maxDrawdown;
        final double sharpeRatio;
        final double winRate;
        final List<Double> trades;
        
        IndividualResult(double totalReturn, double maxDrawdown, double sharpeRatio, double winRate, List<Double> trades) {
            this.totalReturn = totalReturn;
            this.maxDrawdown = maxDrawdown;
            this.sharpeRatio = sharpeRatio;
            this.winRate = winRate;
            this.trades = new ArrayList<>(trades);
        }
    }
    
    private static class SimulationResult {
        final List<IndividualResult> individualResults;
        
        SimulationResult(List<IndividualResult> individualResults) {
            this.individualResults = new ArrayList<>(individualResults);
        }
    }
}
