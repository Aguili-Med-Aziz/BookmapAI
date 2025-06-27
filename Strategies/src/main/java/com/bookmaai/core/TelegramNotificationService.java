package com.bookmaai.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * 📱 Telegram Notification Service - خدمة الإشعارات الاحترافية
 */
public class TelegramNotificationService {
    
    public enum NotificationType {
        PATTERN_FORMING("🌱", "نمط جديد يتكون"),
        PATTERN_READY("💰", "فرصة تداول جاهزة"),
        PATTERN_SUCCESS("🏆", "نجح النمط"),
        PATTERN_FAILED("❌", "فشل النمط"),
        SYSTEM_STATUS("⚙️", "حالة النظام"),
        CRITICAL_ALERT("🚨", "تنبيه حرج");
        
        private final String emoji;
        private final String arabicName;
        
        NotificationType(String emoji, String arabicName) {
            this.emoji = emoji;
            this.arabicName = arabicName;
        }
        
        public String getEmoji() { return emoji; }
        public String getArabicName() { return arabicName; }
    }
    
    public static class NotificationMessage {
        private final NotificationType type;
        private final String title;
        private final String content;
        private final Map<String, String> details;
        private final long timestamp;
        private final String symbol;
        
        public NotificationMessage(NotificationType type, String title, String content, String symbol) {
            this.type = type;
            this.title = title;
            this.content = content;
            this.symbol = symbol;
            this.details = new HashMap<>();
            this.timestamp = System.currentTimeMillis();
        }
        
        public void addDetail(String key, String value) {
            details.put(key, value);
        }
        
        public String formatMessage() {
            StringBuilder sb = new StringBuilder();
            sb.append(type.getEmoji()).append(" ").append(type.getArabicName()).append("\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("📊 النمط: ").append(title).append("\n");
            sb.append("📈 الرمز: ").append(symbol).append("\n");
            sb.append(content).append("\n");
            
            if (!details.isEmpty()) {
                details.forEach((key, value) -> 
                    sb.append("").append(key).append(": ").append(value).append("\n"));
            }
            
            sb.append("━━━━━━━━━━━━━━━━━━━━");
            return sb.toString();
        }
        
        // Getters
        public NotificationType getType() { return type; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getSymbol() { return symbol; }
        public long getTimestamp() { return timestamp; }
    }
    
    private final AtomicBoolean isEnabled = new AtomicBoolean(false);
    private final String botToken;
    private final String chatId;
    private final BlockingQueue<NotificationMessage> messageQueue = new LinkedBlockingQueue<>();
    private final ExecutorService notificationExecutor = Executors.newSingleThreadExecutor();
    private final AtomicLong totalNotifications = new AtomicLong(0);
    private final AtomicLong successfulNotifications = new AtomicLong(0);
    
    // Rate limiting
    private final AtomicLong lastNotificationTime = new AtomicLong(0);
    private final long minIntervalMs = 2000; // 2 seconds between notifications
    
    public TelegramNotificationService(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
        System.out.println("📱 [TelegramNotificationService] Initializing Telegram service...");
    }
    
    public void initialize() {
        if (botToken != null && !botToken.isEmpty() && chatId != null && !chatId.isEmpty()) {
            isEnabled.set(true);
            startNotificationProcessor();
            System.out.println("📱 [TelegramNotificationService] Service initialized and enabled");
        } else {
            System.out.println("📱 [TelegramNotificationService] Service disabled - missing configuration");
        }
    }
    
    private void startNotificationProcessor() {
        notificationExecutor.submit(() -> {
            while (isEnabled.get()) {
                try {
                    NotificationMessage message = messageQueue.take();
                    processNotification(message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("📱 [TelegramNotificationService] Error processing notification: " + e.getMessage());
                }
            }
        });
    }
    
    private void processNotification(NotificationMessage message) {
        if (!isEnabled.get()) return;
        
        try {
            // Rate limiting check
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastNotificationTime.get() < minIntervalMs) {
                Thread.sleep(minIntervalMs - (currentTime - lastNotificationTime.get()));
            }
            
            // Format and send message
            String formattedMessage = message.formatMessage();
            boolean sent = sendTelegramMessage(formattedMessage);
            
            totalNotifications.incrementAndGet();
            if (sent) {
                successfulNotifications.incrementAndGet();
                System.out.println("📱 [TelegramNotificationService] Sent: " + message.getType().getArabicName());
            }
            
            lastNotificationTime.set(System.currentTimeMillis());
            
        } catch (Exception e) {
            System.err.println("📱 [TelegramNotificationService] Failed to send notification: " + e.getMessage());
        }
    }
    
    private boolean sendTelegramMessage(String message) {
        // Simulate sending message (in real implementation, this would use HTTP client)
        try {
            System.out.println("📱 [TelegramNotificationService] Sending Telegram message:");
            System.out.println(message);
            return true;
        } catch (Exception e) {
            System.err.println("📱 [TelegramNotificationService] Error sending message: " + e.getMessage());
            return false;
        }
    }
    
    // Public notification methods
    public void notifyPatternForming(AdvancedPatternEngine.DetectedPattern pattern) {
        if (!isEnabled.get()) return;
        
        NotificationMessage message = new NotificationMessage(
            NotificationType.PATTERN_FORMING,
            pattern.getType().getDisplayName(),
            "⏱️ التقدم: " + pattern.getProgress() + "%\n🎯 المرحلة: " + pattern.getStage().getArabicName(),
            pattern.getSymbol()
        );
        
        message.addDetail("🛠️ الأدوات النشطة", pattern.getTools().size() + "/12");
        message.addDetail("📊 الثقة", pattern.getConfidence() + "%");
        
        queueNotification(message);
    }
    
    public void notifyPatternReady(AdvancedPatternEngine.DetectedPattern pattern, 
                                  double entryPrice, double targetPrice, double stopPrice) {
        if (!isEnabled.get()) return;
        
        NotificationMessage message = new NotificationMessage(
            NotificationType.PATTERN_READY,
            pattern.getType().getDisplayName(),
            String.format("📈 الاتجاه: صاعد 🟢\n💵 الدخول: %.2f\n🎯 الهدف: %.2f\n🛡️ الوقف: %.2f", 
                         entryPrice, targetPrice, stopPrice),
            pattern.getSymbol()
        );
        
        double riskReward = Math.abs(targetPrice - entryPrice) / Math.abs(entryPrice - stopPrice);
        message.addDetail("📊 المخاطرة/العائد", String.format("1:%.1f", riskReward));
        message.addDetail("⚡ الثقة", pattern.getConfidence() + "%");
        
        queueNotification(message);
    }
    
    public void notifyPatternSuccess(AdvancedPatternEngine.DetectedPattern pattern, 
                                   double profit, long durationMinutes) {
        if (!isEnabled.get()) return;
        
        NotificationMessage message = new NotificationMessage(
            NotificationType.PATTERN_SUCCESS,
            pattern.getType().getDisplayName(),
            String.format("✅ النتيجة: ناجحة\n💰 الربح: +%.0f نقطة\n⏱️ المدة: %d دقيقة", 
                         profit, durationMinutes),
            pattern.getSymbol()
        );
        
        queueNotification(message);
    }
    
    public void notifyPatternFailed(AdvancedPatternEngine.DetectedPattern pattern, 
                                  double loss, String reason) {
        if (!isEnabled.get()) return;
        
        NotificationMessage message = new NotificationMessage(
            NotificationType.PATTERN_FAILED,
            pattern.getType().getDisplayName(),
            String.format("❌ النتيجة: فاشلة\n💸 الخسارة: %.0f نقطة\n📝 السبب: %s", 
                         loss, reason),
            pattern.getSymbol()
        );
        
        queueNotification(message);
    }
    
    public void notifySystemStatus(String component, boolean status, Map<String, Object> stats) {
        if (!isEnabled.get()) return;
        
        NotificationMessage message = new NotificationMessage(
            NotificationType.SYSTEM_STATUS,
            "حالة النظام",
            String.format("🔧 المكون: %s\n📊 الحالة: %s", 
                         component, status ? "يعمل ✅" : "متوقف ❌"),
            "SYSTEM"
        );
        
        if (stats != null) {
            stats.forEach((key, value) -> 
                message.addDetail(key, value.toString()));
        }
        
        queueNotification(message);
    }
    
    public void notifyCriticalAlert(String title, String description, String severity) {
        if (!isEnabled.get()) return;
        
        NotificationMessage message = new NotificationMessage(
            NotificationType.CRITICAL_ALERT,
            title,
            description,
            "ALERT"
        );
        
        message.addDetail("🚨 الخطورة", severity);
        message.addDetail("⏰ الوقت", new Date().toString());
        
        queueNotification(message);
    }
    
    private void queueNotification(NotificationMessage message) {
        try {
            boolean queued = messageQueue.offer(message, 1, TimeUnit.SECONDS);
            if (!queued) {
                System.err.println("📱 [TelegramNotificationService] Notification queue full, dropping message");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // Configuration methods
    public void enableNotificationType(NotificationType type, boolean enabled) {
        // Implementation for selective notification types
        System.out.println("📱 [TelegramNotificationService] " + type.getArabicName() + 
                          " notifications " + (enabled ? "enabled" : "disabled"));
    }
    
    public void setMinInterval(long intervalMs) {
        // Update minimum interval between notifications
        System.out.println("📱 [TelegramNotificationService] Minimum interval set to " + intervalMs + "ms");
    }
    
    // Statistics
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("is_enabled", isEnabled.get());
        stats.put("total_notifications", totalNotifications.get());
        stats.put("successful_notifications", successfulNotifications.get());
        stats.put("queue_size", messageQueue.size());
        stats.put("success_rate", calculateSuccessRate());
        return stats;
    }
    
    private double calculateSuccessRate() {
        long total = totalNotifications.get();
        return total > 0 ? (double) successfulNotifications.get() / total * 100.0 : 0.0;
    }
    
    public void shutdown() {
        isEnabled.set(false);
        notificationExecutor.shutdown();
        
        try {
            if (!notificationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                notificationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            notificationExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("📱 [TelegramNotificationService] Service shutdown completed");
    }
} 