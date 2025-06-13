package com.bookmaai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.bookmaai.notifications.TelegramNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
public class TelegramTestDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(TelegramTestDemo.class);
    
    @Autowired
    private TelegramNotificationService telegramService;
    
    @Test
    public void testTelegramConfiguration() {
        logger.info("🚀 ==================== TELEGRAM TEST ====================");
        logger.info("📱 Testing Telegram configuration and notifications...");
        
        // Test 1: Test Notification
        logger.info("📤 Sending test notification...");
        telegramService.sendTestNotification();
        
        // Test 2: Trading Signal Test
        logger.info("📤 Sending trading signal test...");
        telegramService.sendTradingSignal("EURUSD", "BUY", 1.0850, 1.0830, 1.0880, 0.85, "BREAKOUT_PATTERN", "Strong signals detected");
        
        // Test 3: Pattern Alert Test
        logger.info("📤 Sending pattern formation test...");
        telegramService.sendPatternFormationAlert("GOLDEN_CROSS_FORMATION", "GBPUSD", 1.2650, 75);
        
        // Test 4: System Status Test  
        logger.info("📤 Sending system status test...");
        telegramService.sendSystemStatusAlert("System fully operational with 4 chat IDs configured", "OPERATIONAL", 12, 15);
        
        logger.info("✅ All Telegram tests completed!");
        logger.info("🎯 Check your Telegram chats for messages");
        logger.info("============================================================");
    }
} 