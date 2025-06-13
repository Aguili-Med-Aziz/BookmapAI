package com.bookmaai.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 📊 System Status Monitor - مراقب حالة النظام
 * 
 * يراقب:
 * ✅ حالة الاتصال بـ Bookmap
 * ✅ الأسواق المتصلة  
 * ✅ حالة الـ12 أداة
 * ✅ أداء النظام والأعطال
 */
@Service
public class SystemStatusMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemStatusMonitor.class);
    
    // حالات الاتصال
    private SystemConnectionStatus overallStatus = SystemConnectionStatus.STARTING;
    private final Map<String, MarketConnection> marketConnections = new ConcurrentHashMap<>();
    private final Map<String, ToolStatus> toolStatuses = new ConcurrentHashMap<>();
    
    // إحصائيات الأداء
    private final AtomicLong totalPatternsDetected = new AtomicLong(0);
    private final AtomicLong telegramMessagesSent = new AtomicLong(0);
    private final AtomicLong csvRecordsLogged = new AtomicLong(0);
    private LocalDateTime systemStartTime = LocalDateTime.now();
    private LocalDateTime lastDataReceived = LocalDateTime.now();
    
    // قائمة التنبيهات النشطة
    private final List<SystemAlert> activeAlerts = new ArrayList<>();
    
    public SystemStatusMonitor() {
        initializeToolStatuses();
        logger.info("📊 SystemStatusMonitor initialized");
    }
    
    /**
     * تهيئة حالات الأدوات الـ12
     */
    private void initializeToolStatuses() {
        // Tier 1 Tools
        setToolStatus("cvd", ToolStatus.ToolStatusEnum.HEALTHY, "CVD Tool", 99);
        setToolStatus("heatmap", ToolStatus.ToolStatusEnum.HEALTHY, "Heatmap", 97);
        setToolStatus("volume_dots", ToolStatus.ToolStatusEnum.HEALTHY, "Volume Dots", 96);
        setToolStatus("vwap", ToolStatus.ToolStatusEnum.HEALTHY, "VWAP", 94);
        
        // Tier 2 Tools  
        setToolStatus("volume_profile", ToolStatus.ToolStatusEnum.HEALTHY, "Volume Profile", 97);
        setToolStatus("iceberg_detector", ToolStatus.ToolStatusEnum.HEALTHY, "Iceberg Detector", 85);
        setToolStatus("volume_bubbles", ToolStatus.ToolStatusEnum.HEALTHY, "Volume Bubbles", 82);
        
        // Tier 3 Tools
        setToolStatus("large_lot_tracker", ToolStatus.ToolStatusEnum.HEALTHY, "Large Lot Tracker", 88);
        setToolStatus("imbalance_indicator", ToolStatus.ToolStatusEnum.HEALTHY, "Imbalance Indicator", 80);
        setToolStatus("absorption_indicator", ToolStatus.ToolStatusEnum.HEALTHY, "Absorption Indicator", 85);
        
        // Tier 4 Tools
        setToolStatus("strength_level_indicator", ToolStatus.ToolStatusEnum.HEALTHY, "Strength Level", 75);
        setToolStatus("stop_run", ToolStatus.ToolStatusEnum.HEALTHY, "Stop Run", 72);
    }
    
    /**
     * 🔌 تحديث حالة اتصال السوق
     */
    public void updateMarketConnection(String symbol, boolean isConnected, 
                                     String windowName, double lastPrice, 
                                     LocalDateTime lastUpdate) {
        
        MarketConnection connection = marketConnections.computeIfAbsent(symbol, 
            s -> new MarketConnection(symbol));
        
        connection.setConnected(isConnected);
        connection.setWindowName(windowName);
        connection.setLastPrice(lastPrice);
        connection.setLastUpdate(lastUpdate);
        
        if (isConnected) {
            lastDataReceived = LocalDateTime.now();
            removeAlert(AlertType.MARKET_DISCONNECTED, symbol);
        } else {
            addAlert(new SystemAlert(
                AlertType.MARKET_DISCONNECTED, 
                "Market " + symbol + " disconnected", 
                AlertSeverity.HIGH
            ));
        }
        
        updateOverallStatus();
        logger.debug("📊 Market {} connection updated: {}", symbol, isConnected);
    }
    
    /**
     * 🔧 تحديث حالة الأداة
     */
    public void updateToolStatus(String toolName, ToolStatus.ToolStatusEnum status, String description, double accuracy) {
        setToolStatus(toolName, status, description, accuracy);
        
        if (status == ToolStatus.ToolStatusEnum.ERROR || status == ToolStatus.ToolStatusEnum.DEGRADED) {
            addAlert(new SystemAlert(
                AlertType.TOOL_MALFUNCTION,
                "Tool " + toolName + " is " + status + ": " + description,
                status == ToolStatus.ToolStatusEnum.ERROR ? AlertSeverity.HIGH : AlertSeverity.MEDIUM
            ));
        } else {
            removeAlert(AlertType.TOOL_MALFUNCTION, toolName);
        }
        
        updateOverallStatus();
    }
    
    /**
     * 📈 تحديث إحصائيات الأداء  
     */
    public void recordPatternDetection() {
        totalPatternsDetected.incrementAndGet();
        lastDataReceived = LocalDateTime.now();
    }
    
    public void recordTelegramMessage() {
        telegramMessagesSent.incrementAndGet();
    }
    
    public void recordCSVEntry() {
        csvRecordsLogged.incrementAndGet();
    }
    
    /**
     * 📊 الحصول على تقرير حالة شامل
     */
    public SystemStatusReport getSystemStatusReport() {
        return new SystemStatusReport(
            overallStatus,
            new HashMap<>(marketConnections),
            new HashMap<>(toolStatuses),
            new SystemPerformanceMetrics(
                totalPatternsDetected.get(),
                telegramMessagesSent.get(),
                csvRecordsLogged.get(),
                systemStartTime,
                lastDataReceived,
                calculateUptime()
            ),
            new ArrayList<>(activeAlerts)
        );
    }
    
    /**
     * 📱 تقرير مبسط للـ Telegram
     */
    public String getStatusSummaryForTelegram() {
        StringBuilder report = new StringBuilder();
        
        // حالة النظام العامة
        report.append("🤖 **BOOKMAP AI SYSTEM STATUS**\n\n");
        report.append(String.format("📊 **Overall Status:** %s %s\n", 
                                   getStatusEmoji(overallStatus), overallStatus));
        report.append(String.format("⏰ **Uptime:** %s\n", formatUptime(calculateUptime())));
        report.append(String.format("📡 **Last Data:** %s ago\n\n", 
                                   formatTimeSince(lastDataReceived)));
        
        // الأسواق المتصلة
        report.append("💹 **CONNECTED MARKETS:**\n");
        if (marketConnections.isEmpty()) {
            report.append("   ❌ No markets connected\n");
        } else {
            marketConnections.values().forEach(market -> {
                String emoji = market.isConnected() ? "✅" : "❌";
                report.append(String.format("   %s **%s** - %.5f\n", 
                                          emoji, market.getSymbol(), market.getLastPrice()));
                if (market.getWindowName() != null) {
                    report.append(String.format("       📋 Window: %s\n", market.getWindowName()));
                }
            });
        }
        
        // حالة الأدوات
        report.append("\n🔧 **TOOLS STATUS:**\n");
        long healthyTools = toolStatuses.values().stream()
            .mapToLong(tool -> tool.getStatus() == ToolStatus.ToolStatusEnum.HEALTHY ? 1 : 0).sum();
        report.append(String.format("   ✅ Healthy: %d/12 tools\n", healthyTools));
        
        // الأدوات المعطلة
        toolStatuses.values().stream()
            .filter(tool -> tool.getStatus() != ToolStatus.ToolStatusEnum.HEALTHY)
            .forEach(tool -> {
                String emoji = tool.getStatus() == ToolStatus.ToolStatusEnum.ERROR ? "❌" : "⚠️";
                report.append(String.format("   %s %s: %s\n", 
                                          emoji, tool.getDisplayName(), tool.getStatus()));
            });
        
        // الإحصائيات
        report.append("\n📈 **PERFORMANCE:**\n");
        report.append(String.format("   🎯 Patterns: %d\n", totalPatternsDetected.get()));
        report.append(String.format("   📱 Telegrams: %d\n", telegramMessagesSent.get()));
        report.append(String.format("   📊 CSV Records: %d\n", csvRecordsLogged.get()));
        
        // التنبيهات النشطة
        if (!activeAlerts.isEmpty()) {
            report.append("\n⚠️ **ACTIVE ALERTS:**\n");
            activeAlerts.forEach(alert -> {
                String emoji = alert.getSeverity() == AlertSeverity.HIGH ? "🚨" : "⚠️";
                report.append(String.format("   %s %s\n", emoji, alert.getMessage()));
            });
        }
        
        return report.toString();
    }
    
    /**
     * 🔍 تحديث الحالة العامة للنظام
     */
    private void updateOverallStatus() {
        boolean hasConnectedMarkets = marketConnections.values().stream()
            .anyMatch(MarketConnection::isConnected);
        boolean hasHealthyTools = toolStatuses.values().stream()
            .anyMatch(tool -> tool.getStatus() == ToolStatus.ToolStatusEnum.HEALTHY);
        boolean hasHighSeverityAlerts = activeAlerts.stream()
            .anyMatch(alert -> alert.getSeverity() == AlertSeverity.HIGH);
        
        if (hasHighSeverityAlerts) {
            overallStatus = SystemConnectionStatus.ERROR;
        } else if (!hasConnectedMarkets) {
            overallStatus = SystemConnectionStatus.DISCONNECTED;
        } else if (!hasHealthyTools) {
            overallStatus = SystemConnectionStatus.DEGRADED;
        } else {
            overallStatus = SystemConnectionStatus.CONNECTED;
        }
    }
    
    /**
     * Helper methods
     */
    private void setToolStatus(String toolName, ToolStatus.ToolStatusEnum status, String displayName, double accuracy) {
        toolStatuses.put(toolName, new ToolStatus(toolName, status, displayName, accuracy));
    }
    
    private void addAlert(SystemAlert alert) {
        // تجنب التكرار
        if (activeAlerts.stream().noneMatch(a -> 
            a.getType() == alert.getType() && a.getMessage().equals(alert.getMessage()))) {
            activeAlerts.add(alert);
            logger.warn("🚨 New alert: {}", alert.getMessage());
        }
    }
    
    private void removeAlert(AlertType type, String context) {
        activeAlerts.removeIf(alert -> 
            alert.getType() == type && alert.getMessage().contains(context));
    }
    
    private long calculateUptime() {
        return java.time.Duration.between(systemStartTime, LocalDateTime.now()).toMinutes();
    }
    
    private String getStatusEmoji(SystemConnectionStatus status) {
        switch (status) {
            case CONNECTED: return "🟢";
            case DISCONNECTED: return "🔴";
            case DEGRADED: return "🟡";
            case ERROR: return "🚨";
            case STARTING: return "🔄";
            default: return "❓";
        }
    }
    
    private String formatUptime(long minutes) {
        if (minutes < 60) return minutes + " minutes";
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        return String.format("%d hours, %d minutes", hours, remainingMinutes);
    }
    
    private String formatTimeSince(LocalDateTime time) {
        long minutes = java.time.Duration.between(time, LocalDateTime.now()).toMinutes();
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " minutes";
        return (minutes / 60) + " hours";
    }
    
    // ================ Data Classes ================
    
    public enum SystemConnectionStatus {
        STARTING, CONNECTED, DISCONNECTED, DEGRADED, ERROR
    }
    
    public enum AlertType {
        MARKET_DISCONNECTED, TOOL_MALFUNCTION, SLOW_PERFORMANCE, DATA_INTERRUPTION
    }
    
    public enum AlertSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public static class MarketConnection {
        private final String symbol;
        private boolean connected;
        private String windowName;
        private double lastPrice;
        private LocalDateTime lastUpdate;
        
        public MarketConnection(String symbol) {
            this.symbol = symbol;
            this.connected = false;
            this.lastUpdate = LocalDateTime.now();
        }
        
        // Getters and setters
        public String getSymbol() { return symbol; }
        public boolean isConnected() { return connected; }
        public void setConnected(boolean connected) { this.connected = connected; }
        public String getWindowName() { return windowName; }
        public void setWindowName(String windowName) { this.windowName = windowName; }
        public double getLastPrice() { return lastPrice; }
        public void setLastPrice(double lastPrice) { this.lastPrice = lastPrice; }
        public LocalDateTime getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
    }
    
    public static class ToolStatus {
        private final String toolName;
        private ToolStatusEnum status;
        private final String displayName;
        private final double accuracy;
        
        public ToolStatus(String toolName, ToolStatusEnum status, String displayName, double accuracy) {
            this.toolName = toolName;
            this.status = status;
            this.displayName = displayName;
            this.accuracy = accuracy;
        }
        
        public enum ToolStatusEnum {
            HEALTHY, DEGRADED, ERROR, OFFLINE
        }
        
        // Getters
        public String getToolName() { return toolName; }
        public ToolStatusEnum getStatus() { return status; }
        public String getDisplayName() { return displayName; }
        public double getAccuracy() { return accuracy; }
    }
    
    public static class SystemAlert {
        private final AlertType type;
        private final String message;
        private final AlertSeverity severity;
        private final LocalDateTime timestamp;
        
        public SystemAlert(AlertType type, String message, AlertSeverity severity) {
            this.type = type;
            this.message = message;
            this.severity = severity;
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters
        public AlertType getType() { return type; }
        public String getMessage() { return message; }
        public AlertSeverity getSeverity() { return severity; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    public static class SystemPerformanceMetrics {
        private final long totalPatterns;
        private final long telegramMessages;
        private final long csvRecords;
        private final LocalDateTime systemStartTime;
        private final LocalDateTime lastDataReceived;
        private final long uptimeMinutes;
        
        public SystemPerformanceMetrics(long totalPatterns, long telegramMessages, 
                                      long csvRecords, LocalDateTime systemStartTime,
                                      LocalDateTime lastDataReceived, long uptimeMinutes) {
            this.totalPatterns = totalPatterns;
            this.telegramMessages = telegramMessages;
            this.csvRecords = csvRecords;
            this.systemStartTime = systemStartTime;
            this.lastDataReceived = lastDataReceived;
            this.uptimeMinutes = uptimeMinutes;
        }
        
        // Getters
        public long getTotalPatterns() { return totalPatterns; }
        public long getTelegramMessages() { return telegramMessages; }
        public long getCsvRecords() { return csvRecords; }
        public LocalDateTime getSystemStartTime() { return systemStartTime; }
        public LocalDateTime getLastDataReceived() { return lastDataReceived; }
        public long getUptimeMinutes() { return uptimeMinutes; }
    }
    
    public static class SystemStatusReport {
        private final SystemConnectionStatus overallStatus;
        private final Map<String, MarketConnection> marketConnections;
        private final Map<String, ToolStatus> toolStatuses;
        private final SystemPerformanceMetrics performance;
        private final List<SystemAlert> activeAlerts;
        
        public SystemStatusReport(SystemConnectionStatus overallStatus, 
                                Map<String, MarketConnection> marketConnections,
                                Map<String, ToolStatus> toolStatuses,
                                SystemPerformanceMetrics performance,
                                List<SystemAlert> activeAlerts) {
            this.overallStatus = overallStatus;
            this.marketConnections = marketConnections;
            this.toolStatuses = toolStatuses;
            this.performance = performance;
            this.activeAlerts = activeAlerts;
        }
        
        // Getters
        public SystemConnectionStatus getOverallStatus() { return overallStatus; }
        public Map<String, MarketConnection> getMarketConnections() { return marketConnections; }
        public Map<String, ToolStatus> getToolStatuses() { return toolStatuses; }
        public SystemPerformanceMetrics getPerformance() { return performance; }
        public List<SystemAlert> getActiveAlerts() { return activeAlerts; }
    }
} 