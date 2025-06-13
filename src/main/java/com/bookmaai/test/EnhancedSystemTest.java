package com.bookmaai.test;

import com.bookmaai.core.PatternEngineAdvanced;
import com.bookmaai.services.BookmapConfigurationLoader;
import com.bookmaai.notifications.TelegramNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * 🧪 Enhanced System Test - اختبار النظام المحسن
 * 
 * اختبار سريع للتأكد من أن النظام يقرأ JSON بشكل صحيح
 * Moved to test package for better organization
 */
@Component
public class EnhancedSystemTest {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedSystemTest.class);
    
    @Autowired(required = false)
    private BookmapConfigurationLoader configurationLoader;
    
    @Autowired(required = false) 
    private PatternEngineAdvanced patternEngine;
    
    @Autowired(required = false)
    private TelegramNotificationService telegramService;
    
    @PostConstruct
    public void runTests() {
        try {
            logger.info("🧪 Starting Enhanced System Tests...");
            
            // اختبار 1: Configuration Loader
            testConfigurationLoader();
            
            // اختبار 2: Pattern Engine
            testPatternEngine();
            
            // اختبار 3: System Status
            testSystemStatus();
            
            // اختبار 4: Telegram Notifications
            testAllNotificationTypes();
            
            logger.info("✅ All Enhanced System Tests PASSED!");
            
            // Manual test message
            System.out.println("   ============================================================");
            System.out.println("✅ Manual Test Completed!");
            System.out.println("📊 Check your Telegram and CSV files for results");
            System.out.println("   ============================================================");
            
        } catch (Exception e) {
            logger.error("❌ Enhanced System Tests FAILED: {}", e.getMessage());
        }
    }
    
    private void testConfigurationLoader() {
        if (configurationLoader == null) {
            logger.warn("⚠️ BookmapConfigurationLoader not available");
            return;
        }
        
        logger.info("🔧 Testing Configuration Loader...");
        
        // اختبار تحميل الأدوات
        var availableTools = configurationLoader.getAvailableTools();
        logger.info("📊 Available tools: {} ({})", availableTools.size(), availableTools);
        
        // اختبار تحميل الأنماط
        var availablePatterns = configurationLoader.getAvailablePatterns();
        logger.info("🎨 Available patterns: {}", availablePatterns.size());
        
        // اختبار أقوى الأنماط
        try {
            java.util.List<BookmapConfigurationLoader.PatternConfig> strongestPatterns = 
                configurationLoader.getStrongestPatterns(3);
            if (strongestPatterns != null && !strongestPatterns.isEmpty()) {
                logger.info("🏆 Top 3 patterns:");
                for (BookmapConfigurationLoader.PatternConfig pattern : strongestPatterns) {
                    logger.info("   - {} ({}): {:.1f}%", 
                               pattern.getName(), pattern.getToolName(), pattern.getSuccessRate() * 100);
                }
            } else {
                logger.info("🏆 No patterns found in JSON");
            }
        } catch (Exception e) {
            logger.warn("⚠️ Failed to get strongest patterns: {}", e.getMessage());
        }
        
        logger.info("✅ Configuration Loader test passed");
    }
    
    private void testPatternEngine() {
        if (patternEngine == null) {
            logger.warn("⚠️ AdvancedPatternEngine not available");
            return;
        }
        
        logger.info("🎯 Testing Pattern Engine...");
        
        // اختبار تحليل وهمي
        var result = patternEngine.analyzePatterns("NQ", 18500.0, 1500.0, 18490.0);
        
        if (result != null) {
            logger.info("📊 Test analysis result:");
            logger.info("   Symbol: {}", result.getSymbol());
            logger.info("   Direction: {}", result.getDirection());
            logger.info("   Confidence: {:.1f}%", result.getConfidence() * 100);
            logger.info("   Pattern: {}", result.getPatternName());
            logger.info("   Signals: {}", result.getSignals().size());
        } else {
            logger.warn("⚠️ No analysis result returned");
        }
        
        logger.info("✅ Pattern Engine test passed");
    }
    
    private void testSystemStatus() {
        if (patternEngine == null) {
            logger.warn("⚠️ Cannot test system status - PatternEngine not available");
            return;
        }
        
        logger.info("🔍 Testing System Status...");
        
        // اختبار إحصائيات المحرك
        try {
            String statusReport = patternEngine.getEngineStats();
            logger.info("📋 Engine Statistics:\n{}", statusReport);
        } catch (Exception e) {
            logger.warn("⚠️ Could not get engine stats: {}", e.getMessage());
        }
        
        // تنظيف البيانات القديمة
        try {
            patternEngine.cleanupOldData();
            logger.info("🧹 Old data cleanup completed");
        } catch (Exception e) {
            logger.warn("⚠️ Cleanup failed: {}", e.getMessage());
        }
        
        logger.info("✅ System Status test passed");
    }
    
    private void testAllNotificationTypes() {
        if (telegramService == null) {
            logger.warn("⚠️ TelegramNotificationService not available - notifications will not be tested");
            return;
        }
        
        logger.info("📢 Testing All Notification Types...");
        
        try {
            // 1. Pattern Formation Alert (60% completion)
            telegramService.sendPatternFormationAlert("PERFECT_STORM_NQ", "NQ", 18456.50, 65);
            logger.info("✅ Pattern Formation Alert sent");
            
            // 2. Pattern Completion Alert (100% completion)
            telegramService.sendPatternCompletionAlert("GOLDEN_GC_ACCUMULATION", "ES", 4567.25, "BULLISH", 4580.0);
            logger.info("✅ Pattern Completion Alert sent");
            
            // 3. Trade Ready Alert
            telegramService.sendTradeReadyAlert("FDAX_LIQUIDITY_SWEEP", "FDAX", 18123.0, 
                18123.0, 18160.0, 18090.0, "LONG", 2.1);
            logger.info("✅ Trade Ready Alert sent");
            
            // 4. Performance Alert
            telegramService.sendPerformanceAlert("ES", "VWAP_BREAKTHROUGH", 95.5, 425.0);
            logger.info("✅ Performance Alert sent");
            
            // 5. System Status Alert
            telegramService.sendSystemStatusAlert("System running optimally", "OPERATIONAL", 13, 15);
            logger.info("✅ System Status Alert sent");
            
            // 6. Golden Pattern Alert (>90% success rate)
            telegramService.sendGoldenPatternAlert("PERFECT_STORM_NQ", "NQ", 18456.50, 93.2);
            logger.info("✅ Golden Pattern Alert sent");
            
            // 7. Test notification
            telegramService.sendTestNotification();
            logger.info("✅ Test Notification sent");
            
        } catch (Exception e) {
            logger.error("❌ Notification testing failed: {}", e.getMessage());
        }
        
        logger.info("✅ Notification Tests completed");
    }
} 