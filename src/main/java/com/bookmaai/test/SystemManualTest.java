package com.bookmaai.test;

import com.bookmaai.services.*;
import com.bookmaai.core.PatternEngineAdvanced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * 🧪 Manual System Test - اختبار يدوي للنظام
 * 
 * لاختبار:
 * ✅ Telegram Notifications  
 * ✅ CSV Pattern Logging
 * ✅ Risk/Reward Calculations
 */
@Component  
public class SystemManualTest implements ApplicationRunner {
    
    @Autowired(required = false)
    private PatternEngineAdvanced advancedEngine;
    
    @Autowired(required = false) 
    private PatternLearningLogger learningLogger;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // إعطاء النظام وقت للبدء
        Thread.sleep(5000);
        
        System.out.println("\n🧪 ==================== SYSTEM MANUAL TEST ====================");
        System.out.println("🎯 Testing: Telegram Notifications + CSV Logging + Risk/Reward");
        System.out.println("============================================================\n");
        
        if (advancedEngine != null) {
            runPatternDetectionTests();
        } else {
            System.out.println("❌ PatternEngineAdvanced not available for testing");
        }
        
        System.out.println("\n✅ Manual Test Completed!");
        System.out.println("📋 Check your Telegram and CSV files for results");
    }
    
    private void runPatternDetectionTests() {
        System.out.println("🔥 Running Pattern Detection Tests...\n");
        
        // Test Case 1: High Confidence EURUSD
        testPattern("EURUSD", 1.0850, 1200.0, 1.0848, "High volume EURUSD test");
        
        // Test Case 2: Strong GBPUSD Signal  
        testPattern("GBPUSD", 1.2750, 1500.0, 1.2745, "Strong GBPUSD signal test");
        
        // Test Case 3: Perfect Storm USDJPY
        testPattern("USDJPY", 149.85, 2000.0, 149.80, "Perfect storm USDJPY test");
        
        // Test Case 4: Medium Confidence USDCHF
        testPattern("USDCHF", 0.8920, 800.0, 0.8918, "Medium confidence USDCHF test");
        
        // Test Case 5: Triple Confirmation AUDUSD
        testPattern("AUDUSD", 0.6750, 1800.0, 0.6748, "Triple confirmation AUDUSD test");
    }
    
    private void testPattern(String symbol, double price, double volume, double vwap, String description) {
        try {
            System.out.println(String.format("🎯 Testing: %s | Price: %.5f | Volume: %.0f", symbol, price, volume));
            System.out.println("   📝 Description: " + description);
            
            // تشغيل تحليل الأنماط
            var result = advancedEngine.analyzePatterns(symbol, price, volume, vwap);
            
            if (result != null) {
                System.out.println(String.format("   📊 Result: %s | %s | Confidence: %.1f%%", 
                    result.getDirection(), result.getPatternName(), result.getConfidence() * 100));
                
                if (result.getConfidence() >= 0.70) {
                    System.out.println("   🚨 HIGH CONFIDENCE PATTERN - Should trigger Telegram!");
                    System.out.println("   📱 Expected: Telegram notification sent");
                    System.out.println("   📊 Expected: CSV entry logged");
                    
                    // محاكاة تحديث النتيجة بعد فترة
                    if (learningLogger != null) {
                        System.out.println("   💾 Manually logging to CSV for verification...");
                        
                        // إنشاء بيانات تجريبية للـ CSV logging
                        Map<String, Double> toolResults = new HashMap<>();
                        toolResults.put("cvd", 0.85);
                        toolResults.put("heatmap", 0.78);
                        toolResults.put("volume_dots", 0.82);
                        toolResults.put("vwap", 0.75);
                        toolResults.put("volume_profile", 0.88);
                        toolResults.put("iceberg_detector", 0.72);
                        
                        PatternLearningLogger.PatternDetectionData testData = 
                            new PatternLearningLogger.PatternDetectionData(
                                symbol, result.getDirection(), result.getPatternName(),
                                result.getConfidence(), toolResults, price, 
                                price - 0.0020, price + 0.0040, 2.0, price - 0.0015, 
                                price + 0.0035, price - 0.0018, price + 0.0038, "MEDIUM",
                                volume, "NORMAL", getCurrentTimeOfDay(), 0.75, 
                                "TEST_SEQUENCE", "MEDIUM", 40.0
                            );
                        
                        String patternId = learningLogger.logPatternDetection(testData);
                        System.out.println("   ✅ Pattern logged with ID: " + patternId);
                    }
                } else {
                    System.out.println("   ⚠️ Low confidence - No Telegram notification expected");
                }
                
            } else {
                System.out.println("   ❌ No analysis result returned");
            }
            
        } catch (Exception e) {
            System.out.println("   ❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            Thread.sleep(3000); // انتظار 3 ثواني بين الاختبارات
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("   " + "=".repeat(60) + "\n");
    }
    
    private String getCurrentTimeOfDay() {
        int hour = LocalDateTime.now().getHour();
        if (hour >= 8 && hour <= 10) return "LONDON_OPEN";
        if (hour >= 13 && hour <= 15) return "NY_OPEN";
        if (hour >= 21 && hour <= 23) return "SYDNEY_OPEN";
        return "NORMAL_HOURS";
    }
} 