package com.bookmaai.core.enhanced;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Advanced Alerts & Notification Manager v2.0
 * Features:
 * - Real-time pattern alerts
 * - Risk management warnings
 * - Market sentiment notifications
 * - Multi-channel delivery (Telegram, Email, Dashboard)
 * - Smart filtering and prioritization
 * - Performance tracking
 */
public class AdvancedAlertsManager {
    
    private static final String VERSION = "2.0-Enhanced";
    private final Map<String, AlertRule> alertRules = new ConcurrentHashMap<>();
    private final Queue<Alert> alertQueue = new ConcurrentLinkedQueue<>();
    private final ExecutorService notificationExecutor = Executors.newFixedThreadPool(5);
    private final TelegramNotificationService telegramService;
    private final EmailNotificationService emailService;
    private final DashboardNotificationService dashboardService;
    
    public AdvancedAlertsManager() {
        this.telegramService = new TelegramNotificationService();
        this.emailService = new EmailNotificationService();
        this.dashboardService = new DashboardNotificationService();
        initializeDefaultAlertRules();
    }
    
    // ==================== PATTERN ALERTS ====================
    
    public void processPatternAlert(AdvancedICTPatternEngine.PatternResult pattern) {
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
        return alert;
    }
    
    private String createPatternTitle(AdvancedICTPatternEngine.PatternResult pattern) {
        return String.format("🎯 %s Pattern Detected - %s", 
            pattern.getPatternType().replace("_", " "), 
            pattern.getSymbol());
    }
    
    private String createPatternMessage(AdvancedICTPatternEngine.PatternResult pattern) {
        StringBuilder message = new StringBuilder();
        message.append("📊 **Pattern Details:**\n");
        message.append("• Type: ").append(pattern.getPatternType()).append("\n");
        message.append("• Symbol: ").append(pattern.getSymbol()).append("\n");
        message.append("• Confidence: ").append(String.format("%.1f%%", pattern.getConfidence())).append("\n");
        message.append("• Strength: ").append(String.format("%.1f/100", pattern.getStrength())).append("\n");
        message.append("• Time: ").append(pattern.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n");
        
        // Add specific pattern information
        if (pattern instanceof AdvancedICTPatternEngine.FairValueGap) {
            AdvancedICTPatternEngine.FairValueGap fvg = (AdvancedICTPatternEngine.FairValueGap) pattern;
            message.append("\n🎯 **FVG Levels:**\n");
            message.append("• Upper: ").append(String.format("%.5f", fvg.getUpperLevel())).append("\n");
            message.append("• Lower: ").append(String.format("%.5f", fvg.getLowerLevel())).append("\n");
            message.append("• Direction: ").append(fvg.getDirection()).append("\n");
        }
        
        if (pattern instanceof AdvancedICTPatternEngine.OrderBlock) {
            AdvancedICTPatternEngine.OrderBlock ob = (AdvancedICTPatternEngine.OrderBlock) pattern;
            message.append("\n🧱 **Order Block:**\n");
            message.append("• High: ").append(String.format("%.5f", ob.getHighLevel())).append("\n");
            message.append("• Low: ").append(String.format("%.5f", ob.getLowLevel())).append("\n");
            message.append("• Type: ").append(ob.getDirection()).append("\n");
        }
        
        // Add trading recommendations
        message.append("\n💡 **Trading Suggestion:**\n");
        message.append(generateTradingSuggestion(pattern));
        
        return message.toString();
    }
    
    // ==================== RISK MANAGEMENT ALERTS ====================
    
    public void processRiskAlert(String symbol, RiskEvent riskEvent) {
        if (shouldTriggerRiskAlert(riskEvent)) {
            Alert alert = createRiskAlert(symbol, riskEvent);
            processAlert(alert);
        }
    }
    
    private Alert createRiskAlert(String symbol, RiskEvent riskEvent) {
        Alert alert = new Alert();
        alert.setId(generateAlertId());
        alert.setType(AlertType.RISK_WARNING);
        alert.setPriority(AlertPriority.HIGH);
        alert.setSymbol(symbol);
        alert.setTitle(createRiskTitle(riskEvent));
        alert.setMessage(createRiskMessage(symbol, riskEvent));
        alert.setTimestamp(LocalDateTime.now());
        alert.setMetadata(createRiskMetadata(riskEvent));
        return alert;
    }
    
    private String createRiskTitle(RiskEvent riskEvent) {
        return String.format("⚠️ Risk Alert: %s", riskEvent.getRiskType());
    }
    
    private String createRiskMessage(String symbol, RiskEvent riskEvent) {
        StringBuilder message = new StringBuilder();
        message.append("🚨 **Risk Warning:**\n");
        message.append("• Symbol: ").append(symbol).append("\n");
        message.append("• Risk Type: ").append(riskEvent.getRiskType()).append("\n");
        message.append("• Severity: ").append(riskEvent.getSeverity()).append("\n");
        message.append("• Current Exposure: ").append(String.format("%.2f%%", riskEvent.getCurrentExposure())).append("\n");
        message.append("• Risk Limit: ").append(String.format("%.2f%%", riskEvent.getRiskLimit())).append("\n");
        
        message.append("\n🎯 **Recommended Actions:**\n");
        message.append(generateRiskRecommendations(riskEvent));
        
        return message.toString();
    }
    
    // ==================== SENTIMENT ALERTS ====================
    
    public void processSentimentAlert(String symbol, SentimentChange sentimentChange) {
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
    
    // ==================== ALERT PROCESSING ====================
    
    private void processAlert(Alert alert) {
        alertQueue.offer(alert);
        
        // Send notifications based on priority and settings
        notificationExecutor.submit(() -> {
            try {
                sendNotifications(alert);
                logAlert(alert);
                updateAlertStatistics(alert);
            } catch (Exception e) {
                System.err.println("Error processing alert: " + e.getMessage());
            }
        });
    }
    
    private void sendNotifications(Alert alert) {
        // Always send to dashboard
        dashboardService.sendNotification(alert);
        
        // Send to Telegram for high priority alerts
        if (alert.getPriority() == AlertPriority.HIGH || alert.getPriority() == AlertPriority.CRITICAL) {
            telegramService.sendAlert(alert);
        }
        
        // Send email for critical alerts
        if (alert.getPriority() == AlertPriority.CRITICAL) {
            emailService.sendAlert(alert);
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
        
        return true;
    }
    
    private boolean shouldTriggerRiskAlert(RiskEvent riskEvent) {
        return riskEvent.getCurrentExposure() > riskEvent.getRiskLimit() * 0.8; // 80% of risk limit
    }
    
    private boolean isSignificantSentimentChange(SentimentChange sentimentChange) {
        return Math.abs(sentimentChange.getChangePercentage()) > 15.0; // 15% change threshold
    }
    
    // ==================== UTILITY METHODS ====================
    
    private void initializeDefaultAlertRules() {
        // Fair Value Gap alerts
        alertRules.put("FVG_BULLISH", new AlertRule("FVG_BULLISH", 80.0, true));
        alertRules.put("FVG_BEARISH", new AlertRule("FVG_BEARISH", 80.0, true));
        
        // Order Block alerts
        alertRules.put("ORDER_BLOCK_BULLISH", new AlertRule("ORDER_BLOCK_BULLISH", 85.0, true));
        alertRules.put("ORDER_BLOCK_BEARISH", new AlertRule("ORDER_BLOCK_BEARISH", 85.0, true));
        
        // Structure Break alerts
        alertRules.put("BOS_BULLISH", new AlertRule("BOS_BULLISH", 75.0, true));
        alertRules.put("BOS_BEARISH", new AlertRule("BOS_BEARISH", 75.0, true));
        
        // Liquidity Sweep alerts
        alertRules.put("LIQUIDITY_SWEEP_BUY_SIDE", new AlertRule("LIQUIDITY_SWEEP_BUY_SIDE", 82.0, true));
        alertRules.put("LIQUIDITY_SWEEP_SELL_SIDE", new AlertRule("LIQUIDITY_SWEEP_SELL_SIDE", 82.0, true));
    }
    
    private AlertRule findMatchingRule(AdvancedICTPatternEngine.PatternResult pattern) {
        return alertRules.get(pattern.getPatternType());
    }
    
    private AlertPriority calculatePriority(AdvancedICTPatternEngine.PatternResult pattern) {
        double confidence = pattern.getConfidence();
        if (confidence >= 90.0) return AlertPriority.CRITICAL;
        if (confidence >= 85.0) return AlertPriority.HIGH;
        if (confidence >= 75.0) return AlertPriority.MEDIUM;
        return AlertPriority.LOW;
    }
    
    private AlertPriority calculateSentimentPriority(SentimentChange sentimentChange) {
        double changePercentage = Math.abs(sentimentChange.getChangePercentage());
        if (changePercentage >= 30.0) return AlertPriority.CRITICAL;
        if (changePercentage >= 20.0) return AlertPriority.HIGH;
        if (changePercentage >= 15.0) return AlertPriority.MEDIUM;
        return AlertPriority.LOW;
    }
    
    private String generateAlertId() {
        return "ALERT_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    private String generateTradingSuggestion(AdvancedICTPatternEngine.PatternResult pattern) {
        StringBuilder suggestion = new StringBuilder();
        
        if (pattern instanceof AdvancedICTPatternEngine.FairValueGap) {
            AdvancedICTPatternEngine.FairValueGap fvg = (AdvancedICTPatternEngine.FairValueGap) pattern;
            if (fvg.getDirection().equals("BULLISH")) {
                suggestion.append("• Consider LONG entry on gap retest\n");
                suggestion.append("• Stop Loss: Below gap low\n");
                suggestion.append("• Take Profit: Next resistance level");
            } else {
                suggestion.append("• Consider SHORT entry on gap retest\n");
                suggestion.append("• Stop Loss: Above gap high\n");
                suggestion.append("• Take Profit: Next support level");
            }
        } else if (pattern instanceof AdvancedICTPatternEngine.OrderBlock) {
            AdvancedICTPatternEngine.OrderBlock ob = (AdvancedICTPatternEngine.OrderBlock) pattern;
            if (ob.getDirection().equals("BULLISH")) {
                suggestion.append("• Watch for rejection at order block\n");
                suggestion.append("• Entry: On bounce from block\n");
                suggestion.append("• Risk/Reward: 1:2 minimum");
            } else {
                suggestion.append("• Watch for rejection at order block\n");
                suggestion.append("• Entry: On rejection from block\n");
                suggestion.append("• Risk/Reward: 1:2 minimum");
            }
        } else {
            suggestion.append("• Wait for confirmation\n");
            suggestion.append("• Use proper risk management\n");
            suggestion.append("• Consider market context");
        }
        
        return suggestion.toString();
    }
    
    private String generateRiskRecommendations(RiskEvent riskEvent) {
        StringBuilder recommendations = new StringBuilder();
        
        switch (riskEvent.getRiskType()) {
            case "OVEREXPOSURE":
                recommendations.append("• Reduce position sizes\n");
                recommendations.append("• Close profitable positions\n");
                recommendations.append("• Avoid new trades until exposure decreases");
                break;
            case "CORRELATION_RISK":
                recommendations.append("• Diversify across different assets\n");
                recommendations.append("• Check position correlations\n");
                recommendations.append("• Consider hedging strategies");
                break;
            case "VOLATILITY_SPIKE":
                recommendations.append("• Reduce leverage\n");
                recommendations.append("• Widen stop losses\n");
                recommendations.append("• Consider closing sensitive positions");
                break;
            default:
                recommendations.append("• Review current positions\n");
                recommendations.append("• Apply conservative approach\n");
                recommendations.append("• Monitor market conditions closely");
        }
        
        return recommendations.toString();
    }
    
    private boolean isInCooldownPeriod(String symbol, String patternType) {
        // Implementation would check if similar alert was sent recently
        return false; // Simplified
    }
    
    private boolean isValidMarketCondition(String symbol) {
        // Implementation would check if market is open and liquid
        return true; // Simplified
    }
    
    private void logAlert(Alert alert) {
        System.out.println(String.format("[%s] %s - %s: %s", 
            alert.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            alert.getPriority(), alert.getSymbol(), alert.getTitle()));
    }
    
    private void updateAlertStatistics(Alert alert) {
        // Implementation would update alert performance metrics
    }
    
    private Map<String, Object> createPatternMetadata(AdvancedICTPatternEngine.PatternResult pattern) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("pattern_type", pattern.getPatternType());
        metadata.put("confidence", pattern.getConfidence());
        metadata.put("strength", pattern.getStrength());
        return metadata;
    }
    
    private Map<String, Object> createRiskMetadata(RiskEvent riskEvent) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("risk_type", riskEvent.getRiskType());
        metadata.put("severity", riskEvent.getSeverity());
        metadata.put("current_exposure", riskEvent.getCurrentExposure());
        return metadata;
    }
    
    private Map<String, Object> createSentimentMetadata(SentimentChange sentimentChange) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("direction", sentimentChange.getDirection());
        metadata.put("change_percentage", sentimentChange.getChangePercentage());
        metadata.put("change_type", sentimentChange.getChangeType());
        return metadata;
    }
    
    private String createSentimentMessage(String symbol, SentimentChange sentimentChange) {
        StringBuilder message = new StringBuilder();
        message.append("💭 **Sentiment Analysis:**\n");
        message.append("• Symbol: ").append(symbol).append("\n");
        message.append("• Direction: ").append(sentimentChange.getDirection()).append("\n");
        message.append("• Change: ").append(String.format("%.1f%%", sentimentChange.getChangePercentage())).append("\n");
        message.append("• Type: ").append(sentimentChange.getChangeType()).append("\n");
        message.append("• Time: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n");
        
        message.append("\n🎯 **Market Impact:**\n");
        if (sentimentChange.getDirection().equals("BULLISH")) {
            message.append("• Increased buying interest\n");
            message.append("• Potential upward pressure\n");
            message.append("• Watch for momentum continuation");
        } else {
            message.append("• Increased selling pressure\n");
            message.append("• Potential downward pressure\n");
            message.append("• Watch for support levels");
        }
        
        return message.toString();
    }
    
    // ==================== PUBLIC API ====================
    
    public List<Alert> getRecentAlerts(int count) {
        return alertQueue.stream()
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .limit(count)
            .collect(ArrayList::new, (list, alert) -> list.add(alert), ArrayList::addAll);
    }
    
    public Map<String, Object> getAlertStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("version", VERSION);
        stats.put("total_alerts", alertQueue.size());
        stats.put("active_rules", alertRules.size());
        stats.put("notification_channels", 3);
        return stats;
    }
    
    // ==================== SUPPORT CLASSES ====================
    
    public static class Alert {
        private String id, title, message, symbol;
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
        private String patternType;
        private double minConfidence;
        private boolean enabled;
        
        public AlertRule(String patternType, double minConfidence, boolean enabled) {
            this.patternType = patternType;
            this.minConfidence = minConfidence;
            this.enabled = enabled;
        }
        
        public String getPatternType() { return patternType; }
        public double getMinConfidence() { return minConfidence; }
        public boolean isEnabled() { return enabled; }
    }
    
    public static class RiskEvent {
        private String riskType, severity;
        private double currentExposure, riskLimit;
        
        public RiskEvent(String riskType, String severity, double exposure, double limit) {
            this.riskType = riskType;
            this.severity = severity;
            this.currentExposure = exposure;
            this.riskLimit = limit;
        }
        
        public String getRiskType() { return riskType; }
        public String getSeverity() { return severity; }
        public double getCurrentExposure() { return currentExposure; }
        public double getRiskLimit() { return riskLimit; }
    }
    
    public static class SentimentChange {
        private String direction, changeType;
        private double changePercentage;
        
        public SentimentChange(String direction, String changeType, double changePercentage) {
            this.direction = direction;
            this.changeType = changeType;
            this.changePercentage = changePercentage;
        }
        
        public String getDirection() { return direction; }
        public String getChangeType() { return changeType; }
        public double getChangePercentage() { return changePercentage; }
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
} 