package com.bookmaai.core;

import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * 💰 Risk Reward Calculator - حاسبة المخاطر والعوائد الذكية
 */
public class RiskRewardCalculator {
    
    public static class RiskRewardAnalysis {
        private final double entryPrice;
        private final double targetPrice;
        private final double stopLossPrice;
        private final double riskAmount;
        private final double rewardAmount;
        private final double riskRewardRatio;
        private final double successProbability;
        private final String recommendation;
        
        public RiskRewardAnalysis(double entryPrice, double targetPrice, double stopLossPrice, 
                                double successProbability) {
            this.entryPrice = entryPrice;
            this.targetPrice = targetPrice;
            this.stopLossPrice = stopLossPrice;
            this.successProbability = successProbability;
            
            this.riskAmount = Math.abs(entryPrice - stopLossPrice);
            this.rewardAmount = Math.abs(targetPrice - entryPrice);
            this.riskRewardRatio = rewardAmount / riskAmount;
            this.recommendation = calculateRecommendation();
        }
        
        private String calculateRecommendation() {
            if (riskRewardRatio >= 3.0 && successProbability >= 0.8) {
                return "STRONG_BUY";
            } else if (riskRewardRatio >= 2.0 && successProbability >= 0.7) {
                return "BUY";
            } else if (riskRewardRatio >= 1.5 && successProbability >= 0.6) {
                return "MODERATE_BUY";
            } else if (riskRewardRatio < 1.0 || successProbability < 0.5) {
                return "AVOID";
            } else {
                return "HOLD";
            }
        }
        
        // Getters
        public double getEntryPrice() { return entryPrice; }
        public double getTargetPrice() { return targetPrice; }
        public double getStopLossPrice() { return stopLossPrice; }
        public double getRiskAmount() { return riskAmount; }
        public double getRewardAmount() { return rewardAmount; }
        public double getRiskRewardRatio() { return riskRewardRatio; }
        public double getSuccessProbability() { return successProbability; }
        public String getRecommendation() { return recommendation; }
        
        @Override
        public String toString() {
            return String.format("RR Analysis: Entry=%.2f, Target=%.2f, Stop=%.2f, Ratio=1:%.1f, Probability=%.0f%%, Rec=%s",
                    entryPrice, targetPrice, stopLossPrice, riskRewardRatio, successProbability * 100, recommendation);
        }
    }
    
    public static class PositionSizing {
        private final double accountBalance;
        private final double riskPercentage;
        private final double riskAmount;
        private final double positionSize;
        private final double maxPositionSize;
        
        public PositionSizing(double accountBalance, double riskPercentage, double riskAmount) {
            this.accountBalance = accountBalance;
            this.riskPercentage = riskPercentage;
            this.riskAmount = riskAmount;
            this.positionSize = (accountBalance * riskPercentage) / riskAmount;
            this.maxPositionSize = accountBalance * 0.1; // Max 10% of account
        }
        
        public double getRecommendedSize() {
            return Math.min(positionSize, maxPositionSize);
        }
        
        // Getters
        public double getAccountBalance() { return accountBalance; }
        public double getRiskPercentage() { return riskPercentage; }
        public double getRiskAmount() { return riskAmount; }
        public double getPositionSize() { return positionSize; }
        public double getMaxPositionSize() { return maxPositionSize; }
    }
    
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicLong totalCalculations = new AtomicLong(0);
    private final AtomicLong successfulTrades = new AtomicLong(0);
    private final AtomicLong failedTrades = new AtomicLong(0);
    
    // Configuration
    private double defaultRiskPercent = 0.02; // 2% risk per trade
    private double minRiskRewardRatio = 1.5; // Minimum 1.5:1 ratio
    private double maxRiskPercent = 0.05; // Maximum 5% risk per trade
    
    public RiskRewardCalculator() {
        System.out.println("💰 [RiskRewardCalculator] Initializing Risk Reward Calculator...");
    }
    
    public void initialize() {
        isRunning.set(true);
        System.out.println("💰 [RiskRewardCalculator] Calculator initialized and running");
    }
    
    public RiskRewardAnalysis calculateRiskReward(double entryPrice, double currentPrice,
                                                Map<String, Double> indicators,
                                                String patternType) {
        if (!isRunning.get()) {
            return null;
        }
        
        try {
            // Calculate target and stop loss based on pattern type and indicators
            double[] levels = calculateTradingLevels(entryPrice, currentPrice, indicators, patternType);
            double targetPrice = levels[0];
            double stopLossPrice = levels[1];
            
            // Calculate success probability based on indicators and pattern
            double successProbability = calculateSuccessProbability(indicators, patternType);
            
            RiskRewardAnalysis analysis = new RiskRewardAnalysis(entryPrice, targetPrice, 
                                                               stopLossPrice, successProbability);
            
            totalCalculations.incrementAndGet();
            
            System.out.println("💰 [RiskRewardCalculator] " + analysis.toString());
            
            return analysis;
            
        } catch (Exception e) {
            System.err.println("💰 [RiskRewardCalculator] Error calculating risk/reward: " + e.getMessage());
            return null;
        }
    }
    
    private double[] calculateTradingLevels(double entryPrice, double currentPrice,
                                          Map<String, Double> indicators, String patternType) {
        double volatility = indicators.getOrDefault("Volatility", 0.001);
        double atr = indicators.getOrDefault("ATR", entryPrice * 0.01); // Default 1% ATR
        
        double targetDistance;
        double stopDistance;
        
        switch (patternType) {
            case "PERFECT_STORM_NQ":
                targetDistance = atr * 3.0; // 3x ATR target
                stopDistance = atr * 1.0;   // 1x ATR stop
                break;
            case "ICEBERG_PATTERN":
                targetDistance = atr * 2.5; // 2.5x ATR target
                stopDistance = atr * 1.0;   // 1x ATR stop
                break;
            case "ABSORPTION_PATTERN":
                targetDistance = atr * 2.0; // 2x ATR target
                stopDistance = atr * 0.8;   // 0.8x ATR stop
                break;
            default:
                targetDistance = atr * 2.0; // Default 2x ATR
                stopDistance = atr * 1.0;   // Default 1x ATR
        }
        
        // Adjust for market direction
        boolean isLong = currentPrice >= entryPrice;
        
        double targetPrice = isLong ? entryPrice + targetDistance : entryPrice - targetDistance;
        double stopLossPrice = isLong ? entryPrice - stopDistance : entryPrice + stopDistance;
        
        return new double[]{targetPrice, stopLossPrice};
    }
    
    private double calculateSuccessProbability(Map<String, Double> indicators, String patternType) {
        double baseProbability = getPatternBaseProbability(patternType);
        
        // Adjust based on indicators
        double rsi = indicators.getOrDefault("RSI", 50.0);
        double volume = indicators.getOrDefault("Volume", 100.0);
        double avgVolume = indicators.getOrDefault("AvgVolume", 100.0);
        
        double adjustmentFactor = 1.0;
        
        // RSI adjustment
        if (rsi > 70 || rsi < 30) {
            adjustmentFactor += 0.1; // Extreme RSI levels increase probability
        }
        
        // Volume adjustment
        double volumeRatio = volume / avgVolume;
        if (volumeRatio > 2.0) {
            adjustmentFactor += 0.15; // High volume increases probability
        } else if (volumeRatio < 0.5) {
            adjustmentFactor -= 0.1; // Low volume decreases probability
        }
        
        // Market session adjustment
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if ((hour >= 13 && hour <= 17) || (hour >= 8 && hour <= 12)) {
            adjustmentFactor += 0.05; // Active trading sessions
        }
        
        double finalProbability = baseProbability * adjustmentFactor;
        return Math.max(0.1, Math.min(0.95, finalProbability)); // Bound between 10% and 95%
    }
    
    private double getPatternBaseProbability(String patternType) {
        switch (patternType) {
            case "PERFECT_STORM_NQ": return 0.85;
            case "ICEBERG_PATTERN": return 0.80;
            case "ABSORPTION_PATTERN": return 0.75;
            case "REVERSAL_PATTERN": return 0.70;
            case "SWEEP_PATTERN": return 0.78;
            default: return 0.65;
        }
    }
    
    public PositionSizing calculatePositionSize(double accountBalance, double entryPrice, 
                                              double stopLossPrice, double riskPercent) {
        if (riskPercent == 0) {
            riskPercent = defaultRiskPercent;
        }
        
        // Bound risk percentage
        riskPercent = Math.max(0.001, Math.min(maxRiskPercent, riskPercent));
        
        double riskAmount = Math.abs(entryPrice - stopLossPrice);
        
        return new PositionSizing(accountBalance, riskPercent, riskAmount);
    }
    
    public boolean isTradeWorthTaking(RiskRewardAnalysis analysis) {
        if (analysis == null) return false;
        
        return analysis.getRiskRewardRatio() >= minRiskRewardRatio && 
               analysis.getSuccessProbability() >= 0.6 &&
               !analysis.getRecommendation().equals("AVOID");
    }
    
    public void recordTradeOutcome(RiskRewardAnalysis analysis, boolean successful) {
        if (successful) {
            successfulTrades.incrementAndGet();
        } else {
            failedTrades.incrementAndGet();
        }
        
        System.out.println(String.format("💰 [RiskRewardCalculator] Trade outcome recorded: %s (Total success rate: %.1f%%)",
                successful ? "SUCCESS" : "FAILURE", getSuccessRate()));
    }
    
    public double getSuccessRate() {
        long total = successfulTrades.get() + failedTrades.get();
        return total > 0 ? (double) successfulTrades.get() / total * 100.0 : 0.0;
    }
    
    // Configuration methods
    public void setDefaultRiskPercent(double riskPercent) {
        this.defaultRiskPercent = Math.max(0.001, Math.min(0.1, riskPercent));
        System.out.println("💰 [RiskRewardCalculator] Default risk percentage set to: " + 
                          (this.defaultRiskPercent * 100) + "%");
    }
    
    public void setMinRiskRewardRatio(double ratio) {
        this.minRiskRewardRatio = Math.max(1.0, ratio);
        System.out.println("💰 [RiskRewardCalculator] Minimum risk/reward ratio set to: 1:" + ratio);
    }
    
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("is_running", isRunning.get());
        stats.put("total_calculations", totalCalculations.get());
        stats.put("successful_trades", successfulTrades.get());
        stats.put("failed_trades", failedTrades.get());
        stats.put("success_rate", getSuccessRate());
        stats.put("default_risk_percent", defaultRiskPercent * 100);
        stats.put("min_risk_reward_ratio", minRiskRewardRatio);
        return stats;
    }
    
    public void shutdown() {
        isRunning.set(false);
        System.out.println("💰 [RiskRewardCalculator] Calculator shutdown completed");
    }
} 