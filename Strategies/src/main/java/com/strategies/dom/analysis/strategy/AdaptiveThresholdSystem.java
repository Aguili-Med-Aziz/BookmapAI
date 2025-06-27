package com.strategies.dom.analysis.strategy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Map;
import java.util.Deque;

public class AdaptiveThresholdSystem {
    
    // ========== ADAPTIVE TIMEFRAME SYSTEM ==========
    public enum TimeFrame {
        FRAME_15M(15 * 60 * 1000, "15m"),    // 15 minutes
        FRAME_30M(30 * 60 * 1000, "30m");   // 30 minutes
        
        private final long durationMs;
        private final String label;
        
        TimeFrame(long durationMs, String label) {
            this.durationMs = durationMs;
            this.label = label;
        }
        
        public long getDurationMs() { return durationMs; }
        public String getLabel() { return label; }
    }
    
    // ========== MARKET AVERAGES TRACKER ==========
    private static class MarketAverages {
        private final Deque<Double> volumeWindow = new ConcurrentLinkedDeque<>();
        private final Deque<Double> absorptionWindow = new ConcurrentLinkedDeque<>();
        private final Deque<Double> deltaWindow = new ConcurrentLinkedDeque<>();
        private final Deque<Double> liquidityWindow = new ConcurrentLinkedDeque<>();
        private final Deque<Double> volatilityWindow = new ConcurrentLinkedDeque<>();
        
        private final int maxWindowSize = 500; // Last 500 readings
        
        // Calculated averages
        private volatile double avgVolume = 100.0;
        private volatile double avgAbsorption = 80.0;
        private volatile double avgDelta = 50.0;
        private volatile double avgLiquidity = 1000.0;
        private volatile double avgVolatility = 0.001;
        
        public void updateAverages(double volume, double absorption, double delta, 
                                 double liquidity, double volatility) {
            // Update data windows
            updateWindow(volumeWindow, volume);
            updateWindow(absorptionWindow, absorption);
            updateWindow(deltaWindow, delta);
            updateWindow(liquidityWindow, liquidity);
            updateWindow(volatilityWindow, volatility);
            
            // Recalculate averages
            avgVolume = calculateAverage(volumeWindow);
            avgAbsorption = calculateAverage(absorptionWindow);
            avgDelta = calculateAverage(deltaWindow);
            avgLiquidity = calculateAverage(liquidityWindow);
            avgVolatility = calculateAverage(volatilityWindow);
        }
        
        private void updateWindow(Deque<Double> window, double value) {
            if (window.size() >= maxWindowSize) {
                window.removeFirst();
            }
            window.addLast(value);
        }
        
        private double calculateAverage(Deque<Double> window) {
            if (window.isEmpty()) return 0.0;
            return window.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }
        
        // Getters
        public double getAvgVolume() { return avgVolume; }
        public double getAvgAbsorption() { return avgAbsorption; }
        public double getAvgDelta() { return avgDelta; }
        public double getAvgLiquidity() { return avgLiquidity; }
        public double getAvgVolatility() { return avgVolatility; }
    }
    
    // ========== DYNAMIC THRESHOLDS ==========
    public static class DynamicThresholds {
        // Adaptive Iceberg thresholds
        private volatile double icebergMinVolume = 50.0;
        private volatile double icebergOrderFactor = 0.15;
        private volatile long icebergDetectionWindow = 15 * 60 * 1000; // 15 minutes
        
        // Adaptive Absorption thresholds
        private volatile double absorptionMinVolume = 80.0;
        private volatile double absorptionSpeedFactor = 0.5;
        private volatile long absorptionDetectionWindow = 15 * 60 * 1000; // 15 minutes
        
        // Adaptive Heatmap thresholds
        private volatile double heatmapThreshold = 0.30;
        private volatile double heatmapColorThreshold = 0.7;
        private volatile long heatmapAnalysisWindow = 15 * 60 * 1000; // 15 minutes
        
        // Adaptive Delta Imbalance thresholds
        private volatile double deltaImbalanceThreshold = 0.05;
        private volatile int deltaMinRatio = 2;
        private volatile long deltaAnalysisWindow = 15 * 60 * 1000; // 15 minutes
        
        // Adaptive VWAP thresholds
        private volatile int vwapNeutralZoneTicks = 50;
        private volatile double vwapDistanceMin = 0.0002;
        private volatile double vwapDistanceMax = 0.002;
        
        // Adaptive Sweep thresholds (new)
        private volatile double sweepMinVolume = 100.0;
        private volatile long sweepDetectionWindow = 10 * 60 * 1000; // 10 minutes
        private volatile double sweepLiquidityThreshold = 0.8;
        
        // Adaptive Volume Profile thresholds (new)
        private volatile double vpocThreshold = 0.6;
        private volatile long vpocAnalysisWindow = 30 * 60 * 1000; // 30 minutes
        
        // Update thresholds based on market averages
        public void adaptToMarket(MarketAverages averages, double trendStrength, 
                                double volatility, TimeFrame timeFrame) {
            
            // Adapt Iceberg thresholds
            adaptIcebergThresholds(averages, trendStrength, volatility, timeFrame);
            
            // Adapt Absorption thresholds
            adaptAbsorptionThresholds(averages, trendStrength, volatility, timeFrame);
            
            // Adapt Heatmap thresholds
            adaptHeatmapThresholds(averages, trendStrength, volatility, timeFrame);
            
            // Adapt Delta thresholds
            adaptDeltaThresholds(averages, trendStrength, volatility, timeFrame);
            
            // Adapt VWAP thresholds
            adaptVwapThresholds(averages, trendStrength, volatility, timeFrame);
            
            // Adapt new tool thresholds
            adaptSweepThresholds(averages, trendStrength, volatility, timeFrame);
            adaptVolumeProfileThresholds(averages, trendStrength, volatility, timeFrame);
        }
        
        private void adaptIcebergThresholds(MarketAverages avg, double trendStrength, 
                                          double volatility, TimeFrame timeFrame) {
            // Adjust minimum Iceberg volume based on market average
            double baseVolume = avg.getAvgVolume() * 1.5; // 150% of average
            
            // Adjust for trend strength
            double trendFactor = 1.0 + (trendStrength / 100.0);
            
            // Adjust for volatility
            double volatilityFactor = 1.0 + volatility;
            
            // Adjust for timeframe
            double timeFrameFactor = timeFrame == TimeFrame.FRAME_30M ? 1.5 : 1.0;
            
            icebergMinVolume = baseVolume * trendFactor * volatilityFactor * timeFrameFactor;
            
            // Adjust detection window based on timeframe
            icebergDetectionWindow = timeFrame.getDurationMs();
            
            // Adjust order factor based on volatility
            icebergOrderFactor = Math.max(0.10, Math.min(0.25, 0.15 * volatilityFactor));
        }
        
        private void adaptAbsorptionThresholds(MarketAverages avg, double trendStrength, 
                                             double volatility, TimeFrame timeFrame) {
            // Calculate basic absorption threshold
            double baseAbsorption = avg.getAvgAbsorption() * 1.3; // 130% of average
            
            // Adjust for trend strength (strong trend needs bigger absorption)
            double trendAdjustment = 1.0 + (trendStrength / 100.0) * 0.5;
            
            // Adjust for volatility
            double volatilityAdjustment = 1.0 + volatility * 0.8;
            
            // Adjust for timeframe
            double timeFrameAdjustment = timeFrame == TimeFrame.FRAME_30M ? 1.8 : 1.0;
            
            absorptionMinVolume = baseAbsorption * trendAdjustment * volatilityAdjustment * timeFrameAdjustment;
            
            // Adjust speed factor
            absorptionSpeedFactor = Math.max(0.3, Math.min(0.8, 0.5 * (1.0 + volatility)));
            
            // Adjust detection window
            absorptionDetectionWindow = timeFrame.getDurationMs();
        }
        
        private void adaptHeatmapThresholds(MarketAverages avg, double trendStrength, 
                                          double volatility, TimeFrame timeFrame) {
            // Adjust basic heatmap threshold
            double baseThreshold = 0.30;
            
            // Adjust for market activity
            double activityFactor = avg.getAvgLiquidity() / 1000.0; // Liquidity metric
            
            // Adjust for volatility
            double volatilityAdjustment = 1.0 + volatility * 0.5;
            
            // Adjust for timeframe
            double timeFrameAdjustment = timeFrame == TimeFrame.FRAME_30M ? 0.8 : 1.0;
            
            heatmapThreshold = baseThreshold * activityFactor * volatilityAdjustment * timeFrameAdjustment;
            heatmapThreshold = Math.max(0.20, Math.min(0.50, heatmapThreshold));
            
            // Adjust color threshold
            heatmapColorThreshold = Math.max(0.60, Math.min(0.85, 0.70 * volatilityAdjustment));
            
            // Adjust analysis window
            heatmapAnalysisWindow = timeFrame.getDurationMs();
        }
        
        private void adaptDeltaThresholds(MarketAverages avg, double trendStrength, 
                                        double volatility, TimeFrame timeFrame) {
            // Adjust imbalance threshold
            double baseDelta = 0.05;
            
            // Adjust for average delta in market
            double deltaFactor = avg.getAvgDelta() / 50.0;
            
            // Adjust for volatility
            double volatilityAdjustment = 1.0 + volatility * 0.3;
            
            // Adjust for timeframe
            double timeFrameAdjustment = timeFrame == TimeFrame.FRAME_30M ? 0.7 : 1.0;
            
            deltaImbalanceThreshold = baseDelta * deltaFactor * volatilityAdjustment * timeFrameAdjustment;
            deltaImbalanceThreshold = Math.max(0.03, Math.min(0.10, deltaImbalanceThreshold));
            
            // Adjust minimum ratio
            deltaMinRatio = (int) Math.max(1.5, Math.min(3.0, 2.0 * volatilityAdjustment));
            
            // Adjust analysis window
            deltaAnalysisWindow = timeFrame.getDurationMs();
        }
        
        private void adaptVwapThresholds(MarketAverages avg, double trendStrength, 
                                       double volatility, TimeFrame timeFrame) {
            // Adjust neutral zone
            double baseNeutralZone = 50.0;
            double volatilityAdjustment = 1.0 + volatility * 2.0;
            double timeFrameAdjustment = timeFrame == TimeFrame.FRAME_30M ? 1.5 : 1.0;
            
            vwapNeutralZoneTicks = (int) (baseNeutralZone * volatilityAdjustment * timeFrameAdjustment);
            vwapNeutralZoneTicks = Math.max(30, Math.min(100, vwapNeutralZoneTicks));
            
            // Adjust distances
            vwapDistanceMin = 0.0002 * volatilityAdjustment;
            vwapDistanceMax = 0.002 * volatilityAdjustment * timeFrameAdjustment;
        }
        
        private void adaptSweepThresholds(MarketAverages avg, double trendStrength, 
                                        double volatility, TimeFrame timeFrame) {
            // New Sweep thresholds
            double baseSweepVolume = avg.getAvgVolume() * 2.0; // Double the average
            double volatilityAdjustment = 1.0 + volatility;
            double timeFrameAdjustment = timeFrame == TimeFrame.FRAME_30M ? 2.0 : 1.0;
            
            sweepMinVolume = baseSweepVolume * volatilityAdjustment * timeFrameAdjustment;
            sweepDetectionWindow = timeFrame.getDurationMs() / 3; // Third of timeframe period
            sweepLiquidityThreshold = Math.max(0.6, Math.min(0.9, 0.8 * (1.0 + volatility * 0.3)));
        }
        
        private void adaptVolumeProfileThresholds(MarketAverages avg, double trendStrength, 
                                                double volatility, TimeFrame timeFrame) {
            // New Volume Profile thresholds
            double baseVpoc = 0.6;
            double activityAdjustment = avg.getAvgLiquidity() / 1000.0;
            double timeFrameAdjustment = timeFrame == TimeFrame.FRAME_30M ? 0.8 : 1.0;
            
            vpocThreshold = baseVpoc * activityAdjustment * timeFrameAdjustment;
            vpocThreshold = Math.max(0.4, Math.min(0.8, vpocThreshold));
            vpocAnalysisWindow = timeFrame.getDurationMs();
        }
        
        // Getters for all thresholds
        public double getIcebergMinVolume() { return icebergMinVolume; }
        public double getIcebergOrderFactor() { return icebergOrderFactor; }
        public long getIcebergDetectionWindow() { return icebergDetectionWindow; }
        
        public double getAbsorptionMinVolume() { return absorptionMinVolume; }
        public double getAbsorptionSpeedFactor() { return absorptionSpeedFactor; }
        public long getAbsorptionDetectionWindow() { return absorptionDetectionWindow; }
        
        public double getHeatmapThreshold() { return heatmapThreshold; }
        public double getHeatmapColorThreshold() { return heatmapColorThreshold; }
        public long getHeatmapAnalysisWindow() { return heatmapAnalysisWindow; }
        
        public double getDeltaImbalanceThreshold() { return deltaImbalanceThreshold; }
        public int getDeltaMinRatio() { return deltaMinRatio; }
        public long getDeltaAnalysisWindow() { return deltaAnalysisWindow; }
        
        public int getVwapNeutralZoneTicks() { return vwapNeutralZoneTicks; }
        public double getVwapDistanceMin() { return vwapDistanceMin; }
        public double getVwapDistanceMax() { return vwapDistanceMax; }
        
        public double getSweepMinVolume() { return sweepMinVolume; }
        public long getSweepDetectionWindow() { return sweepDetectionWindow; }
        public double getSweepLiquidityThreshold() { return sweepLiquidityThreshold; }
        
        public double getVpocThreshold() { return vpocThreshold; }
        public long getVpocAnalysisWindow() { return vpocAnalysisWindow; }
    }
    
    // ========== TIMEFRAME MANAGER ==========
    private static class TimeFrameManager {
        private final Map<TimeFrame, Long> lastUpdateTimes = new ConcurrentHashMap<>();
        private final Map<TimeFrame, MarketAverages> timeFrameAverages = new ConcurrentHashMap<>();
        private final Map<TimeFrame, DynamicThresholds> timeFrameThresholds = new ConcurrentHashMap<>();
        
        public TimeFrameManager() {
            // Initialize for each timeframe
            for (TimeFrame tf : TimeFrame.values()) {
                lastUpdateTimes.put(tf, 0L);
                timeFrameAverages.put(tf, new MarketAverages());
                timeFrameThresholds.put(tf, new DynamicThresholds());
            }
        }
        
        public boolean shouldUpdateTimeFrame(TimeFrame timeFrame, long currentTime) {
            long lastUpdate = lastUpdateTimes.get(timeFrame);
            return (currentTime - lastUpdate) >= timeFrame.getDurationMs();
        }
        
        public void updateTimeFrame(TimeFrame timeFrame, long currentTime, 
                                  double volume, double absorption, double delta, 
                                  double liquidity, double volatility, 
                                  double trendStrength) {
            
            // Update averages
            MarketAverages averages = timeFrameAverages.get(timeFrame);
            averages.updateAverages(volume, absorption, delta, liquidity, volatility);
            
            // Update thresholds
            DynamicThresholds thresholds = timeFrameThresholds.get(timeFrame);
            thresholds.adaptToMarket(averages, trendStrength, volatility, timeFrame);
            
            // Update last update time
            lastUpdateTimes.put(timeFrame, currentTime);
        }
        
        public DynamicThresholds getThresholds(TimeFrame timeFrame) {
            return timeFrameThresholds.get(timeFrame);
        }
        
        public MarketAverages getAverages(TimeFrame timeFrame) {
            return timeFrameAverages.get(timeFrame);
        }
    }
    
    // ========== MAIN CONTROL ==========
    private final MarketAverages globalAverages = new MarketAverages();
    private final TimeFrameManager timeFrameManager = new TimeFrameManager();
    private final AtomicReference<TimeFrame> primaryTimeFrame = new AtomicReference<>(TimeFrame.FRAME_15M);
    private final AtomicReference<TimeFrame> confirmationTimeFrame = new AtomicReference<>(TimeFrame.FRAME_30M);
    
    // Update main system
    public void updateSystem(long currentTime, double volume, double absorption, 
                           double delta, double liquidity, double volatility, 
                           double trendStrength) {
        
        // Update global averages
        globalAverages.updateAverages(volume, absorption, delta, liquidity, volatility);
        
        // Update each timeframe if needed
        for (TimeFrame tf : TimeFrame.values()) {
            if (timeFrameManager.shouldUpdateTimeFrame(tf, currentTime)) {
                timeFrameManager.updateTimeFrame(tf, currentTime, volume, absorption, 
                                               delta, liquidity, volatility, trendStrength);
            }
        }
    }
    
    // Get thresholds for primary timeframe
    public DynamicThresholds getPrimaryThresholds() {
        return timeFrameManager.getThresholds(primaryTimeFrame.get());
    }
    
    // Get thresholds for confirmation timeframe
    public DynamicThresholds getConfirmationThresholds() {
        return timeFrameManager.getThresholds(confirmationTimeFrame.get());
    }
    
    // Change primary timeframe
    public void setPrimaryTimeFrame(TimeFrame timeFrame) {
        primaryTimeFrame.set(timeFrame);
    }
    
    // Change confirmation timeframe
    public void setConfirmationTimeFrame(TimeFrame timeFrame) {
        confirmationTimeFrame.set(timeFrame);
    }
    
    // Check if signals align between timeframes
    public boolean areTimeFramesAligned(String signalType, double strength15m, double strength30m) {
        // Signals must be in same direction with appropriate strength
        boolean sameDirection = (strength15m > 0 && strength30m > 0) || (strength15m < 0 && strength30m < 0);
        boolean strongEnough = Math.abs(strength15m) >= 0.6 && Math.abs(strength30m) >= 0.6;
        
        return sameDirection && strongEnough;
    }
    
    // Calculate combined signal strength
    public double calculateCombinedSignalStrength(double strength15m, double strength30m) {
        if (!areTimeFramesAligned("", strength15m, strength30m)) {
            return 0.0; // No signal if not aligned
        }
        
        // Weight: 40% for 15m, 60% for 30m
        return (strength15m * 0.4) + (strength30m * 0.6);
    }
    
    // System statistics
    public String getSystemStats() {
        DynamicThresholds primary = getPrimaryThresholds();
        DynamicThresholds confirmation = getConfirmationThresholds();
        
        StringBuilder stats = new StringBuilder();
        stats.append("=== Adaptive Threshold System Stats ===\n");
        stats.append("Primary TimeFrame (15m):\n");
        stats.append(String.format("  Iceberg Min Volume: %.1f\n", primary.getIcebergMinVolume()));
        stats.append(String.format("  Absorption Min Volume: %.1f\n", primary.getAbsorptionMinVolume()));
        stats.append(String.format("  Heatmap Threshold: %.3f\n", primary.getHeatmapThreshold()));
        stats.append(String.format("  Delta Threshold: %.3f\n", primary.getDeltaImbalanceThreshold()));
        
        stats.append("\nConfirmation TimeFrame (30m):\n");
        stats.append(String.format("  Iceberg Min Volume: %.1f\n", confirmation.getIcebergMinVolume()));
        stats.append(String.format("  Absorption Min Volume: %.1f\n", confirmation.getAbsorptionMinVolume()));
        stats.append(String.format("  Heatmap Threshold: %.3f\n", confirmation.getHeatmapThreshold()));
        stats.append(String.format("  Delta Threshold: %.3f\n", confirmation.getDeltaImbalanceThreshold()));
        
        return stats.toString();
    }
} 