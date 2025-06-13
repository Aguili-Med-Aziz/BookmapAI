package com.bookmaai.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 📚 Bookmap Configuration Loader - محمل إعدادات Bookmap من JSON
 * 
 * يقرأ ملف bookmap_knowledge_base.json ويحوله لكائنات Java قابلة للاستخدام
 * ✅ العتبات المحسنة لكل أداة
 * ✅ الأنماط المتقدمة والتكاملات
 * ✅ إعدادات خاصة بكل أصل ومالي
 * ✅ معايير الثقة والنجاح
 */
@Service
public class BookmapConfigurationLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(BookmapConfigurationLoader.class);
    
    private JsonNode bookmapConfig;
    private Map<String, BookmapToolConfig> toolConfigurations = new ConcurrentHashMap<>();
    private Map<String, PatternConfig> patternConfigurations = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void loadConfiguration() {
        try {
            logger.info("🔧 Loading Bookmap configuration from JSON...");
            
            // قراءة ملف JSON من resources
            ClassPathResource resource = new ClassPathResource("bookmap_knowledge_base.json");
            InputStream inputStream = resource.getInputStream();
            
            ObjectMapper mapper = new ObjectMapper();
            bookmapConfig = mapper.readTree(inputStream);
            
            // تحميل إعدادات الأدوات
            loadToolConfigurations();
            
            // تحميل إعدادات الأنماط
            loadPatternConfigurations();
            
            logger.info("✅ Bookmap configuration loaded successfully: {} tools, {} patterns", 
                       toolConfigurations.size(), patternConfigurations.size());
            
        } catch (IOException e) {
            logger.error("❌ Failed to load Bookmap configuration: {}", e.getMessage());
            // استخدام إعدادات افتراضية
            loadDefaultConfiguration();
        }
    }
    
    /**
     * 🔧 تحميل إعدادات الأدوات من JSON
     */
    private void loadToolConfigurations() {
        try {
            JsonNode toolsNode = bookmapConfig.path("bookmap_knowledge_base").path("tools");
            
            toolsNode.fieldNames().forEachRemaining(toolName -> {
                try {
                    JsonNode toolNode = toolsNode.get(toolName);
                    BookmapToolConfig config = parseToolConfiguration(toolName, toolNode);
                    toolConfigurations.put(toolName, config);
                    
                    logger.debug("📊 Loaded tool config: {} (accuracy: {}%)", 
                                toolName, config.getAccuracyRating());
                    
                } catch (Exception e) {
                    logger.warn("⚠️ Failed to load config for tool {}: {}", toolName, e.getMessage());
                }
            });
            
        } catch (Exception e) {
            logger.error("❌ Error loading tool configurations: {}", e.getMessage());
        }
    }
    
    /**
     * 🎭 تحميل إعدادات الأنماط من JSON
     */
    private void loadPatternConfigurations() {
        try {
            JsonNode toolsNode = bookmapConfig.path("bookmap_knowledge_base").path("tools");
            
            toolsNode.fieldNames().forEachRemaining(toolName -> {
                JsonNode toolNode = toolsNode.get(toolName);
                JsonNode enhancedThresholds = toolNode.path("enhanced_thresholds");
                JsonNode patternDefs = enhancedThresholds.path("pattern_definitions");
                
                if (!patternDefs.isMissingNode()) {
                    patternDefs.fieldNames().forEachRemaining(patternName -> {
                        try {
                            JsonNode patternNode = patternDefs.get(patternName);
                            PatternConfig config = parsePatternConfiguration(patternName, patternNode, toolName);
                            patternConfigurations.put(patternName, config);
                            
                        } catch (Exception e) {
                            logger.warn("⚠️ Failed to load pattern {}: {}", patternName, e.getMessage());
                        }
                    });
                }
            });
            
        } catch (Exception e) {
            logger.error("❌ Error loading pattern configurations: {}", e.getMessage());
        }
    }
    
    /**
     * 🔧 تحليل إعدادات أداة معينة
     */
    private BookmapToolConfig parseToolConfiguration(String toolName, JsonNode toolNode) {
        return new BookmapToolConfig(
            toolNode.path("id").asText(),
            toolNode.path("name").asText(),
            toolNode.path("category").asText(),
            toolNode.path("priority").asText(),
            toolNode.path("accuracy_rating").asDouble(75.0),
            parseEnhancedThresholds(toolNode.path("enhanced_thresholds")),
            parseAssetSpecificConfig(toolNode.path("enhanced_thresholds"))
        );
    }
    
    /**
     * 🎭 تحليل إعدادات نمط معين
     */
    private PatternConfig parsePatternConfiguration(String patternName, JsonNode patternNode, String toolName) {
        double successRate = 0.75; // قيمة افتراضية
        
        // محاولة استخراج success_rate من مواقع مختلفة
        if (patternNode.has("success_rate")) {
            successRate = patternNode.path("success_rate").asDouble(0.75);
        } else if (patternNode.has("conditions") && patternNode.path("conditions").has("success_rate")) {
            successRate = patternNode.path("conditions").path("success_rate").asDouble(0.75);
        }
        
        return new PatternConfig(
            patternName,
            toolName,
            successRate,
            patternNode.path("description").asText(""),
            parsePatternConditions(patternNode),
            parsePatternTargets(patternNode)
        );
    }
    
    /**
     * 📊 تحليل العتبات المحسنة
     */
    private Map<String, Object> parseEnhancedThresholds(JsonNode thresholdsNode) {
        Map<String, Object> thresholds = new HashMap<>();
        
        // تحليل asset_specific_thresholds
        JsonNode assetNode = thresholdsNode.path("asset_specific_thresholds");
        if (!assetNode.isMissingNode()) {
            Map<String, Map<String, Object>> assetThresholds = new HashMap<>();
            assetNode.fieldNames().forEachRemaining(asset -> {
                Map<String, Object> assetConfig = new HashMap<>();
                JsonNode assetConfigNode = assetNode.get(asset);
                
                assetConfigNode.fieldNames().forEachRemaining(key -> {
                    JsonNode valueNode = assetConfigNode.get(key);
                    if (valueNode.isTextual()) {
                        assetConfig.put(key, valueNode.asText());
                    } else if (valueNode.isNumber()) {
                        assetConfig.put(key, valueNode.asDouble());
                    } else if (valueNode.isBoolean()) {
                        assetConfig.put(key, valueNode.asBoolean());
                    }
                });
                
                assetThresholds.put(asset, assetConfig);
            });
            thresholds.put("asset_specific_thresholds", assetThresholds);
        }
        
        return thresholds;
    }
    
    /**
     * 💰 تحليل إعدادات خاصة بالأصول
     */
    private Map<String, AssetConfig> parseAssetSpecificConfig(JsonNode thresholdsNode) {
        Map<String, AssetConfig> assetConfigs = new HashMap<>();
        
        JsonNode assetNode = thresholdsNode.path("asset_specific_thresholds");
        if (!assetNode.isMissingNode()) {
            assetNode.fieldNames().forEachRemaining(asset -> {
                JsonNode assetConfigNode = assetNode.get(asset);
                AssetConfig config = new AssetConfig(
                    asset,
                    assetConfigNode.path("success_rate").asDouble(0.75),
                    assetConfigNode.path("best_session").asText("NY_Open"),
                    parseSessionThresholds(assetConfigNode)
                );
                assetConfigs.put(asset, config);
            });
        }
        
        return assetConfigs;
    }
    
    /**
     * ⏰ تحليل عتبات الجلسات
     */
    private Map<String, SessionThresholds> parseSessionThresholds(JsonNode assetNode) {
        Map<String, SessionThresholds> sessions = new HashMap<>();
        
        // البحث عن جلسات مختلفة في البيانات
        String[] sessionKeys = {"london_session", "ny_session", "asia_session"};
        
        for (String sessionKey : sessionKeys) {
            JsonNode sessionNode = assetNode.path(sessionKey);
            if (!sessionNode.isMissingNode()) {
                SessionThresholds thresholds = new SessionThresholds(
                    sessionNode.path("buy_threshold").asDouble(0),
                    sessionNode.path("sell_threshold").asDouble(0),
                    sessionNode.path("reversal_point").asDouble(0),
                    sessionNode.path("confirmation_time_minutes").asInt(15)
                );
                sessions.put(sessionKey.replace("_session", ""), thresholds);
            }
        }
        
        return sessions;
    }
    
    /**
     * 🎯 تحليل شروط النمط
     */
    private Map<String, Object> parsePatternConditions(JsonNode patternNode) {
        Map<String, Object> conditions = new HashMap<>();
        
        JsonNode conditionsNode = patternNode.path("conditions");
        if (!conditionsNode.isMissingNode()) {
            conditionsNode.fieldNames().forEachRemaining(key -> {
                JsonNode valueNode = conditionsNode.get(key);
                if (valueNode.isTextual()) {
                    conditions.put(key, valueNode.asText());
                } else if (valueNode.isNumber()) {
                    conditions.put(key, valueNode.asDouble());
                }
            });
        }
        
        // إضافة الشروط المباشرة من النمط نفسه
        if (patternNode.has("target")) {
            conditions.put("target", patternNode.path("target").asText());
        }
        
        return conditions;
    }
    
    /**
     * 🎯 تحليل أهداف النمط
     */
    private Map<String, Double> parsePatternTargets(JsonNode patternNode) {
        Map<String, Double> targets = new HashMap<>();
        
        if (patternNode.has("target")) {
            String targetText = patternNode.path("target").asText();
            // استخراج القيم الرقمية من النص (مثل "+120 points")
            if (targetText.contains("points")) {
                try {
                    String numberPart = targetText.replaceAll("[^0-9.-]", "");
                    if (!numberPart.isEmpty()) {
                        targets.put("points", Double.parseDouble(numberPart));
                    }
                } catch (NumberFormatException e) {
                    logger.debug("Could not parse target points from: {}", targetText);
                }
            }
        }
        
        return targets;
    }
    
    /**
     * 🔧 تحميل إعدادات افتراضية في حالة فشل قراءة JSON
     */
    private void loadDefaultConfiguration() {
        logger.warn("⚠️ Loading default configuration...");
        
        // أدوات افتراضية أساسية
        toolConfigurations.put("cvd", new BookmapToolConfig("tool_003", "CVD", "volume_analysis", "core", 99.0, new HashMap<>(), new HashMap<>()));
        toolConfigurations.put("heatmap", new BookmapToolConfig("tool_001", "Heatmap", "liquidity_analysis", "core", 97.0, new HashMap<>(), new HashMap<>()));
        toolConfigurations.put("volume_dots", new BookmapToolConfig("tool_002", "Volume Dots", "volume_analysis", "core", 96.0, new HashMap<>(), new HashMap<>()));
        toolConfigurations.put("vwap", new BookmapToolConfig("tool_004", "VWAP", "price_analysis", "core", 94.0, new HashMap<>(), new HashMap<>()));
        
        logger.info("✅ Default configuration loaded: {} tools", toolConfigurations.size());
    }
    
    // ================ Public Access Methods ================
    
    /**
     * 📊 الحصول على إعدادات أداة معينة
     */
    public BookmapToolConfig getToolConfiguration(String toolName) {
        return toolConfigurations.get(toolName);
    }
    
    /**
     * 🎭 الحصول على إعدادات نمط معين
     */
    public PatternConfig getPatternConfiguration(String patternName) {
        return patternConfigurations.get(patternName);
    }
    
    /**
     * 📋 الحصول على جميع الأدوات المحملة
     */
    public Set<String> getAvailableTools() {
        return toolConfigurations.keySet();
    }
    
    /**
     * 🎨 الحصول على جميع الأنماط المحملة
     */
    public Set<String> getAvailablePatterns() {
        return patternConfigurations.keySet();
    }
    
    /**
     * 💰 الحصول على عتبات خاصة بأصل معين
     */
    public Map<String, Object> getAssetThresholds(String toolName, String asset) {
        BookmapToolConfig config = toolConfigurations.get(toolName);
        if (config != null && config.getAssetConfigurations().containsKey(asset)) {
            AssetConfig assetConfig = config.getAssetConfigurations().get(asset);
            Map<String, Object> thresholds = new HashMap<>();
            thresholds.put("success_rate", assetConfig.getSuccessRate());
            thresholds.put("best_session", assetConfig.getBestSession());
            thresholds.put("session_thresholds", assetConfig.getSessionThresholds());
            return thresholds;
        }
        return new HashMap<>();
    }
    
    /**
     * 🔍 البحث عن أنماط تحتوي على كلمة معينة
     */
    public List<PatternConfig> findPatternsByKeyword(String keyword) {
        return patternConfigurations.values().stream()
            .filter(pattern -> pattern.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                             pattern.getDescription().toLowerCase().contains(keyword.toLowerCase()))
            .toList();
    }
    
    /**
     * 📈 الحصول على أقوى الأنماط حسب معدل النجاح
     */
    public List<PatternConfig> getStrongestPatterns(int limit) {
        return patternConfigurations.values().stream()
            .sorted((p1, p2) -> Double.compare(p2.getSuccessRate(), p1.getSuccessRate()))
            .limit(limit)
            .toList();
    }
    
    // ================ Data Classes ================
    
    /**
     * 🔧 إعدادات أداة Bookmap
     */
    public static class BookmapToolConfig {
        private final String id;
        private final String name;
        private final String category;
        private final String priority;
        private final double accuracyRating;
        private final Map<String, Object> enhancedThresholds;
        private final Map<String, AssetConfig> assetConfigurations;
        
        public BookmapToolConfig(String id, String name, String category, String priority, 
                               double accuracyRating, Map<String, Object> enhancedThresholds,
                               Map<String, AssetConfig> assetConfigurations) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.priority = priority;
            this.accuracyRating = accuracyRating;
            this.enhancedThresholds = enhancedThresholds;
            this.assetConfigurations = assetConfigurations;
        }
        
        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getPriority() { return priority; }
        public double getAccuracyRating() { return accuracyRating; }
        public Map<String, Object> getEnhancedThresholds() { return enhancedThresholds; }
        public Map<String, AssetConfig> getAssetConfigurations() { return assetConfigurations; }
    }
    
    /**
     * 🎭 إعدادات نمط تداول
     */
    public static class PatternConfig {
        private final String name;
        private final String toolName;
        private final double successRate;
        private final String description;
        private final Map<String, Object> conditions;
        private final Map<String, Double> targets;
        
        public PatternConfig(String name, String toolName, double successRate, String description,
                           Map<String, Object> conditions, Map<String, Double> targets) {
            this.name = name;
            this.toolName = toolName;
            this.successRate = successRate;
            this.description = description;
            this.conditions = conditions;
            this.targets = targets;
        }
        
        // Getters
        public String getName() { return name; }
        public String getToolName() { return toolName; }
        public double getSuccessRate() { return successRate; }
        public String getDescription() { return description; }
        public Map<String, Object> getConditions() { return conditions; }
        public Map<String, Double> getTargets() { return targets; }
    }
    
    /**
     * 💰 إعدادات خاصة بأصل مالي
     */
    public static class AssetConfig {
        private final String assetName;
        private final double successRate;
        private final String bestSession;
        private final Map<String, SessionThresholds> sessionThresholds;
        
        public AssetConfig(String assetName, double successRate, String bestSession,
                         Map<String, SessionThresholds> sessionThresholds) {
            this.assetName = assetName;
            this.successRate = successRate;
            this.bestSession = bestSession;
            this.sessionThresholds = sessionThresholds;
        }
        
        // Getters
        public String getAssetName() { return assetName; }
        public double getSuccessRate() { return successRate; }
        public String getBestSession() { return bestSession; }
        public Map<String, SessionThresholds> getSessionThresholds() { return sessionThresholds; }
    }
    
    /**
     * ⏰ عتبات الجلسات
     */
    public static class SessionThresholds {
        private final double buyThreshold;
        private final double sellThreshold;
        private final double reversalPoint;
        private final int confirmationTimeMinutes;
        
        public SessionThresholds(double buyThreshold, double sellThreshold, 
                               double reversalPoint, int confirmationTimeMinutes) {
            this.buyThreshold = buyThreshold;
            this.sellThreshold = sellThreshold;
            this.reversalPoint = reversalPoint;
            this.confirmationTimeMinutes = confirmationTimeMinutes;
        }
        
        // Getters
        public double getBuyThreshold() { return buyThreshold; }
        public double getSellThreshold() { return sellThreshold; }
        public double getReversalPoint() { return reversalPoint; }
        public int getConfirmationTimeMinutes() { return confirmationTimeMinutes; }
    }
}
