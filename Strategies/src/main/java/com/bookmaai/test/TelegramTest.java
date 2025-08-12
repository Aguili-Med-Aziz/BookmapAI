package com.bookmaai.test;

import com.bookmaai.core.TelegramNotificationService;
import com.bookmaai.core.AdvancedPatternEngine;
import com.bookmaai.config.TelegramConfig;

/**
 * 📱 Telegram Test for Aziz (+21696543589)
 * 
 * This test demonstrates the Telegram notification system
 * configured specifically for your phone number.
 */
public class TelegramTest {
    
    public static void main(String[] args) {
        System.out.println("📱 ========================================");
        System.out.println("📱 Telegram Notification Test");
        System.out.println("📱 User: " + TelegramConfig.getUserContact());
        System.out.println("📱 ========================================");
        
        // Show configuration status
        testConfiguration();
        
        // Test notification service
        testNotificationService();
        
        // Simulate trading notifications
        simulateTradingNotifications();
        
        System.out.println("📱 ========================================");
        System.out.println("📱 Test completed!");
        System.out.println("📱 ========================================");
    }
    
    private static void testConfiguration() {
        System.out.println("\n🔧 Testing Configuration:");
        System.out.println("   Name: " + TelegramConfig.USER_NAME);
        System.out.println("   Phone: " + TelegramConfig.USER_PHONE);
        System.out.println("   Country: " + TelegramConfig.USER_COUNTRY);
        System.out.println("   Configured: " + (TelegramConfig.isConfigured() ? "✅ Yes" : "❌ No"));
        
        if (!TelegramConfig.isConfigured()) {
            System.out.println("\n📋 Setup Instructions:");
            System.out.println(TelegramConfig.getSetupInstructions());
        }
    }
    
    private static void testNotificationService() {
        System.out.println("\n📱 Testing Notification Service:");
        
        // Create notification service
        TelegramNotificationService telegramService = new TelegramNotificationService(
            TelegramConfig.BOT_TOKEN, 
            TelegramConfig.CHAT_ID
        );
        
        // Initialize service
        telegramService.initialize();
        
        // Test welcome message
        System.out.println("\n🎉 Welcome Message:");
        System.out.println(TelegramConfig.getWelcomeMessage());
        
        // Shutdown service
        telegramService.shutdown();
    }
    
    private static void simulateTradingNotifications() {
        System.out.println("\n🎯 Simulating Trading Notifications:");
        
        TelegramNotificationService telegramService = new TelegramNotificationService(
            TelegramConfig.BOT_TOKEN, 
            TelegramConfig.CHAT_ID
        );
        telegramService.initialize();
        
        try {
            // Simulate pattern detection
            System.out.println("\n1️⃣ Pattern Detection Alert:");
            simulatePatternAlert(telegramService);
            
            Thread.sleep(1000);
            
            // Simulate trading opportunity
            System.out.println("\n2️⃣ Trading Opportunity Alert:");
            simulateTradeAlert(telegramService);
            
            Thread.sleep(1000);
            
            // Simulate profit notification
            System.out.println("\n3️⃣ Profit Notification:");
            simulateProfitAlert(telegramService);
            
            Thread.sleep(1000);
            
            // Simulate system status
            System.out.println("\n4️⃣ System Status Update:");
            simulateSystemAlert(telegramService);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            telegramService.shutdown();
        }
    }
    
    private static void simulatePatternAlert(TelegramNotificationService service) {
        // Create a mock pattern (simplified for demo)
        System.out.println("📊 Simulated Pattern Detection:");
        
        String patternMessage = String.format(
            "👋 مرحباً %s!\n" +
            "🌱 نمط جديد يتكون\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "📊 النمط: Perfect Storm\n" +
            "📈 الرمز: EURUSD\n" +
            "⏱️ التقدم: 75%%\n" +
            "🎯 المرحلة: تكوين النمط\n" +
            "🛠️ الأدوات النشطة: 8/12\n" +
            "📊 الثقة: 85%%\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "📱 BookmapAI System",
            TelegramConfig.USER_NAME
        );
        
        System.out.println("📱 [TelegramNotificationService] Sending Telegram message to " + TelegramConfig.USER_PHONE + ":");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println(patternMessage);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    private static void simulateTradeAlert(TelegramNotificationService service) {
        String tradeMessage = String.format(
            "👋 مرحباً %s!\n" +
            "💰 فرصة تداول جاهزة\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "📊 النمط: Perfect Storm\n" +
            "📈 الرمز: EURUSD\n" +
            "📈 الاتجاه: صاعد 🟢\n" +
            "💵 الدخول: 1.0850\n" +
            "🎯 الهدف: 1.0900\n" +
            "🛡️ الوقف: 1.0830\n" +
            "📊 المخاطرة/العائد: 1:2.5\n" +
            "⚡ الثقة: 92%%\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "📱 BookmapAI System",
            TelegramConfig.USER_NAME
        );
        
        System.out.println("📱 [TelegramNotificationService] Sending Telegram message to " + TelegramConfig.USER_PHONE + ":");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println(tradeMessage);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    private static void simulateProfitAlert(TelegramNotificationService service) {
        String profitMessage = String.format(
            "👋 مرحباً %s!\n" +
            "🏆 نجح النمط\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "📊 النمط: Perfect Storm\n" +
            "📈 الرمز: EURUSD\n" +
            "✅ النتيجة: ناجحة\n" +
            "💰 الربح: +50 نقطة\n" +
            "⏱️ المدة: 45 دقيقة\n" +
            "🎯 العائد: 2.5:1\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "📱 BookmapAI System",
            TelegramConfig.USER_NAME
        );
        
        System.out.println("📱 [TelegramNotificationService] Sending Telegram message to " + TelegramConfig.USER_PHONE + ":");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println(profitMessage);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    private static void simulateSystemAlert(TelegramNotificationService service) {
        String systemMessage = String.format(
            "👋 مرحباً %s!\n" +
            "⚙️ حالة النظام\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "📊 النمط: System Status\n" +
            "📈 الرمز: SYSTEM\n" +
            "🔧 المكون: BookmapAI Core\n" +
            "📊 الحالة: يعمل ✅\n" +
            "📈 الأنماط المكتشفة: 127\n" +
            "💰 الصفقات الناجحة: 89%%\n" +
            "🎯 دقة النظام: 94.2%%\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "📱 BookmapAI System",
            TelegramConfig.USER_NAME
        );
        
        System.out.println("📱 [TelegramNotificationService] Sending Telegram message to " + TelegramConfig.USER_PHONE + ":");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println(systemMessage);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
} 