package com.bookmaai.test;

import com.bookmaai.core.PatternEngineAdvanced;
import com.bookmaai.notifications.TelegramNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 🧪 اختبار النافذة المتحركة
 * يحاكي تطور نمط عبر النوافذ
 */
@Component
@Order(2) // يعمل بعد QuickTelegramTest
public class SlidingWindowTest implements CommandLineRunner {
    
    @Autowired
    private PatternEngineAdvanced patternEngine;
    
    @Autowired
    private TelegramNotificationService telegramService;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n🕐 ========== SLIDING WINDOW TEST ==========");
        System.out.println("📊 Simulating pattern evolution across windows...\n");
        
        try {
            // محاكاة تطور نمط PERFECT_STORM_NQ عبر 45 دقيقة
            simulatePatternEvolution();
            
        } catch (Exception e) {
            System.out.println("❌ Error in sliding window test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void simulatePatternEvolution() throws InterruptedException {
        String symbol = "NQ";
        double basePrice = 18450.0;
        Random random = new Random();
        
        System.out.println("🎯 Simulating PERFECT_STORM_NQ pattern evolution...\n");
        
        // Window 1: Initial Detection (0-15 min) - 30% progress
        System.out.println("⏰ Window 1 (0-15 min) - Initial Detection");
        for (int i = 0; i < 5; i++) {
            double price = basePrice + random.nextDouble() * 10;
            double volume = 1000 + random.nextDouble() * 500; // حجم منخفض
            double vwap = basePrice + 2;
            
            patternEngine.analyzePatterns(symbol, price, volume, vwap);
            Thread.sleep(100); // محاكاة تأخير
        }
        System.out.println("   ✅ Window 1 complete - Pattern should be at ~30%\n");
        
        // Window 2: Pattern Growth (15-30 min) - 60% progress
        System.out.println("⏰ Window 2 (15-30 min) - Pattern Growing");
        for (int i = 0; i < 10; i++) {
            double price = basePrice + 15 + random.nextDouble() * 10;
            double volume = 2000 + random.nextDouble() * 1000; // حجم متوسط
            double vwap = basePrice + 10;
            
            patternEngine.analyzePatterns(symbol, price, volume, vwap);
            Thread.sleep(100);
        }
        System.out.println("   ✅ Window 2 complete - Pattern should be at ~60%\n");
        
        // Window 3: Pattern Maturity (30-45 min) - 100% progress
        System.out.println("⏰ Window 3 (30-45 min) - Pattern Completion");
        for (int i = 0; i < 15; i++) {
            double price = basePrice + 30 + random.nextDouble() * 15;
            double volume = 3000 + random.nextDouble() * 2000; // حجم عالي
            double vwap = basePrice + 20;
            
            patternEngine.analyzePatterns(symbol, price, volume, vwap);
            Thread.sleep(100);
        }
        System.out.println("   ✅ Window 3 complete - Pattern should be at 100%!\n");
        
        System.out.println("🎉 Pattern evolution simulation complete!");
        System.out.println("📱 Check Telegram for progress notifications (30%, 60%, 100%)");
    }
} 