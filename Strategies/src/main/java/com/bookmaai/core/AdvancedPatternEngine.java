package com.bookmaai.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.Collectors;

/**
 * 🎯 Advanced Pattern Engine - محرك تحليل الأنماط الرئيسي
 * 
 * Enterprise-grade pattern detection engine that identifies and validates
 * trading patterns with AI-enhanced analysis and multi-timeframe confirmation.
 */
public class AdvancedPatternEngine {
    
    // Pattern Types
    public enum PatternType {
        PERFECT_STORM_NQ("Perfect Storm NQ", 95, 7),
        REVERSAL_PATTERN("Reversal Pattern", 85, 5),
        ICEBERG_PATTERN("Iceberg Pattern", 90, 6),
        ABSORPTION_PATTERN("Absorption Pattern", 88, 5),
        SWEEP_PATTERN("Sweep Pattern", 92, 6),
        DELTA_IMBALANCE("Delta Imbalance", 80, 4),
        VOLUME_SPIKE("Volume Spike", 75, 3),
        LIQUIDITY_HUNT("Liquidity Hunt", 85, 5);
        
        private final String displayName;
        private final int minConfidence;
        private final int stages;
        
        PatternType(String displayName, int minConfidence, int stages) {
            this.displayName = displayName;
            this.minConfidence = minConfidence;
            this.stages = stages;
        }
        
        public String getDisplayName() { return displayName; }
        public int getMinConfidence() { return minConfidence; }
        public int getStages() { return stages; }
    }
    
    // Pattern Stages
    public enum PatternStage {
        INITIATING(0, 20, "بدء التكوين"),
        FORMING(20, 40, "التشكيل"),
        DEVELOPING(40, 60, "التطوير"),
        MATURING(60, 80, "النضج"),
        CONFIRMING(80, 95, "التأكيد"),
        COMPLETED(95, 100, "مكتمل"),
        SUCCESSFUL(100, 100, "ناجح"),
        FAILED(-1, -1, "فاشل");
        
        private final int minProgress;
        private final int maxProgress;
        private final String arabicName;
        
        PatternStage(int minProgress, int maxProgress, String arabicName) {
            this.minProgress = minProgress;
            this.maxProgress = maxProgress;
            this.arabicName = arabicName;
        }
        
        public int getMinProgress() { return minProgress; }
        public int getMaxProgress() { return maxProgress; }
        public String getArabicName() { return arabicName; }
        
        public static PatternStage fromProgress(int progress) {
            for (PatternStage stage : values()) {
                if (progress >= stage.minProgress && progress <= stage.maxProgress) {
                    return stage;
                }
            }
            return FAILED;
        }
    }
    
    // Detected Pattern Data
    public static class DetectedPattern {
        private final String id;
        private final PatternType type;
        private final String symbol;
        private final double price;
        private final long timestamp;
        private final AtomicInteger confidence = new AtomicInteger(0);
        private final AtomicInteger progress = new AtomicInteger(0);
        private final AtomicReference<PatternStage> stage = new AtomicReference<>(PatternStage.INITIATING);
        private final Map<String, Double> indicators = new ConcurrentHashMap<>();
        private final List<String> tools = new CopyOnWriteArrayList<>();
        private final AtomicBoolean isActive = new AtomicBoolean(true);
        private final AtomicLong lastUpdate = new AtomicLong(System.currentTimeMillis());
        
        public DetectedPattern(PatternType type, String symbol, double price) {
            this.id = generatePatternId(type, symbol);
            this.type = type;
            this.symbol = symbol;
            this.price = price;
            this.timestamp = System.currentTimeMillis();
        }
        
        private String generatePatternId(PatternType type, String symbol) {
            return String.format("%s_%s_%d", type.name(), symbol, System.currentTimeMillis());
        }
        
        // Getters and setters
        public String getId() { return id; }
        public PatternType getType() { return type; }
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public long getTimestamp() { return timestamp; }
        public int getConfidence() { return confidence.get(); }
        public int getProgress() { return progress.get(); }
        public PatternStage getStage() { return stage.get(); }
        public Map<String, Double> getIndicators() { return new HashMap<>(indicators); }
        public List<String> getTools() { return new ArrayList<>(tools); }
        public boolean isActive() { return isActive.get(); }
        public long getLastUpdate() { return lastUpdate.get(); }
        
        public void updateConfidence(int newConfidence) {
            confidence.set(Math.max(0, Math.min(100, newConfidence)));
            lastUpdate.set(System.currentTimeMillis());
        }
        
        public void updateProgress(int newProgress) {
            progress.set(Math.max(0, Math.min(100, newProgress)));
            stage.set(PatternStage.fromProgress(newProgress));
            lastUpdate.set(System.currentTimeMillis());
        }
        
        public void addIndicator(String name, double value) {
            indicators.put(name, value);
            lastUpdate.set(System.currentTimeMillis());
        }
        
        public void addActiveTool(String tool) {
            if (!tools.contains(tool)) {
                tools.add(tool);
            }
            lastUpdate.set(System.currentTimeMillis());
        }
        
        public void complete() {
            updateProgress(100);
            isActive.set(false);
        }
        
        public void fail() {
            stage.set(PatternStage.FAILED);
            isActive.set(false);
            lastUpdate.set(System.currentTimeMillis());
        }
        
        @Override
        public String toString() {
            return String.format("Pattern[%s]: %s on %s - Stage: %s (%d%%), Confidence: %d%%, Tools: %d",
                    id, type.getDisplayName(), symbol, stage.get().getArabicName(), 
                    progress.get(), confidence.get(), tools.size());
        }
    }
    
    // Engine State
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final Map<String, DetectedPattern> activePatterns = new ConcurrentHashMap<>();
    private final Map<String, List<DetectedPattern>> patternHistory = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    // Dependencies
    private AdaptiveLearningSystem learningSystem;
    private SystemStatusMonitor statusMonitor;
    private PatternLearningLogger learningLogger;
    
    // Statistics
    private final AtomicLong totalPatternsDetected = new AtomicLong(0);
    private final AtomicLong successfulPatterns = new AtomicLong(0);
    private final AtomicLong failedPatterns = new AtomicLong(0);
    
    public AdvancedPatternEngine() {
        System.out.println("🎯 [AdvancedPatternEngine] Initializing Advanced Pattern Engine...");
    }
    
    /**
     * Initialize the pattern engine with dependencies
     */
    public void initialize(AdaptiveLearningSystem learningSystem, 
                          SystemStatusMonitor statusMonitor,
                          PatternLearningLogger learningLogger) {
        this.learningSystem = learningSystem;
        this.statusMonitor = statusMonitor;
        this.learningLogger = learningLogger;
        
        // Start pattern monitoring
        startPatternMonitoring();
        isRunning.set(true);
        
        System.out.println("🎯 [AdvancedPatternEngine] Engine initialized and running");
        if (statusMonitor != null) {
            statusMonitor.updateComponentStatus("AdvancedPatternEngine", true);
        }
    }
    
    /**
     * Analyze market data for pattern detection
     */
    public List<DetectedPattern> analyzeMarketData(String symbol, double price, 
                                                  double volume, double vwap,
                                                  Map<String, Double> indicators) {
        if (!isRunning.get()) {
            return Collections.emptyList();
        }
        
        List<DetectedPattern> newPatterns = new ArrayList<>();
        
        try {
            // Check for different pattern types
            for (PatternType patternType : PatternType.values()) {
                DetectedPattern pattern = detectPattern(patternType, symbol, price, volume, vwap, indicators);
                if (pattern != null) {
                    newPatterns.add(pattern);
                    activePatterns.put(pattern.getId(), pattern);
                    totalPatternsDetected.incrementAndGet();
                    
                    // Log pattern detection
                    if (learningLogger != null) {
                        learningLogger.logPatternDetection(pattern);
                    }
                    
                    System.out.println("🎯 [AdvancedPatternEngine] New pattern detected: " + pattern);
                }
            }
            
            // Update existing patterns
            updateExistingPatterns(symbol, price, volume, vwap, indicators);
            
        } catch (Exception e) {
            System.err.println("🎯 [AdvancedPatternEngine] Error analyzing market data: " + e.getMessage());
        }
        
        return newPatterns;
    }
    
    /**
     * Detect specific pattern type
     */
    private DetectedPattern detectPattern(PatternType patternType, String symbol, 
                                        double price, double volume, double vwap,
                                        Map<String, Double> indicators) {
        
        boolean patternDetected = false;
        int initialConfidence = 0;
        
        switch (patternType) {
            case PERFECT_STORM_NQ:
                patternDetected = detectPerfectStormNQ(price, volume, vwap, indicators);
                initialConfidence = patternDetected ? 85 : 0;
                break;
                
            case REVERSAL_PATTERN:
                patternDetected = detectReversalPattern(price, volume, vwap, indicators);
                initialConfidence = patternDetected ? 80 : 0;
                break;
                
            case ICEBERG_PATTERN:
                patternDetected = detectIcebergPattern(volume, indicators);
                initialConfidence = patternDetected ? 88 : 0;
                break;
                
            case ABSORPTION_PATTERN:
                patternDetected = detectAbsorptionPattern(volume, indicators);
                initialConfidence = patternDetected ? 85 : 0;
                break;
                
            case SWEEP_PATTERN:
                patternDetected = detectSweepPattern(price, volume, indicators);
                initialConfidence = patternDetected ? 90 : 0;
                break;
                
            case DELTA_IMBALANCE:
                patternDetected = detectDeltaImbalance(indicators);
                initialConfidence = patternDetected ? 75 : 0;
                break;
                
            case VOLUME_SPIKE:
                patternDetected = detectVolumeSpike(volume, indicators);
                initialConfidence = patternDetected ? 70 : 0;
                break;
                
            case LIQUIDITY_HUNT:
                patternDetected = detectLiquidityHunt(price, volume, indicators);
                initialConfidence = patternDetected ? 82 : 0;
                break;
        }
        
        if (patternDetected && initialConfidence >= patternType.getMinConfidence()) {
            DetectedPattern pattern = new DetectedPattern(patternType, symbol, price);
            pattern.updateConfidence(initialConfidence);
            pattern.updateProgress(15); // Initial progress
            
            // Add relevant indicators
            for (Map.Entry<String, Double> entry : indicators.entrySet()) {
                pattern.addIndicator(entry.getKey(), entry.getValue());
            }
            
            return pattern;
        }
        
        return null;
    }
    
    // Pattern Detection Methods
    private boolean detectPerfectStormNQ(double price, double volume, double vwap, Map<String, Double> indicators) {
        double cvd = indicators.getOrDefault("CVD", 0.0);
        double heatmap = indicators.getOrDefault("Heatmap", 0.0);
        double delta = indicators.getOrDefault("Delta", 0.0);
        
        return volume > 150.0 && Math.abs(price - vwap) < 0.02 && 
               cvd > 100.0 && heatmap > 0.7 && Math.abs(delta) > 50.0;
    }
    
    private boolean detectReversalPattern(double price, double volume, double vwap, Map<String, Double> indicators) {
        double rsi = indicators.getOrDefault("RSI", 50.0);
        double macd = indicators.getOrDefault("MACD", 0.0);
        
        return (rsi > 70 || rsi < 30) && Math.abs(macd) > 0.05 && volume > 100.0;
    }
    
    private boolean detectIcebergPattern(double volume, Map<String, Double> indicators) {
        double avgVolume = indicators.getOrDefault("AvgVolume", 100.0);
        return volume > avgVolume * 3.0;
    }
    
    private boolean detectAbsorptionPattern(double volume, Map<String, Double> indicators) {
        double absorptionRate = indicators.getOrDefault("AbsorptionRate", 0.0);
        return volume > 80.0 && absorptionRate > 0.6;
    }
    
    private boolean detectSweepPattern(double price, double volume, Map<String, Double> indicators) {
        double liquidityLevel = indicators.getOrDefault("LiquidityLevel", 0.0);
        return volume > 120.0 && liquidityLevel > 0.8;
    }
    
    private boolean detectDeltaImbalance(Map<String, Double> indicators) {
        double delta = indicators.getOrDefault("Delta", 0.0);
        double imbalanceRatio = indicators.getOrDefault("ImbalanceRatio", 1.0);
        return Math.abs(delta) > 30.0 && imbalanceRatio > 2.0;
    }
    
    private boolean detectVolumeSpike(double volume, Map<String, Double> indicators) {
        double avgVolume = indicators.getOrDefault("AvgVolume", 100.0);
        return volume > avgVolume * 5.0;
    }
    
    private boolean detectLiquidityHunt(double price, double volume, Map<String, Double> indicators) {
        double liquidityZone = indicators.getOrDefault("LiquidityZone", 0.0);
        return volume > 100.0 && liquidityZone > 0.75;
    }
    
    /**
     * Update existing patterns with new market data
     */
    private void updateExistingPatterns(String symbol, double price, double volume, 
                                      double vwap, Map<String, Double> indicators) {
        List<DetectedPattern> symbolPatterns = activePatterns.values().stream()
                .filter(p -> p.getSymbol().equals(symbol) && p.isActive())
                .collect(Collectors.toList());
        
        for (DetectedPattern pattern : symbolPatterns) {
            updatePatternProgress(pattern, price, volume, vwap, indicators);
        }
    }
    
    /**
     * Update individual pattern progress and confidence
     */
    private void updatePatternProgress(DetectedPattern pattern, double price, double volume,
                                     double vwap, Map<String, Double> indicators) {
        
        int currentProgress = pattern.getProgress();
        int newProgress = calculatePatternProgress(pattern, price, volume, vwap, indicators);
        int newConfidence = calculatePatternConfidence(pattern, price, volume, vwap, indicators);
        
        pattern.updateProgress(newProgress);
        pattern.updateConfidence(newConfidence);
        
        // Check if pattern is completed or failed
        if (newProgress >= 95 && newConfidence >= pattern.getType().getMinConfidence()) {
            pattern.complete();
            successfulPatterns.incrementAndGet();
            
            // Store in history
            storePatternInHistory(pattern);
            
            System.out.println("🎯 [AdvancedPatternEngine] Pattern completed successfully: " + pattern.getId());
        } else if (newConfidence < pattern.getType().getMinConfidence() / 2) {
            pattern.fail();
            failedPatterns.incrementAndGet();
            
            System.out.println("🎯 [AdvancedPatternEngine] Pattern failed: " + pattern.getId());
        }
    }
    
    private int calculatePatternProgress(DetectedPattern pattern, double price, double volume,
                                       double vwap, Map<String, Double> indicators) {
        // Progressive calculation based on pattern type and market conditions
        int baseProgress = pattern.getProgress();
        long timeSinceDetection = System.currentTimeMillis() - pattern.getTimestamp();
        
        // Time-based progress (patterns evolve over time)
        int timeProgress = (int) Math.min(50, timeSinceDetection / (1000 * 60 * 5)); // 5 minutes = 50%
        
        // Condition-based progress
        int conditionProgress = calculateConditionProgress(pattern, price, volume, vwap, indicators);
        
        return Math.min(100, Math.max(baseProgress, timeProgress + conditionProgress));
    }
    
    private int calculateConditionProgress(DetectedPattern pattern, double price, double volume,
                                         double vwap, Map<String, Double> indicators) {
        switch (pattern.getType()) {
            case PERFECT_STORM_NQ:
                return calculatePerfectStormProgress(pattern, price, volume, vwap, indicators);
            case REVERSAL_PATTERN:
                return calculateReversalProgress(pattern, price, volume, vwap, indicators);
            default:
                return 20; // Base progress for other patterns
        }
    }
    
    private int calculatePerfectStormProgress(DetectedPattern pattern, double price, double volume,
                                            double vwap, Map<String, Double> indicators) {
        int progress = 0;
        
        // Stage 1: CVD confirmation (0-15%)
        if (indicators.getOrDefault("CVD", 0.0) > 100.0) progress += 15;
        
        // Stage 2: Heatmap activation (15-30%)
        if (indicators.getOrDefault("Heatmap", 0.0) > 0.7) progress += 15;
        
        // Stage 3: Volume confirmation (30-50%)
        if (volume > 150.0) progress += 20;
        
        // Stage 4: VWAP alignment (50-70%)
        if (Math.abs(price - vwap) < 0.02) progress += 20;
        
        // Stage 5: Delta confirmation (70-85%)
        if (Math.abs(indicators.getOrDefault("Delta", 0.0)) > 50.0) progress += 15;
        
        // Stage 6: Final confirmation (85-95%)
        if (progress >= 85 && volume > 200.0) progress += 10;
        
        // Stage 7: Completion (95-100%)
        if (progress >= 95) progress = 100;
        
        return progress;
    }
    
    private int calculateReversalProgress(DetectedPattern pattern, double price, double volume,
                                        double vwap, Map<String, Double> indicators) {
        int progress = 0;
        double rsi = indicators.getOrDefault("RSI", 50.0);
        double macd = indicators.getOrDefault("MACD", 0.0);
        
        // RSI extreme levels
        if (rsi > 70 || rsi < 30) progress += 25;
        
        // MACD divergence
        if (Math.abs(macd) > 0.05) progress += 25;
        
        // Volume confirmation
        if (volume > 100.0) progress += 25;
        
        // Price action confirmation
        if (Math.abs(price - vwap) > 0.01) progress += 25;
        
        return progress;
    }
    
    private int calculatePatternConfidence(DetectedPattern pattern, double price, double volume,
                                         double vwap, Map<String, Double> indicators) {
        // Use learning system if available
        if (learningSystem != null) {
            // Convert pattern to Map for learning system
            Map<String, Object> patternData = convertPatternToMap(pattern);
            return learningSystem.calculatePatternConfidence(patternData, price, volume, vwap, indicators);
        }
        
        // Fallback calculation
        int baseConfidence = pattern.getConfidence();
        int progressBonus = pattern.getProgress() / 10; // 1% per 10% progress
        
        return Math.min(100, baseConfidence + progressBonus);
    }
    
    private Map<String, Object> convertPatternToMap(DetectedPattern pattern) {
        Map<String, Object> patternData = new HashMap<>();
        patternData.put("type", pattern.getType().name());
        patternData.put("confidence", (double) pattern.getConfidence());
        patternData.put("progress", (double) pattern.getProgress());
        patternData.put("id", pattern.getId());
        patternData.put("symbol", pattern.getSymbol());
        patternData.put("price", pattern.getPrice());
        return patternData;
    }
    
    private void storePatternInHistory(DetectedPattern pattern) {
        patternHistory.computeIfAbsent(pattern.getSymbol(), k -> new CopyOnWriteArrayList<>())
                     .add(pattern);
        
        // Keep only last 100 patterns per symbol
        List<DetectedPattern> history = patternHistory.get(pattern.getSymbol());
        if (history.size() > 100) {
            history.remove(0);
        }
    }
    
    /**
     * Start pattern monitoring background task
     */
    private void startPatternMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupExpiredPatterns();
                updateSystemStats();
            } catch (Exception e) {
                System.err.println("🎯 [AdvancedPatternEngine] Error in pattern monitoring: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
    
    private void cleanupExpiredPatterns() {
        long currentTime = System.currentTimeMillis();
        long maxAge = 30 * 60 * 1000; // 30 minutes
        
        activePatterns.entrySet().removeIf(entry -> {
            DetectedPattern pattern = entry.getValue();
            boolean expired = (currentTime - pattern.getLastUpdate()) > maxAge;
            
            if (expired && pattern.isActive()) {
                pattern.fail();
                failedPatterns.incrementAndGet();
                System.out.println("🎯 [AdvancedPatternEngine] Pattern expired: " + pattern.getId());
            }
            
            return expired;
        });
    }
    
    private void updateSystemStats() {
        if (statusMonitor != null) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("active_patterns", activePatterns.size());
            stats.put("total_detected", totalPatternsDetected.get());
            stats.put("successful", successfulPatterns.get());
            stats.put("failed", failedPatterns.get());
            stats.put("success_rate", calculateSuccessRate());
            
            statusMonitor.updateComponentStats("AdvancedPatternEngine", stats);
        }
    }
    
    private double calculateSuccessRate() {
        long total = successfulPatterns.get() + failedPatterns.get();
        return total > 0 ? (double) successfulPatterns.get() / total * 100.0 : 0.0;
    }
    
    // Public API methods
    public List<DetectedPattern> getActivePatterns() {
        return new ArrayList<>(activePatterns.values());
    }
    
    public List<DetectedPattern> getActivePatternsForSymbol(String symbol) {
        return activePatterns.values().stream()
                .filter(p -> p.getSymbol().equals(symbol) && p.isActive())
                .collect(Collectors.toList());
    }
    
    public List<DetectedPattern> getPatternHistory(String symbol) {
        return patternHistory.getOrDefault(symbol, Collections.emptyList());
    }
    
    public Map<String, Object> getEngineStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("is_running", isRunning.get());
        stats.put("active_patterns", activePatterns.size());
        stats.put("total_detected", totalPatternsDetected.get());
        stats.put("successful", successfulPatterns.get());
        stats.put("failed", failedPatterns.get());
        stats.put("success_rate", calculateSuccessRate());
        return stats;
    }
    
    /**
     * Shutdown the pattern engine
     */
    public void shutdown() {
        isRunning.set(false);
        scheduler.shutdown();
        executorService.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        if (statusMonitor != null) {
            statusMonitor.updateComponentStatus("AdvancedPatternEngine", false);
        }
        
        System.out.println("🎯 [AdvancedPatternEngine] Engine shutdown completed");
    }
} 