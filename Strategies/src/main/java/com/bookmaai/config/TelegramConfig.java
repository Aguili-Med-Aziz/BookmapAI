package com.bookmaai.config;

/**
 * 📱 Telegram Configuration for BookmapAI
 * Configured for: Aziz +21696543589
 */
public class TelegramConfig {
    
    // User Configuration - Aziz
    public static final String USER_NAME = "Aziz";
    public static final String USER_PHONE = "+21696543589";
    public static final String USER_COUNTRY = "Tunisia"; // +216 is Tunisia country code
    
    // Telegram Bot Configuration
    // Note: You need to create a Telegram bot using @BotFather to get these values
    public static final String BOT_TOKEN = System.getProperty("telegram.bot.token", "YOUR_BOT_TOKEN_HERE");
    public static final String CHAT_ID = System.getProperty("telegram.chat.id", "YOUR_CHAT_ID_HERE");
    
    // Alternative: Direct phone number configuration for SMS-style notifications
    public static final String PHONE_NOTIFICATIONS_ENABLED = System.getProperty("phone.notifications.enabled", "true");
    
    // Notification Settings
    public static final boolean PATTERN_ALERTS = true;
    public static final boolean RISK_WARNINGS = true;
    public static final boolean SYSTEM_STATUS = true;
    public static final boolean TRADE_SIGNALS = true;
    public static final boolean PROFIT_LOSS_UPDATES = true;
    
    // Language preference
    public static final String LANGUAGE = "Arabic"; // Arabic + English mixed
    
    /**
     * Get the formatted user contact information
     */
    public static String getUserContact() {
        return String.format("%s (%s) - %s", USER_NAME, USER_PHONE, USER_COUNTRY);
    }
    
    /**
     * Get welcome message for the user
     */
    public static String getWelcomeMessage() {
        return String.format(
            "🚀 مرحباً %s!\n" +
            "📱 رقم الهاتف: %s\n" +
            "🇹🇳 الدولة: %s\n" +
            "✅ نظام BookmapAI جاهز لإرسال الإشعارات\n" +
            "\n" +
            "📊 سيتم إرسال:\n" +
            "• إشارات التداول الذكية\n" +
            "• تنبيهات إدارة المخاطر\n" +
            "• تحديثات الأرباح والخسائر\n" +
            "• حالة النظام\n" +
            "\n" +
            "🎯 نتمنى لك تداولاً موفقاً!",
            USER_NAME, USER_PHONE, USER_COUNTRY
        );
    }
    
    /**
     * Check if Telegram is properly configured
     */
    public static boolean isConfigured() {
        return !BOT_TOKEN.equals("YOUR_BOT_TOKEN_HERE") && 
               !CHAT_ID.equals("YOUR_CHAT_ID_HERE") &&
               !BOT_TOKEN.isEmpty() && 
               !CHAT_ID.isEmpty();
    }
    
    /**
     * Get configuration instructions for the user
     */
    public static String getSetupInstructions() {
        return "📱 إعداد تليجرام للمستخدم: " + USER_NAME + " (" + USER_PHONE + ")\n\n" +
               "1️⃣ افتح تليجرام وابحث عن @BotFather\n" +
               "2️⃣ أرسل /newbot لإنشاء بوت جديد\n" +
               "3️⃣ أعط البوت اسماً مثل: BookmapAI_" + USER_NAME + "_Bot\n" +
               "4️⃣ احفظ الـ TOKEN الذي سيرسله لك BotFather\n" +
               "5️⃣ ابدأ محادثة مع البوت الجديد\n" +
               "6️⃣ أرسل /start للبوت\n" +
               "7️⃣ احصل على CHAT_ID من @userinfobot\n" +
               "8️⃣ ضع القيم في متغيرات النظام:\n" +
               "   telegram.bot.token=YOUR_TOKEN\n" +
               "   telegram.chat.id=YOUR_CHAT_ID\n\n" +
               "📞 سيتم إرسال الإشعارات إلى: " + USER_PHONE;
    }
} 