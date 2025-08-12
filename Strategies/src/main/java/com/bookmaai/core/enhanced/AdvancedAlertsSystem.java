package com.bookmaai.core.enhanced;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Advanced Alerts System v2.0 - Comprehensive Trading Alert Management
 * Features:
 * - Real-time pattern alerts with confidence scoring
 * - Multi-channel notifications (Dashboard, Telegram, Email, SMS)
 * - Smart filtering and prioritization
 * - Risk management alerts
 * - Market sentiment notifications
 * - Performance tracking and analytics
 * - Custom alert rules and conditions
 * - Alert history and statistics
 */
public class AdvancedAlertsSystem {
    
    private static final String VERSION = "2.0-Enhanced";
    private final Map<String, AlertRule> alertRules = new ConcurrentHashMap<>();
    private final Queue<Alert> alertQueue = new ConcurrentLinkedQueue<>();
    private final List<Alert> alertHistory = new CopyOnWriteArrayList<>();
    private final ExecutorService notificationExecutor = Executors.newFixedThreadPool(8);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    // Notification services
    private final TelegramNotificationService telegramService;
    private final EmailNotificationService emailService;
    private final DashboardNotificationService dashboardService;
    private final SMSNotificationService smsService;
    
    // Statistics
    private final AtomicLong totalAlerts = new AtomicLong(0);
    private final AtomicLong successfulNotifications = new AtomicLong(0);
    private final AtomicLong failedNotifications = new AtomicLong(0);
    private final Map<String, AtomicLong> alertTypeCounts = new ConcurrentHashMap<>();
    
    // Configuration
    private volatile boolean isEnabled = true;
    private volatile int maxAlertsPerMinute = 50;
    private volatile long minIntervalBetweenAlerts = 5000; // 5 seconds
    private final Map<String, Long> lastAlertTime = new ConcurrentHashMap<>();
    
    public AdvancedAlertsSystem() {
        this.telegramService = new TelegramNotificationService();
        this.emailService = new EmailNotificationService();
        this.dashboardService = new DashboardNotificationService();
        this.smsService = new SMSNotificationService();
        
        initializeDefaultAlertRules();
        startAlertProcessor();
        startStatisticsCollector();
        
        System.out.println("🚨 [AdvancedAlertsSystem] ✅ System initialized successfully");
    }
    
    // ==================== PATTERN ALERTS ====================
    
    public void processPatternAlert(AdvancedICTPatternEngine.PatternResult pattern) {
        if (!isEnabled) return;
        
        AlertRule rule = findMatchingRule(pattern);
        if (rule != null && shouldTriggerAlert(pattern, rule)) {
            Alert alert = createPatternAlert(pattern, rule);
            processAlert(alert);
        }
    }
    
    private Alert createPatternAlert(AdvancedICTPatternEngine.PatternResult pattern, AlertRule rule) {
        Alert alert = new Alert();
        alert.setId(generateAlertId());
        alert.setType(AlertType.PATTERN_DETECTION);
        alert.setPriority(calculatePriority(pattern));
        alert.setSymbol(pattern.getSymbol());
        alert.setTitle(createPatternTitle(pattern));
        alert.setMessage(createPatternMessage(pattern));
        alert.setTimestamp(LocalDateTime.now());
        alert.setConfidence(pattern.getConfidence());
        alert.setMetadata(createPatternMetadata(pattern));
        alert.setRuleId(rule.getId());
        return alert;
    }
    
    private String createPatternTitle(AdvancedICTPatternEngine.PatternResult pattern) {
        String patternName = pattern.getPatternType().replace("_", " ");
        String emoji = getPatternEmoji(pattern.getPatternType());
        return String.format("%s %s Pattern Detected - %s", emoji, patternName, pattern.getSymbol());
    }
    
    private String createPatternMessage(AdvancedICTPatternEngine.PatternResult pattern) {
        StringBuilder message = new StringBuilder();
        message.append(String.format("🎯 Pattern: %s\n", pattern.getPatternType().replace("_", " ")));
        message.append(String.format("📊 Confidence: %.1f%%\n", pattern.getConfidence()));
        message.append(String.format("💰 Symbol: %s\n", pattern.getSymbol()));
        message.append(String.format("⏰ Time: %s\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        
        if (pattern.getEntryPrice() != null) {
            message.append(String.format("💵 Entry: %.5f\n", pattern.getEntryPrice()));
        }
        if (pattern.getTargetPrice() != null) {
            message.append(String.format("🎯 Target: %.5f\n", pattern.getTargetPrice()));
        }
        if (pattern.getStopPrice() != null) {
            message.append(String.format("🛡️ Stop: %.5f\n", pattern.getStopPrice()));
        }
        
        return message.toString();
    }
    
    private String getPatternEmoji(String patternType) {
        switch (patternType.toUpperCase()) {
            case "FAIR_VALUE_GAP": return "⚡";
            case "ORDER_BLOCK": return "📦";
            case "LIQUIDITY_SWEEP": return "🌊";
            case "BREAKOUT": return "🚀";
            case "REVERSAL": return "🔄";
            case "ICEBERG": return "🧊";
            case "PERFECT_STORM": return "⛈️";
            default: return "🎯";
        }
    }
    
    // ==================== RISK ALERTS ====================
    
    public void processRiskAlert(RiskEvent riskEvent) {
        if (!isEnabled) return;
        
        if (shouldTriggerRiskAlert(riskEvent)) {
            Alert alert = createRiskAlert(riskEvent);
            processAlert(alert);
        }
    }
    
    private Alert createRiskAlert(RiskEvent riskEvent) {
        Alert alert = new Alert();
        alert.setId(generateAlertId());
        alert.setType(AlertType.RISK_WARNING);
        alert.setPriority(AlertPriority.HIGH);
        alert.setSymbol(riskEvent.getSymbol());
        alert.setTitle("⚠️ Risk Warning - " + riskEvent.getSymbol());
        alert.setMessage(createRiskMessage(riskEvent));
        alert.setTimestamp(LocalDateTime.now());
        alert.setMetadata(createRiskMetadata(riskEvent));
        return alert;
    }
    
    private String createRiskMessage(RiskEvent riskEvent) {
        return String.format("🚨 Risk Alert for %s\n" +
                           "📊 Current Exposure: %.2f%%\n" +
                           "⚠️ Risk Limit: %.2f%%\n" +
                           "💰 P&L: %.2f\n" +
                           "⏰ Time: %s",
                           riskEvent.getSymbol(),
                           riskEvent.getCurrentExposure(),
                           riskEvent.getRiskLimit(),
                           riskEvent.getCurrentPnL(),
                           LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
    
    // ==================== SENTIMENT ALERTS ====================
    
    public void processSentimentAlert(String symbol, SentimentChange sentimentChange) {
        if (!isEnabled) return;
        
        if (isSignificantSentimentChange(sentimentChange)) {
            Alert alert = createSentimentAlert(symbol, sentimentChange);
            processAlert(alert);
        }
    }
    
    private Alert createSentimentAlert(String symbol, SentimentChange sentimentChange) {
        Alert alert = new Alert();
        alert.setId(generateAlertId());
        alert.setType(AlertType.SENTIMENT_CHANGE);
        alert.setPriority(calculateSentimentPriority(sentimentChange));
        alert.setSymbol(symbol);
        alert.setTitle(createSentimentTitle(sentimentChange));
        alert.setMessage(createSentimentMessage(symbol, sentimentChange));
        alert.setTimestamp(LocalDateTime.now());
        alert.setMetadata(createSentimentMetadata(sentimentChange));
        return alert;
    }
    
    private String createSentimentTitle(SentimentChange sentimentChange) {
        String emoji = sentimentChange.getDirection().equals("BULLISH") ? "📈" : "📉";
        return String.format("%s Market Sentiment Shift: %s", emoji, sentimentChange.getChangeType());
    }
    
    private String createSentimentMessage(String symbol, SentimentChange sentimentChange) {
        return String.format("📊 Symbol: %s\n" +
                           "📈 Direction: %s\n" +
                           "📊 Change: %.2f%%\n" +
                           "🎯 Confidence: %.1f%%\n" +
                           "⏰ Time: %s",
                           symbol,
                           sentimentChange.getDirection(),
                           sentimentChange.getChangePercentage(),
                           sentimentChange.getConfidence(),
                           LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
    
    // ==================== SYSTEM ALERTS ====================
    
    public void processSystemAlert(String component, String message, AlertPriority priority) {
        if (!isEnabled) return;
        
        Alert alert = new Alert();
        alert.setId(generateAlertId());
        alert.setType(AlertType.SYSTEM_STATUS);
        alert.setPriority(priority);
        alert.setSymbol("SYSTEM");
        alert.setTitle("⚙️ System Alert - " + component);
        alert.setMessage(message);
        alert.setTimestamp(LocalDateTime.now());
        
        // Java 8 compatible metadata creation
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("component", component);
        metadata.put("priority", priority.name());
        alert.setMetadata(metadata);
        
        processAlert(alert);
    }
    
    // ==================== ALERT PROCESSING ====================
    
    private void processAlert(Alert alert) {
        // Check rate limiting
        if (isRateLimited(alert)) {
            return;
        }
        
        alertQueue.offer(alert);
        alertHistory.add(alert);
        
        // Update statistics
        totalAlerts.incrementAndGet();
        alertTypeCounts.computeIfAbsent(alert.getType().name(), k -> new AtomicLong()).incrementAndGet();
        lastAlertTime.put(alert.getSymbol(), System.currentTimeMillis());
        
        // Send notifications based on priority and settings
        notificationExecutor.submit(() -> {
            try {
                sendNotifications(alert);
                successfulNotifications.incrementAndGet();
                logAlert(alert);
            } catch (Exception e) {
                failedNotifications.incrementAndGet();
                System.err.println("❌ [AdvancedAlertsSystem] Error processing alert: " + e.getMessage());
            }
        });
    }
    
    private void sendNotifications(Alert alert) {
        // Always send to dashboard
        dashboardService.sendNotification(alert);
        
        // Send to Telegram for medium+ priority alerts
        if (alert.getPriority().ordinal() >= AlertPriority.MEDIUM.ordinal()) {
            telegramService.sendAlert(alert);
        }
        
        // Send email for high+ priority alerts
        if (alert.getPriority().ordinal() >= AlertPriority.HIGH.ordinal()) {
            emailService.sendAlert(alert);
        }
        
        // Send SMS for critical alerts
        if (alert.getPriority() == AlertPriority.CRITICAL) {
            smsService.sendAlert(alert);
        }
    }
    
    // ==================== SMART FILTERING ====================
    
    private boolean shouldTriggerAlert(AdvancedICTPatternEngine.PatternResult pattern, AlertRule rule) {
        // Check confidence threshold
        if (pattern.getConfidence() < rule.getMinConfidence()) {
            return false;
        }
        
        // Check cooldown period
        if (isInCooldownPeriod(pattern.getSymbol(), pattern.getPatternType())) {
            return false;
        }
        
        // Check market conditions
        if (!isValidMarketCondition(pattern.getSymbol())) {
            return false;
        }
        
        // Check volume threshold
        if (pattern.getVolume() != null && pattern.getVolume() < rule.getMinVolume()) {
            return false;
        }
        
        return true;
    }
    
    private boolean shouldTriggerRiskAlert(RiskEvent riskEvent) {
        return riskEvent.getCurrentExposure() > riskEvent.getRiskLimit() * 0.8; // 80% of risk limit
    }
    
    private boolean isSignificantSentimentChange(SentimentChange sentimentChange) {
        return Math.abs(sentimentChange.getChangePercentage()) > 15.0; // 15% change threshold
    }
    
    private boolean isRateLimited(Alert alert) {
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastAlertTime.get(alert.getSymbol());
        
        if (lastTime != null && (currentTime - lastTime) < minIntervalBetweenAlerts) {
            return true;
        }
        
        return false;
    }
    
    private boolean isInCooldownPeriod(String symbol, String patternType) {
        String key = symbol + "_" + patternType;
        Long lastTime = lastAlertTime.get(key);
        
        if (lastTime != null) {
            long cooldownPeriod = 300000; // 5 minutes
            return (System.currentTimeMillis() - lastTime) < cooldownPeriod;
        }
        
        return false;
    }
    
    private boolean isValidMarketCondition(String symbol) {
        // Check if market is open and active
        // This would integrate with market data service
        return true; // Placeholder
    }
    
    // ==================== UTILITY METHODS ====================
    
    private AlertRule findMatchingRule(AdvancedICTPatternEngine.PatternResult pattern) {
        return alertRules.values().stream()
            .filter(rule -> rule.getPatternType().equals(pattern.getPatternType()))
            .filter(rule -> rule.getSymbols().contains(pattern.getSymbol()) || rule.getSymbols().contains("*"))
            .findFirst()
            .orElse(null);
    }
    
    private AlertPriority calculatePriority(AdvancedICTPatternEngine.PatternResult pattern) {
        if (pattern.getConfidence() >= 90) return AlertPriority.HIGH;
        if (pattern.getConfidence() >= 80) return AlertPriority.MEDIUM;
        if (pattern.getConfidence() >= 70) return AlertPriority.LOW;
        return AlertPriority.LOW;
    }
    
    private AlertPriority calculateSentimentPriority(SentimentChange sentimentChange) {
        double change = Math.abs(sentimentChange.getChangePercentage());
        if (change >= 30) return AlertPriority.CRITICAL;
        if (change >= 20) return AlertPriority.HIGH;
        if (change >= 15) return AlertPriority.MEDIUM;
        return AlertPriority.LOW;
    }
    
    private String generateAlertId() {
        return "ALERT_" + System.currentTimeMillis() + "_" + ThreadLocalRandom.current().nextInt(1000);
    }
    
    private Map<String, Object> createPatternMetadata(AdvancedICTPatternEngine.PatternResult pattern) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("pattern_type", pattern.getPatternType());
        metadata.put("confidence", pattern.getConfidence());
        metadata.put("volume", pattern.getVolume());
        metadata.put("timeframe", pattern.getTimeframe());
        return metadata;
    }
    
    private Map<String, Object> createRiskMetadata(RiskEvent riskEvent) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("exposure", riskEvent.getCurrentExposure());
        metadata.put("risk_limit", riskEvent.getRiskLimit());
        metadata.put("pnl", riskEvent.getCurrentPnL());
        return metadata;
    }
    
    private Map<String, Object> createSentimentMetadata(SentimentChange sentimentChange) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("direction", sentimentChange.getDirection());
        metadata.put("change_percentage", sentimentChange.getChangePercentage());
        metadata.put("confidence", sentimentChange.getConfidence());
        return metadata;
    }
    
    private void logAlert(Alert alert) {
        System.out.println(String.format("🚨 [AdvancedAlertsSystem] %s - %s (%s)", 
            alert.getPriority(), alert.getTitle(), alert.getSymbol()));
    }
    
    // ==================== INITIALIZATION ====================
    
    private void initializeDefaultAlertRules() {
        // Pattern detection rules
        addAlertRule(new AlertRule("FAIR_VALUE_GAP", 75.0, 1000.0, 
            Arrays.asList("EURUSD", "GBPUSD", "USDJPY", "*")));
        addAlertRule(new AlertRule("ORDER_BLOCK", 80.0, 1500.0, 
            Arrays.asList("EURUSD", "GBPUSD", "USDJPY", "*")));
        addAlertRule(new AlertRule("LIQUIDITY_SWEEP", 85.0, 2000.0, 
            Arrays.asList("EURUSD", "GBPUSD", "USDJPY", "*")));
        addAlertRule(new AlertRule("PERFECT_STORM", 90.0, 3000.0, 
            Arrays.asList("NQ", "ES", "YM", "*")));
        
        System.out.println("🚨 [AdvancedAlertsSystem] Default alert rules initialized");
    }
    
    private void startAlertProcessor() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                processAlertQueue();
            } catch (Exception e) {
                System.err.println("Error in alert processor: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
    
    private void startStatisticsCollector() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                collectStatistics();
            } catch (Exception e) {
                System.err.println("Error collecting statistics: " + e.getMessage());
            }
        }, 60, 60, TimeUnit.SECONDS);
    }
    
    private void processAlertQueue() {
        // Process any remaining alerts in queue
        Alert alert;
        while ((alert = alertQueue.poll()) != null) {
            // Additional processing if needed
        }
    }
    
    private void collectStatistics() {
        // Clean up old alerts from history (keep last 1000)
        if (alertHistory.size() > 1000) {
            alertHistory.subList(0, alertHistory.size() - 1000).clear();
        }
        
        // Log statistics
        System.out.println(String.format("📊 [AdvancedAlertsSystem] Stats - Total: %d, Success: %d, Failed: %d, Queue: %d",
            totalAlerts.get(), successfulNotifications.get(), failedNotifications.get(), alertQueue.size()));
    }
    
    // ==================== PUBLIC API ====================
    
    public void addAlertRule(AlertRule rule) {
        alertRules.put(rule.getId(), rule);
        System.out.println("🚨 [AdvancedAlertsSystem] Added alert rule: " + rule.getPatternType());
    }
    
    public void removeAlertRule(String ruleId) {
        alertRules.remove(ruleId);
        System.out.println("🚨 [AdvancedAlertsSystem] Removed alert rule: " + ruleId);
    }
    
    public void enableSystem(boolean enabled) {
        this.isEnabled = enabled;
        System.out.println("🚨 [AdvancedAlertsSystem] System " + (enabled ? "enabled" : "disabled"));
    }
    
    public void setMaxAlertsPerMinute(int maxAlerts) {
        this.maxAlertsPerMinute = maxAlerts;
    }
    
    public void setMinIntervalBetweenAlerts(long intervalMs) {
        this.minIntervalBetweenAlerts = intervalMs;
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("version", VERSION);
        stats.put("is_enabled", isEnabled);
        stats.put("total_alerts", totalAlerts.get());
        stats.put("successful_notifications", successfulNotifications.get());
        stats.put("failed_notifications", failedNotifications.get());
        stats.put("queue_size", alertQueue.size());
        stats.put("history_size", alertHistory.size());
        stats.put("active_rules", alertRules.size());
        stats.put("alert_type_counts", alertTypeCounts);
        stats.put("success_rate", calculateSuccessRate());
        return stats;
    }
    
    public List<Alert> getRecentAlerts(int count) {
        int size = alertHistory.size();
        int start = Math.max(0, size - count);
        return new ArrayList<>(alertHistory.subList(start, size));
    }
    
    public void shutdown() {
        isEnabled = false;
        notificationExecutor.shutdown();
        scheduler.shutdown();
        System.out.println("🚨 [AdvancedAlertsSystem] System shutdown complete");
    }
    
    private double calculateSuccessRate() {
        long total = totalAlerts.get();
        return total > 0 ? (double) successfulNotifications.get() / total * 100.0 : 0.0;
    }
    
    // ==================== SUPPORT CLASSES ====================
    
    public static class Alert {
        private String id, title, message, symbol, ruleId;
        private AlertType type;
        private AlertPriority priority;
        private LocalDateTime timestamp;
        private double confidence;
        private Map<String, Object> metadata;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        public AlertType getType() { return type; }
        public void setType(AlertType type) { this.type = type; }
        public AlertPriority getPriority() { return priority; }
        public void setPriority(AlertPriority priority) { this.priority = priority; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
    
    public static class AlertRule {
        private String id, patternType;
        private double minConfidence, minVolume;
        private List<String> symbols;
        
        public AlertRule(String patternType, double minConfidence, double minVolume, List<String> symbols) {
            this.id = "RULE_" + patternType + "_" + System.currentTimeMillis();
            this.patternType = patternType;
            this.minConfidence = minConfidence;
            this.minVolume = minVolume;
            this.symbols = symbols;
        }
        
        // Getters
        public String getId() { return id; }
        public String getPatternType() { return patternType; }
        public double getMinConfidence() { return minConfidence; }
        public double getMinVolume() { return minVolume; }
        public List<String> getSymbols() { return symbols; }
    }
    
    public static class RiskEvent {
        private String symbol;
        private double currentExposure, riskLimit, currentPnL;
        
        public RiskEvent(String symbol, double currentExposure, double riskLimit, double currentPnL) {
            this.symbol = symbol;
            this.currentExposure = currentExposure;
            this.riskLimit = riskLimit;
            this.currentPnL = currentPnL;
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public double getCurrentExposure() { return currentExposure; }
        public double getRiskLimit() { return riskLimit; }
        public double getCurrentPnL() { return currentPnL; }
    }
    
    public static class SentimentChange {
        private String direction, changeType;
        private double changePercentage, confidence;
        
        public SentimentChange(String direction, String changeType, double changePercentage, double confidence) {
            this.direction = direction;
            this.changeType = changeType;
            this.changePercentage = changePercentage;
            this.confidence = confidence;
        }
        
        // Getters
        public String getDirection() { return direction; }
        public String getChangeType() { return changeType; }
        public double getChangePercentage() { return changePercentage; }
        public double getConfidence() { return confidence; }
    }
    
    public enum AlertType {
        PATTERN_DETECTION, RISK_WARNING, SENTIMENT_CHANGE, SYSTEM_STATUS, MARKET_EVENT
    }
    
    public enum AlertPriority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    // ==================== NOTIFICATION SERVICES ====================
    
    private static class TelegramNotificationService {
        public void sendAlert(Alert alert) {
            System.out.println("📱 Telegram Alert: " + alert.getTitle());
        }
    }
    
    private static class EmailNotificationService {
        public void sendAlert(Alert alert) {
            System.out.println("📧 Email Alert: " + alert.getTitle());
        }
    }
    
    private static class DashboardNotificationService {
        public void sendNotification(Alert alert) {
            System.out.println("🖥️ Dashboard Alert: " + alert.getTitle());
        }
    }
    
    private static class SMSNotificationService {
        public void sendAlert(Alert alert) {
            System.out.println("📱 SMS Alert: " + alert.getTitle());
        }
    }
} 