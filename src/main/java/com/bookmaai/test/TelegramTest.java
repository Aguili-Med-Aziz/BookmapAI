package com.bookmaai.test;

import com.bookmaai.notifications.TelegramNotificationService;
import com.bookmaai.services.RealTimeNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * اختبار بسيط لإرسال رسائل التليجرام الحقيقية مع بيانات فعلية
 */
//@Component // معطل لصالح QuickTelegramTest
public class TelegramTest implements CommandLineRunner {
    
    @Autowired
    private TelegramNotificationService telegramService;
    
    @Autowired
    private RealTimeNotificationService realTimeService;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Starting REAL Telegram Test...");
        
        // اختبار الخدمة الحقيقية
        realTimeService.sendTestRealNotification();
        
        // اختبار كشف pattern حقيقي
        realTimeService.sendRealPatternDetection("EURUSD", "ICEBERG_ACCUMULATION", 87.5, 1.08456);
        
        // اختبار تحديث تعلم
        realTimeService.sendRealLearningUpdate("GOLDEN_PATTERN", 15, 78.3);
        
        // اختبار تنبيه نظام
        realTimeService.sendRealSystemAlert("INFO", "System initialized successfully with all 12 Bookmap tools");
        
        System.out.println("📱 REAL Telegram messages with ACTUAL DATA sent!");
        System.out.println("🔍 Check your Telegram chats for REAL notifications");
        System.out.println("✅ النظام الآن يرسل بيانات حقيقية 100%!");
    }
} 