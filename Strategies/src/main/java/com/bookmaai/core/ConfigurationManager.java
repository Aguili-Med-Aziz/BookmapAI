package com.bookmaai.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced Configuration Manager for BookmapAI
 * Loads and manages JSON configuration files
 */
public class ConfigurationManager {
    private static ConfigurationManager instance;
    private final ObjectMapper objectMapper;
    private final Map<String, JsonNode> configurations;
    private final Map<String, Long> lastModified;
    
    // Configuration file paths
    private static final String TRADING_CONFIG = "/config/trading-config.json";
    private static final String ICT_PATTERNS = "/patterns/ict-patterns.json";
    private static final String MARKET_DATA_SCHEMA = "/data/market-data-schema.json";
    
    private ConfigurationManager() {
        this.objectMapper = new ObjectMapper();
        this.configurations = new ConcurrentHashMap<>();
        this.lastModified = new ConcurrentHashMap<>();
        loadAllConfigurations();
    }
    
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }
    
    /**
     * Load all configuration files
     */
    private void loadAllConfigurations() {
        loadConfiguration("trading", TRADING_CONFIG);
        loadConfiguration("patterns", ICT_PATTERNS);
        loadConfiguration("market_data", MARKET_DATA_SCHEMA);
    }
    
    /**
     * Load a specific configuration file
     */
    private void loadConfiguration(String key, String resourcePath) {
        try {
            InputStream inputStream = getClass().getResourceAsStream(resourcePath);
            if (inputStream != null) {
                JsonNode config = objectMapper.readTree(inputStream);
                configurations.put(key, config);
                lastModified.put(key, System.currentTimeMillis());
                System.out.println("✅ Loaded configuration: " + key + " from " + resourcePath);
            } else {
                System.err.println("❌ Configuration file not found: " + resourcePath);
                // Create empty configuration
                configurations.put(key, objectMapper.createObjectNode());
            }
        } catch (IOException e) {
            System.err.println("❌ Error loading configuration " + key + ": " + e.getMessage());
            configurations.put(key, objectMapper.createObjectNode());
        }
    }
    
    /**
     * Get trading configuration
     */
    public TradingConfig getTradingConfig() {
        JsonNode config = configurations.get("trading");
        if (config == null) return new TradingConfig();
        
        return new TradingConfig(config);
    }
    
    /**
     * Get ICT patterns configuration
     */
    public ICTPatterns getICTPatterns() {
        JsonNode config = configurations.get("patterns");
        if (config == null) return new ICTPatterns();
        
        return new ICTPatterns(config);
    }
    
    /**
     * Get market data schema
     */
    public MarketDataConfig getMarketDataConfig() {
        JsonNode config = configurations.get("market_data");
        if (config == null) return new MarketDataConfig();
        
        return new MarketDataConfig(config);
    }
    
    /**
     * Get raw configuration by key
     */
    public JsonNode getConfiguration(String key) {
        return configurations.get(key);
    }
    
    /**
     * Get configuration value by path (e.g., "trading.risk_management.stop_loss.enabled")
     */
    public JsonNode getConfigValue(String configKey, String path) {
        JsonNode config = configurations.get(configKey);
        if (config == null) return null;
        
        String[] pathParts = path.split("\\.");
        JsonNode current = config;
        
        for (String part : pathParts) {
            current = current.get(part);
            if (current == null) return null;
        }
        
        return current;
    }
    
    /**
     * Trading Configuration wrapper
     */
    public static class TradingConfig {
        private final JsonNode config;
        
        public TradingConfig() {
            this.config = new ObjectMapper().createObjectNode();
        }
        
        public TradingConfig(JsonNode config) {
            this.config = config;
        }
        
        public boolean isTradingEnabled() {
            return config.path("trading").path("enabled").asBoolean(false);
        }
        
        public String[] getSymbols() {
            JsonNode symbols = config.path("trading").path("symbols");
            if (symbols.isArray()) {
                String[] result = new String[symbols.size()];
                for (int i = 0; i < symbols.size(); i++) {
                    result[i] = symbols.get(i).asText();
                }
                return result;
            }
            return new String[]{"EURUSD", "GBPUSD"};
        }
        
        public int getMaxPositions() {
            return config.path("trading").path("max_positions").asInt(5);
        }
        
        public double getDefaultPositionPercent() {
            return config.path("trading").path("position_size").path("default_percent").asDouble(2.0);
        }
        
        public boolean isStopLossEnabled() {
            return config.path("risk_management").path("stop_loss").path("enabled").asBoolean(true);
        }
        
        public double getRiskRewardRatio() {
            return config.path("risk_management").path("take_profit").path("risk_reward_ratio").asDouble(2.1);
        }
        
        public double getMaxRiskPercent() {
            return config.path("risk_management").path("stop_loss").path("max_risk_percent").asDouble(2.0);
        }
        
        public boolean isAIModelEnabled() {
            return config.path("ai_model").path("enabled").asBoolean(true);
        }
        
        public int getAIConfidenceThreshold() {
            return config.path("ai_model").path("confidence_threshold").asInt(70);
        }
    }
    
    /**
     * ICT Patterns Configuration wrapper
     */
    public static class ICTPatterns {
        private final JsonNode config;
        
        public ICTPatterns() {
            this.config = new ObjectMapper().createObjectNode();
        }
        
        public ICTPatterns(JsonNode config) {
            this.config = config;
        }
        
        public boolean isPerfectStormEnabled() {
            return config.path("ict_patterns").path("patterns").path("perfect_storm_nq").path("success_rate").asDouble(0) > 0;
        }
        
        public double getPerfectStormSuccessRate() {
            return config.path("ict_patterns").path("patterns").path("perfect_storm_nq").path("success_rate").asDouble(94.2);
        }
        
        public double getReversalSuccessRate() {
            return config.path("ict_patterns").path("patterns").path("reversal_double_top").path("success_rate").asDouble(82.1);
        }
        
        public double getIcebergSuccessRate() {
            return config.path("ict_patterns").path("patterns").path("iceberg_detection").path("success_rate").asDouble(91.4);
        }
        
        public double getBreakoutSuccessRate() {
            return config.path("ict_patterns").path("patterns").path("breakout_consolidation").path("success_rate").asDouble(78.9);
        }
        
        public String[] getAvailablePatterns() {
            JsonNode patterns = config.path("ict_patterns").path("patterns");
            if (patterns.isObject()) {
                return patterns.fieldNames().next() != null ? 
                    new String[]{"perfect_storm_nq", "reversal_double_top", "iceberg_detection", "breakout_consolidation"} :
                    new String[0];
            }
            return new String[0];
        }
    }
    
    /**
     * Market Data Configuration wrapper
     */
    public static class MarketDataConfig {
        private final JsonNode config;
        
        public MarketDataConfig() {
            this.config = new ObjectMapper().createObjectNode();
        }
        
        public MarketDataConfig(JsonNode config) {
            this.config = config;
        }
        
        public String getPrimaryDataSource() {
            return config.path("market_data_schema").path("data_sources").path("bookmap").path("enabled").asBoolean(false) ? 
                "bookmap" : "mt5";
        }
        
        public int getBookmapLatency() {
            return config.path("market_data_schema").path("data_sources").path("bookmap").path("latency_ms").asInt(50);
        }
        
        public String[] getSupportedInstruments() {
            JsonNode instruments = config.path("market_data_schema").path("instruments");
            if (instruments.isObject()) {
                return new String[]{"EURUSD", "GBPUSD", "NQ", "ES"};
            }
            return new String[0];
        }
        
        public boolean isRealTimeDataEnabled() {
            return config.path("market_data_schema").path("real_time_data").path("tick_data").path("frequency").asText().equals("every_tick");
        }
    }
    
    /**
     * Reload configurations (useful for runtime updates)
     */
    public void reloadConfigurations() {
        System.out.println("🔄 Reloading configurations...");
        loadAllConfigurations();
    }
    
    /**
     * Get configuration status for dashboard
     */
    public Map<String, Object> getConfigurationStatus() {
        Map<String, Object> status = new HashMap<>();
        
        TradingConfig trading = getTradingConfig();
        ICTPatterns patterns = getICTPatterns();
        MarketDataConfig marketData = getMarketDataConfig();
        
        status.put("trading_enabled", trading.isTradingEnabled());
        status.put("ai_enabled", trading.isAIModelEnabled());
        status.put("symbols_count", trading.getSymbols().length);
        status.put("patterns_available", patterns.getAvailablePatterns().length);
        status.put("data_source", marketData.getPrimaryDataSource());
        status.put("configurations_loaded", configurations.size());
        status.put("last_reload", lastModified.get("trading"));
        
        return status;
    }
} 