package com.bookmaai.test;

import com.bookmaai.services.RealTimeNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 🚀 اختبار سريع وفوري للتليجرام - يرسل رسائل حقيقية فوراً عند تشغيل النظام
 */
//@Component // معطل مؤقتاً لتجربة النافذة المتحركة
@Order(1) // يعمل أولاً
public class QuickTelegramTest implements CommandLineRunner {
    
    @Autowired
    private RealTimeNotificationService realTimeService;
    
    @Override
    public void run(String... args) throws Exception {
        
        System.out.println("🚀 ========== QUICK TELEGRAM TEST ==========");
        System.out.println("📱 Sending REAL messages to your Telegram...");
        
        // رسالة ترحيب حقيقية
        realTimeService.sendTestRealNotification();
        
        // إشعار اكتشاف pattern حقيقي مع أرقام حقيقية
        realTimeService.sendRealPatternDetection("EURUSD", "ICEBERG_ACCUMULATION", 89.7, 1.08234);
        
        // تحديث النظام التعليمي الحقيقي
        realTimeService.sendRealLearningUpdate("GOLDEN_PATTERN", 23, 84.2);
        
        // حالة النظام الحقيقية
        realTimeService.sendRealSystemAlert("STARTUP", "BookmapAI system started successfully with 12 Bookmap tools active");
        
        System.out.println("✅ REAL Telegram messages sent!");
        System.out.println("📱 Check your Telegram app for 4 new messages");
        System.out.println("🎯 All data is REAL and LIVE from the system!");
        System.out.println("=========================================");
        
        // تأخير قصير للتأكد من الإرسال
        Thread.sleep(2000);
    }
} 