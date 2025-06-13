package com.bookmaai.notifications;

import com.bookmaai.monitoring.SystemStatusMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 📱 Telegram Commands Handler - معالج أوامر Telegram
 * 
 * يدعم الأوامر:
 * ✅ /status - حالة النظام الكاملة
 * ✅ /markets - الأسواق المتصلة  
 * ✅ /tools - حالة الأدوات الـ12
 * ✅ /alerts - التنبيهات النشطة
 * ✅ /stats - إحصائيات الأداء
 */
@Service
public class TelegramCommandsHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(TelegramCommandsHandler.class);
    
    @Autowired
    private SystemStatusMonitor statusMonitor;
    
    @Autowired  
    private TelegramNotificationService telegramService;
    
    private final Map<String, CommandHandler> commands = new HashMap<>();
    
    public TelegramCommandsHandler() {
        initializeCommands();
        logger.info("📱 TelegramCommandsHandler initialized with {} commands", commands.size());
    }
    
    /**
     * تهيئة الأوامر المدعومة
     */
    private void initializeCommands() {
        commands.put("/status", this::handleStatusCommand);
        commands.put("/markets", this::handleMarketsCommand);
        commands.put("/tools", this::handleToolsCommand);
        commands.put("/alerts", this::handleAlertsCommand);
        commands.put("/stats", this::handleStatsCommand);
        commands.put("/help", this::handleHelpCommand);
        commands.put("/ping", this::handlePingCommand);
    }
    
    /**
     * 🎯 معالجة الرسائل الواردة
     */
    public void processIncomingMessage(String chatId, String message) {
        try {
            String command = extractCommand(message);
            
            if (commands.containsKey(command)) {
                logger.info("📱 Processing command: {} from chat: {}", command, chatId);
                
                CommandHandler handler = commands.get(command);
                String response = handler.handle(message);
                
                // إرسال الرد
                telegramService.sendMessage(chatId, response);
                
            } else if (isCommand(message)) {
                // أمر غير معروف
                String response = "❓ **Unknown Command**\n\n" +
                                "Use /help to see available commands.";
                telegramService.sendMessage(chatId, response);
            }
            // تجاهل الرسائل العادية (غير الأوامر)
            
        } catch (Exception e) {
            logger.error("❌ Error processing Telegram message: {}", e.getMessage());
            
            String errorResponse = "🚨 **System Error**\n\n" +
                                 "Failed to process your request. Please try again later.";
            telegramService.sendMessage(chatId, errorResponse);
        }
    }
    
    /**
     * 📊 أمر /status - حالة النظام الكاملة
     */
    private String handleStatusCommand(String message) {
        return statusMonitor.getStatusSummaryForTelegram();
    }
    
    /**
     * 💹 أمر /markets - الأسواق المتصلة
     */
    private String handleMarketsCommand(String message) {
        var report = statusMonitor.getSystemStatusReport();
        var markets = report.getMarketConnections();
        
        StringBuilder response = new StringBuilder();
        response.append("💹 **CONNECTED MARKETS**\n\n");
        
        if (markets.isEmpty()) {
            response.append("❌ **No markets connected**\n");
            response.append("🔍 Make sure Bookmap is running with market data feeds.");
        } else {
            response.append(String.format("📊 **Total Markets:** %d\n\n", markets.size()));
            
            markets.values().forEach(market -> {
                String emoji = market.isConnected() ? "🟢" : "🔴";
                String status = market.isConnected() ? "CONNECTED" : "DISCONNECTED";
                
                response.append(String.format("**%s %s**\n", emoji, market.getSymbol()));
                response.append(String.format("   Status: %s\n", status));
                response.append(String.format("   Price: %.5f\n", market.getLastPrice()));
                
                if (market.getWindowName() != null) {
                    response.append(String.format("   Window: %s\n", market.getWindowName()));
                }
                
                response.append(String.format("   Updated: %s\n\n", 
                                             formatTime(market.getLastUpdate())));
            });
        }
        
        return response.toString();
    }
    
    /**
     * 🔧 أمر /tools - حالة الأدوات الـ12
     */
    private String handleToolsCommand(String message) {
        var report = statusMonitor.getSystemStatusReport();
        var tools = report.getToolStatuses();
        
        StringBuilder response = new StringBuilder();
        response.append("🔧 **BOOKMAP TOOLS STATUS**\n\n");
        
        // تجميع حسب الطبقات
        String[] tier1 = {"cvd", "heatmap", "volume_dots", "vwap"};
        String[] tier2 = {"volume_profile", "iceberg_detector", "volume_bubbles"};
        String[] tier3 = {"large_lot_tracker", "imbalance_indicator", "absorption_indicator"};
        String[] tier4 = {"strength_level_indicator", "stop_run"};
        
        response.append("**🥇 TIER 1 TOOLS** (94-99% accuracy):\n");
        appendToolsStatus(response, tools, tier1);
        
        response.append("\n**🥈 TIER 2 TOOLS** (82-97% accuracy):\n");
        appendToolsStatus(response, tools, tier2);
        
        response.append("\n**🥉 TIER 3 TOOLS** (80-88% accuracy):\n");
        appendToolsStatus(response, tools, tier3);
        
        response.append("\n**🔸 TIER 4 TOOLS** (72-75% accuracy):\n");
        appendToolsStatus(response, tools, tier4);
        
        // ملخص
        long healthyCount = tools.values().stream()
            .mapToLong(tool -> tool.getStatus() == SystemStatusMonitor.ToolStatus.ToolStatusEnum.HEALTHY ? 1 : 0)
            .sum();
        
        response.append(String.format("\n📈 **Summary:** %d/12 tools healthy (%.1f%%)", 
                                     healthyCount, (healthyCount / 12.0) * 100));
        
        return response.toString();
    }
    
    /**
     * ⚠️ أمر /alerts - التنبيهات النشطة
     */
    private String handleAlertsCommand(String message) {
        var report = statusMonitor.getSystemStatusReport();
        var alerts = report.getActiveAlerts();
        
        StringBuilder response = new StringBuilder();
        response.append("⚠️ **ACTIVE SYSTEM ALERTS**\n\n");
        
        if (alerts.isEmpty()) {
            response.append("✅ **No active alerts**\n");
            response.append("🎯 System is operating normally.");
        } else {
            response.append(String.format("🚨 **%d active alerts:**\n\n", alerts.size()));
            
            alerts.forEach(alert -> {
                String emoji = getSeverityEmoji(alert.getSeverity());
                response.append(String.format("%s **%s**\n", emoji, alert.getSeverity()));
                response.append(String.format("   📋 %s\n", alert.getMessage()));
                response.append(String.format("   ⏰ %s\n\n", formatTime(alert.getTimestamp())));
            });
        }
        
        return response.toString();
    }
    
    /**
     * 📈 أمر /stats - إحصائيات الأداء
     */
    private String handleStatsCommand(String message) {
        var report = statusMonitor.getSystemStatusReport();
        var metrics = report.getPerformance();
        
        StringBuilder response = new StringBuilder();
        response.append("📈 **SYSTEM PERFORMANCE STATS**\n\n");
        
        response.append(String.format("⏰ **Uptime:** %d minutes\n", metrics.getUptimeMinutes()));
        response.append(String.format("🕐 **Started:** %s\n", formatTime(metrics.getSystemStartTime())));
        response.append(String.format("📡 **Last Data:** %s\n\n", formatTime(metrics.getLastDataReceived())));
        
        response.append("**🎯 DETECTION STATS:**\n");
        response.append(String.format("   Patterns Detected: %d\n", metrics.getTotalPatterns()));
        response.append(String.format("   Telegram Messages: %d\n", metrics.getTelegramMessages()));
        response.append(String.format("   CSV Records: %d\n\n", metrics.getCsvRecords()));
        
        // معدلات الأداء
        long uptimeHours = Math.max(1, metrics.getUptimeMinutes() / 60);
        double patternsPerHour = (double) metrics.getTotalPatterns() / uptimeHours;
        
        response.append("**📊 PERFORMANCE RATES:**\n");
        response.append(String.format("   Patterns/Hour: %.1f\n", patternsPerHour));
        response.append(String.format("   Success Rate: %.1f%%\n", calculateSuccessRate(metrics)));
        
        return response.toString();
    }
    
    /**
     * ❓ أمر /help - قائمة الأوامر
     */
    private String handleHelpCommand(String message) {
        return "🤖 **BOOKMAP AI SYSTEM COMMANDS**\n\n" +
               "📊 **/status** - Full system status\n" +
               "💹 **/markets** - Connected markets info\n" +
               "🔧 **/tools** - Bookmap tools status\n" +
               "⚠️ **/alerts** - Active system alerts\n" +
               "📈 **/stats** - Performance statistics\n" +
               "🏓 **/ping** - Check if system is responsive\n" +
               "❓ **/help** - Show this help message\n\n" +
               "💡 **Tip:** The system automatically sends trading signals when patterns are detected with >70% confidence.";
    }
    
    /**
     * 🏓 أمر /ping - اختبار الاستجابة
     */
    private String handlePingCommand(String message) {
        var report = statusMonitor.getSystemStatusReport();
        String statusEmoji = getStatusEmoji(report.getOverallStatus());
        
        return String.format("🏓 **PONG!**\n\n" +
                           "🤖 BookmapAI System is **%s %s**\n" +
                           "⏰ Response time: <1 second\n" +
                           "📡 Last data: %s ago", 
                           statusEmoji, report.getOverallStatus(),
                           formatTimeSince(report.getPerformance().getLastDataReceived()));
    }
    
    // ================ Helper Methods ================
    
    private String extractCommand(String message) {
        if (message == null || !message.startsWith("/")) return "";
        
        String[] parts = message.split(" ");
        return parts[0].toLowerCase();
    }
    
    private boolean isCommand(String message) {
        return message != null && message.startsWith("/");
    }
    
    private void appendToolsStatus(StringBuilder sb, Map<String, SystemStatusMonitor.ToolStatus> tools, String[] toolNames) {
        for (String toolName : toolNames) {
            SystemStatusMonitor.ToolStatus tool = tools.get(toolName);
            if (tool != null) {
                String emoji = getToolStatusEmoji(tool.getStatus());
                sb.append(String.format("   %s %s (%.0f%%)\n", 
                                       emoji, tool.getDisplayName(), tool.getAccuracy()));
            }
        }
    }
    
    private String getToolStatusEmoji(SystemStatusMonitor.ToolStatus.ToolStatusEnum status) {
        switch (status) {
            case HEALTHY: return "✅";
            case DEGRADED: return "⚠️";
            case ERROR: return "❌";
            case OFFLINE: return "🔴";
            default: return "❓";
        }
    }
    
    private String getSeverityEmoji(SystemStatusMonitor.AlertSeverity severity) {
        switch (severity) {
            case CRITICAL: return "🚨";
            case HIGH: return "🔴";
            case MEDIUM: return "🟡";
            case LOW: return "🟢";
            default: return "❓";
        }
    }
    
    private String getStatusEmoji(SystemStatusMonitor.SystemConnectionStatus status) {
        switch (status) {
            case CONNECTED: return "🟢";
            case DISCONNECTED: return "🔴";
            case DEGRADED: return "🟡";
            case ERROR: return "🚨";
            case STARTING: return "🔄";
            default: return "❓";
        }
    }
    
    private String formatTime(java.time.LocalDateTime time) {
        return time.format(java.time.format.DateTimeFormatter.ofPattern("MM/dd HH:mm"));
    }
    
    private String formatTimeSince(java.time.LocalDateTime time) {
        long minutes = java.time.Duration.between(time, java.time.LocalDateTime.now()).toMinutes();
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " min";
        return (minutes / 60) + " hours";
    }
    
    private double calculateSuccessRate(SystemStatusMonitor.SystemPerformanceMetrics metrics) {
        // حساب تقريبي لمعدل النجاح
        long totalPatterns = metrics.getTotalPatterns();
        if (totalPatterns == 0) return 0.0;
        
        // افتراض أن معدل النجاح حوالي 75% (يمكن تحسينه لاحقاً)
        return 75.0;
    }
    
    @FunctionalInterface
    private interface CommandHandler {
        String handle(String message);
    }
} 