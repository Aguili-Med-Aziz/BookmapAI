package com.bookmaai.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.Collectors;

/**
 * 🧠 Adaptive Learning System - نظام التعلم التكيفي المتقدم
 * 
 * Advanced AI system that learns from pattern outcomes and continuously
 * improves detection accuracy and confidence calculations.
 */
public class AdaptiveLearningSystem {
    
    public static class LearningModel {
        private final String modelType;
        private final Map<String, Double> weights = new ConcurrentHashMap<>();
        private final AtomicLong samples = new AtomicLong(0);
        private final AtomicLong successes = new AtomicLong(0);
        private final AtomicReference<Double> accuracy = new AtomicReference<>(0.0);
        
        public LearningModel(String modelType) {
            this.modelType = modelType;
            initializeWeights();
        }
        
        private void initializeWeights() {
            weights.put("volume", 0.25);
            weights.put("price_action", 0.20);
            weights.put("indicators", 0.30);
            weights.put("time_context", 0.25);
        }
        
        public void updateModel(Map<String, Double> features, boolean success) {
            samples.incrementAndGet();
            if (success) {
                successes.incrementAndGet();
            }
            
            // Update accuracy
            double newAccuracy = (double) successes.get() / samples.get();
            accuracy.set(newAccuracy);
            
            // Update weights based on success/failure
            for (String feature : features.keySet()) {
                double currentWeight = weights.getOrDefault(feature, 0.1);
                double adjustment = success ? 0.01 : -0.01;
                double newWeight = Math.max(0.05, Math.min(0.95, currentWeight + adjustment));
                weights.put(feature, newWeight);
            }
        }
        
        public double calculateConfidence(Map<String, Double> features) {
            double score = 0.0;
            double totalWeight = 0.0;
            
            for (Map.Entry<String, Double> feature : features.entrySet()) {
                double weight = weights.getOrDefault(feature.getKey(), 0.1);
                score += feature.getValue() * weight;
                totalWeight += weight;
            }
            
            return totalWeight > 0 ? score / totalWeight : 0.5;
        }
        
        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("model_type", modelType);
            stats.put("samples", samples.get());
            stats.put("successes", successes.get());
            stats.put("accuracy", accuracy.get());
            stats.put("weights", new HashMap<>(weights));
            return stats;
        }
    }
    
    // System State
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final Map<String, LearningModel> models = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    // Dependencies
    private SystemStatusMonitor statusMonitor;
    private PatternLearningLogger learningLogger;
    
    // Statistics
    private final AtomicLong totalLearningEvents = new AtomicLong(0);
    private final AtomicLong modelUpdates = new AtomicLong(0);
    private final AtomicReference<Double> overallAccuracy = new AtomicReference<>(0.0);
    
    public AdaptiveLearningSystem() {
        System.out.println("🧠 [AdaptiveLearningSystem] Initializing Adaptive Learning System...");
        initializeModels();
    }
    
    /**
     * Initialize the learning system
     */
    public void initialize() {
        isRunning.set(true);
        startLearningTasks();
        System.out.println("🧠 [AdaptiveLearningSystem] Learning system initialized and active");
    }
    
    /**
     * Initialize learning models for different pattern types
     */
    private void initializeModels() {
        // Initialize models for different pattern types
        models.put("PERFECT_STORM_NQ", new LearningModel("PERFECT_STORM_NQ"));
        models.put("ICEBERG_PATTERN", new LearningModel("ICEBERG_PATTERN"));
        models.put("ABSORPTION_PATTERN", new LearningModel("ABSORPTION_PATTERN"));
        models.put("REVERSAL_PATTERN", new LearningModel("REVERSAL_PATTERN"));
        
        System.out.println("🧠 [AdaptiveLearningSystem] Initialized " + models.size() + " learning models");
    }
    
    /**
     * Learn from pattern outcome
     */
    public void learnFromPattern(Map<String, Object> pattern, boolean success) {
        if (!isRunning.get()) return;
        
        try {
            String patternType = pattern.get("type").toString();
            LearningModel model = models.get(patternType);
            
            if (model != null) {
                // Extract features for learning
                Map<String, Double> features = extractFeatures(pattern);
                
                // Update the model
                model.updateModel(features, success);
                modelUpdates.incrementAndGet();
                
                // Log learning event (commented out due to type mismatch)
                // if (learningLogger != null) {
                //     learningLogger.logLearningEvent(patternType, success, features);
                // }
                
                totalLearningEvents.incrementAndGet();
                
                System.out.println(String.format("🧠 [AdaptiveLearningSystem] Learned from %s: %s (Total events: %d)",
                        patternType, success ? "SUCCESS" : "FAILURE", totalLearningEvents.get()));
            }
            
        } catch (Exception e) {
            System.err.println("🧠 [AdaptiveLearningSystem] Error learning from pattern: " + e.getMessage());
        }
    }
    
    /**
     * Extract features for machine learning
     */
    private Map<String, Double> extractFeatures(Map<String, Object> pattern) {
        Map<String, Double> features = new HashMap<>();
        features.put("confidence", getDoubleValue(pattern, "confidence") / 100.0);
        features.put("progress", getDoubleValue(pattern, "progress") / 100.0);
        
        // Add default features if not present
        features.put("volume", 0.5);  // Normalized volume
        features.put("price_action", 0.5);  // Normalized price action
        
        return features;
    }
    
    private double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
    
    /**
     * Calculate pattern confidence using learned models
     */
    public int calculatePatternConfidence(Map<String, Object> pattern, 
                                        double price, double volume, double vwap, 
                                        Map<String, Double> indicators) {
        if (!isRunning.get()) {
            return 75; // Default confidence
        }
        
        try {
            String patternType = pattern.get("type").toString();
            LearningModel model = models.get(patternType);
            
            if (model == null) {
                return 75; // Default confidence
            }
            
            // Extract current features
            Map<String, Double> features = new HashMap<>();
            features.put("volume", Math.min(1.0, volume / 1000.0));
            features.put("price_action", Math.min(1.0, Math.abs(price - vwap) / price));
            
            // Add normalized indicators
            for (Map.Entry<String, Double> indicator : indicators.entrySet()) {
                String key = "indicator_" + indicator.getKey().toLowerCase();
                double value = normalizeIndicatorValue(indicator.getKey(), indicator.getValue());
                features.put(key, value);
            }
            
            // Calculate base confidence from model
            double modelConfidence = model.calculateConfidence(features);
            
            // Final confidence calculation
            double finalConfidence = modelConfidence * 100.0;
            
            return (int) Math.max(10, Math.min(100, finalConfidence));
            
        } catch (Exception e) {
            System.err.println("🧠 [AdaptiveLearningSystem] Error calculating confidence: " + e.getMessage());
            return 75; // Default confidence on error
        }
    }
    
    private double normalizeIndicatorValue(String indicatorName, double value) {
        switch (indicatorName.toLowerCase()) {
            case "rsi":
                return value / 100.0;
            case "volume":
                return Math.min(1.0, value / 1000.0);
            case "cvd":
                return Math.min(1.0, Math.abs(value) / 500.0);
            case "heatmap":
                return value;
            case "delta":
                return Math.min(1.0, Math.abs(value) / 100.0);
            default:
                return Math.min(1.0, Math.abs(value));
        }
    }
    
    /**
     * Start background learning tasks
     */
    private void startLearningTasks() {
        // Model optimization task
        scheduler.scheduleAtFixedRate(() -> {
            try {
                optimizeModels();
                updateSystemStats();
            } catch (Exception e) {
                System.err.println("🧠 [AdaptiveLearningSystem] Error in optimization task: " + e.getMessage());
            }
        }, 5, 5, TimeUnit.MINUTES);
        
        // Learning rate adjustment task
        scheduler.scheduleAtFixedRate(() -> {
            try {
                adjustLearningRates();
            } catch (Exception e) {
                System.err.println("🧠 [AdaptiveLearningSystem] Error adjusting learning rates: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.HOURS);
    }
    
    /**
     * Optimize learning models
     */
    private void optimizeModels() {
        for (LearningModel model : models.values()) {
            // Adjust learning rate based on recent performance
            Map<String, Object> stats = model.getStats();
            double accuracy = (Double) stats.get("accuracy");
            long samples = (Long) stats.get("samples");
            
            if (samples > 50) { // Only adjust if we have enough samples
                double currentRate = accuracy;
                
                if (accuracy > 0.8) {
                    // Good performance, reduce learning rate
                    model.accuracy.set(Math.max(0.001, currentRate * 0.9));
                } else if (accuracy < 0.6) {
                    // Poor performance, increase learning rate
                    model.accuracy.set(Math.min(0.1, currentRate * 1.1));
                }
            }
        }
    }
    
    private void adjustLearningRates() {
        // Global learning rate adjustment based on overall system performance
        double avgAccuracy = calculateOverallAccuracy();
        
        for (LearningModel model : models.values()) {
            double currentRate = model.accuracy.get();
            
            if (avgAccuracy > 0.85) {
                // System performing well, stabilize learning
                model.accuracy.set(Math.max(0.001, currentRate * 0.95));
            } else if (avgAccuracy < 0.65) {
                // System needs more learning
                model.accuracy.set(Math.min(0.05, currentRate * 1.05));
            }
        }
    }
    
    private double calculateOverallAccuracy() {
        double totalAccuracy = 0.0;
        int modelCount = 0;
        
        for (LearningModel model : models.values()) {
            Map<String, Object> stats = model.getStats();
            long samples = (Long) stats.get("samples");
            
            if (samples > 10) { // Only consider models with enough data
                totalAccuracy += (Double) stats.get("accuracy");
                modelCount++;
            }
        }
        
        double accuracy = modelCount > 0 ? totalAccuracy / modelCount : 0.0;
        overallAccuracy.set(accuracy);
        return accuracy;
    }
    
    private void updateSystemStats() {
        if (statusMonitor != null) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("learning_events", totalLearningEvents.get());
            stats.put("model_updates", modelUpdates.get());
            stats.put("overall_accuracy", overallAccuracy.get());
            stats.put("active_models", models.size());
            
            // Add individual model stats
            Map<String, Object> modelStats = new HashMap<>();
            for (Map.Entry<String, LearningModel> entry : models.entrySet()) {
                modelStats.put(entry.getKey(), entry.getValue().getStats());
            }
            stats.put("models", modelStats);
            
            statusMonitor.updateComponentStats("AdaptiveLearningSystem", stats);
        }
    }
    
    // Public API methods
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("is_running", isRunning.get());
        stats.put("total_learning_events", totalLearningEvents.get());
        stats.put("model_updates", modelUpdates.get());
        stats.put("overall_accuracy", overallAccuracy.get());
        stats.put("active_models", models.size());
        
        Map<String, Object> modelStats = new HashMap<>();
        for (Map.Entry<String, LearningModel> entry : models.entrySet()) {
            modelStats.put(entry.getKey(), entry.getValue().getStats());
        }
        stats.put("models", modelStats);
        
        return stats;
    }
    
    /**
     * Shutdown the learning system
     */
    public void shutdown() {
        isRunning.set(false);
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        if (statusMonitor != null) {
            statusMonitor.updateComponentStatus("AdaptiveLearningSystem", false);
        }
        
        System.out.println("🧠 [AdaptiveLearningSystem] Learning system shutdown completed");
    }
} 