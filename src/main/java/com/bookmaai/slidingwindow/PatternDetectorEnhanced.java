package com.bookmaai.slidingwindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.bookmaai.models.PatternDefinition;
import com.bookmaai.config.ConfigurationLoader;

import java.time.LocalDateTime;
import java.util.*;

/**
 * كاشف الأنماط المحسّن - يكتشف جميع الأنماط بدقة عالية
 */
@Component
public class PatternDetectorEnhanced {
    
    private static final Logger logger = LoggerFactory.getLogger(PatternDetectorEnhanced.class);
    
    private Map<String, PatternDefinition> patternDefinitions = new HashMap<>();
    private ConfigurationLoader configLoader;
    
    public PatternDetectorEnhanced() {
        this.configLoader = new ConfigurationLoader();
        loadAllPatternDefinitions();
    }
    
    /**
     * تحميل جميع تعريفات الأنماط
     */
    private void loadAllPatternDefinitions() {
        try {
            // تحميل من ConfigurationLoader
            Map<String, Object> config = configLoader.getConfig();
            if (config.containsKey("patterns")) {
                List<Map<String, Object>> patterns = (List<Map<String, Object>>) config.get("patterns");
                
                for (Map<String, Object> patternData : patterns) {
                    PatternDefinition definition = new PatternDefinition();
                    definition.setPatternId((String) patternData.get("patternId"));
                    definition.setPatternName((String) patternData.get("patternName"));
                    definition.setRequiredTools((List<String>) patternData.get("requiredTools"));
                    definition.setMinConfidence(((Number) patternData.getOrDefault("minConfidence", 0.7)).doubleValue());
                    
                    patternDefinitions.put(definition.getPatternId(), definition);
                }
                
                logger.info("📚 Loaded {} pattern definitions", patternDefinitions.size());
            }
        } catch (Exception e) {
            logger.error("Error loading pattern definitions", e);
            // تحميل أنماط افتراضية
            loadDefaultPatterns();
        }
    }
    
    /**
     * تحميل أنماط افتراضية
     */
    private void loadDefaultPatterns() {
        // Tier 1 Patterns (High accuracy)
        addPattern("PERFECT_STORM_NQ", "Perfect Storm NQ", 
            Arrays.asList("cvd", "heatmap", "volume_dots", "vwap", "volume_profile"), 0.8);
        
        addPattern("VOLUME_SPIKE_REVERSAL", "Volume Spike Reversal",
            Arrays.asList("volume_dots", "volume_bubbles", "cvd", "absorption_indicator"), 0.75);
        
        addPattern("ICEBERG_ACCUMULATION", "Iceberg Accumulation",
            Arrays.asList("iceberg_detector", "large_lot_tracker", "volume_profile", "cvd"), 0.8);
        
        // Tier 2 Patterns
        addPattern("ABSORPTION_BREAKOUT", "Absorption Breakout",
            Arrays.asList("absorption_indicator", "heatmap", "volume_profile"), 0.7);
        
        addPattern("VWAP_DEVIATION_TRADE", "VWAP Deviation Trade",
            Arrays.asList("vwap", "volume_dots", "cvd"), 0.65);
        
        addPattern("LIQUIDITY_SWEEP", "Liquidity Sweep",
            Arrays.asList("stop_run", "large_lot_tracker", "volume_bubbles"), 0.7);
        
        // Tier 3 Patterns
        addPattern("IMBALANCE_FILL", "Imbalance Fill",
            Arrays.asList("imbalance_indicator", "volume_profile", "cvd"), 0.65);
        
        addPattern("STRENGTH_MOMENTUM", "Strength Momentum",
            Arrays.asList("strength_level_indicator", "cvd", "volume_dots"), 0.6);
        
        logger.info("📚 Loaded {} default patterns", patternDefinitions.size());
    }
    
    /**
     * إضافة نمط
     */
    private void addPattern(String id, String name, List<String> tools, double minConfidence) {
        PatternDefinition definition = new PatternDefinition();
        definition.setPatternId(id);
        definition.setPatternName(name);
        definition.setRequiredTools(tools);
        definition.setMinConfidence(minConfidence);
        patternDefinitions.put(id, definition);
    }
    
    /**
     * كشف جميع الأنماط الممكنة
     */
    public List<DetectedPattern> detectAllPatterns(MarketSnapshot snapshot, Map<String, Double> toolAnalysis) {
        List<DetectedPattern> detectedPatterns = new ArrayList<>();
        
        // 1. كشف الأنماط المحددة مسبقاً
        for (PatternDefinition definition : patternDefinitions.values()) {
            double matchScore = calculatePatternMatch(definition, snapshot, toolAnalysis);
            
            // استخدام عتبة ديناميكية
            double dynamicThreshold = calculateDynamicThreshold(definition, snapshot);
            
            if (matchScore > dynamicThreshold) {
                DetectedPattern detected = new DetectedPattern(
                    definition.getPatternId(),
                    definition.getPatternName(),
                    matchScore,
                    LocalDateTime.now()
                );
                detectedPatterns.add(detected);
                
                logger.info("🎯 Pattern detected: {} ({:.1f}%)", 
                    definition.getPatternName(), matchScore * 100);
            }
        }
        
        // 2. كشف الأنماط الناشئة
        detectEmergingPatterns(snapshot, toolAnalysis, detectedPatterns);
        
        // 3. كشف الأنماط المركبة
        detectCompositePatterns(snapshot, toolAnalysis, detectedPatterns);
        
        // 4. كشف أنماط السوق العامة
        detectMarketRegimePatterns(snapshot, toolAnalysis, detectedPatterns);
        
        // ترتيب حسب القوة
        detectedPatterns.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));
        
        return detectedPatterns;
    }
    
    /**
     * حساب مطابقة النمط
     */
    private double calculatePatternMatch(PatternDefinition definition, 
                                       MarketSnapshot snapshot, 
                                       Map<String, Double> toolAnalysis) {
        double totalScore = 0;
        int matchedTools = 0;
        
        for (String requiredTool : definition.getRequiredTools()) {
            Double toolScore = toolAnalysis.getOrDefault(requiredTool, 0.0);
            
            // وزن مختلف لكل أداة حسب أهميتها
            double weight = getToolWeight(requiredTool);
            
            if (toolScore > 0.4) { // عتبة منخفضة للمرونة
                totalScore += toolScore * weight;
                matchedTools++;
            }
        }
        
        // حساب النتيجة مع مراعاة عدد الأدوات المطابقة
        double matchRatio = (double) matchedTools / definition.getRequiredTools().size();
        double finalScore = (totalScore / definition.getRequiredTools().size()) * matchRatio;
        
        // مكافآت إضافية
        if (matchedTools == definition.getRequiredTools().size()) {
            finalScore *= 1.2; // مكافأة 20% للمطابقة الكاملة
        }
        
        return Math.min(finalScore, 1.0);
    }
    
    /**
     * حساب العتبة الديناميكية
     */
    private double calculateDynamicThreshold(PatternDefinition definition, MarketSnapshot snapshot) {
        double baseThreshold = definition.getMinConfidence();
        
        // خفض العتبة في الأسواق النشطة
        if (snapshot.getVolume() > 3000) {
            baseThreshold *= 0.9;
        }
        
        // خفض العتبة عند وجود حركة سعرية قوية
        double priceChange = Math.abs(snapshot.getPrice() - snapshot.getVwap()) / snapshot.getVwap();
        if (priceChange > 0.002) { // 0.2%
            baseThreshold *= 0.95;
        }
        
        return Math.max(baseThreshold, 0.5); // الحد الأدنى 50%
    }
    
    /**
     * كشف الأنماط الناشئة
     */
    private void detectEmergingPatterns(MarketSnapshot snapshot, 
                                      Map<String, Double> toolAnalysis,
                                      List<DetectedPattern> patterns) {
        
        // نمط ناشئ: تراكم مبكر
        long earlySignals = toolAnalysis.entrySet().stream()
            .filter(e -> e.getValue() > 0.4 && e.getValue() < 0.6)
            .count();
            
        if (earlySignals >= 3) {
            patterns.add(new DetectedPattern(
                "EARLY_ACCUMULATION",
                "Early Accumulation Signal",
                0.5 + (earlySignals * 0.05),
                LocalDateTime.now()
            ));
        }
    }
    
    /**
     * كشف الأنماط المركبة
     */
    private void detectCompositePatterns(MarketSnapshot snapshot, 
                                       Map<String, Double> toolAnalysis,
                                       List<DetectedPattern> patterns) {
        
        // التقاء CVD و Volume
        if (toolAnalysis.getOrDefault("cvd", 0.0) > 0.7 &&
            toolAnalysis.getOrDefault("volume_dots", 0.0) > 0.7) {
            
            patterns.add(new DetectedPattern(
                "CVD_VOLUME_CONFLUENCE",
                "CVD-Volume Confluence",
                (toolAnalysis.get("cvd") + toolAnalysis.get("volume_dots")) / 2,
                LocalDateTime.now()
            ));
        }
        
        // تراكم متعدد المستويات
        if (toolAnalysis.getOrDefault("heatmap", 0.0) > 0.6 &&
            toolAnalysis.getOrDefault("volume_profile", 0.0) > 0.6 &&
            toolAnalysis.getOrDefault("absorption_indicator", 0.0) > 0.5) {
            
            patterns.add(new DetectedPattern(
                "MULTI_LEVEL_ACCUMULATION",
                "Multi-Level Accumulation",
                0.75,
                LocalDateTime.now()
            ));
        }
    }
    
    /**
     * كشف أنماط نظام السوق
     */
    private void detectMarketRegimePatterns(MarketSnapshot snapshot, 
                                          Map<String, Double> toolAnalysis,
                                          List<DetectedPattern> patterns) {
        
        // سوق متجه (Trending)
        if (Math.abs(snapshot.getCvd()) > 100 && snapshot.getVolume() > 2500) {
            patterns.add(new DetectedPattern(
                "TRENDING_MARKET",
                "Trending Market Regime",
                0.7,
                LocalDateTime.now()
            ));
        }
        
        // سوق متوازن (Balanced)
        if (Math.abs(snapshot.getCvd()) < 20 && 
            Math.abs(snapshot.getPrice() - snapshot.getVwap()) / snapshot.getVwap() < 0.001) {
            patterns.add(new DetectedPattern(
                "BALANCED_MARKET",
                "Balanced Market Regime",
                0.6,
                LocalDateTime.now()
            ));
        }
    }
    
    /**
     * الحصول على وزن الأداة
     */
    private double getToolWeight(String tool) {
        // أوزان مختلفة حسب دقة الأداة
        switch (tool) {
            case "cvd":
            case "heatmap":
            case "volume_dots":
                return 1.2; // أدوات عالية الدقة
                
            case "vwap":
            case "volume_profile":
            case "iceberg_detector":
                return 1.0; // أدوات متوسطة
                
            case "large_lot_tracker":
            case "absorption_indicator":
                return 0.9; // أدوات جيدة
                
            default:
                return 0.8; // أدوات أخرى
        }
    }
    
    /**
     * فئة النمط المكتشف
     */
    public static class DetectedPattern {
        private String patternId;
        private String patternName;
        private double confidence;
        private LocalDateTime detectedAt;
        
        public DetectedPattern(String patternId, String patternName, double confidence, LocalDateTime detectedAt) {
            this.patternId = patternId;
            this.patternName = patternName;
            this.confidence = confidence;
            this.detectedAt = detectedAt;
        }
        
        // Getters
        public String getPatternId() { return patternId; }
        public String getPatternName() { return patternName; }
        public double getConfidence() { return confidence; }
        public LocalDateTime getDetectedAt() { return detectedAt; }
    }
} 