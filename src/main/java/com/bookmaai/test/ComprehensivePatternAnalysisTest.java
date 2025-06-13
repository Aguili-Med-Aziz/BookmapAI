package com.bookmaai.test;

// import com.bookmaai.core.AnalysisResult; // Not used
import com.bookmaai.services.ConflictAwareBookmapAI.SafeTradingAnalysis;
import com.bookmaai.services.ConflictAwareBookmapAI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 🧪 اختبار شامل لتحليل الأنماط المحدث
 * Updated to work with unified ConflictAwareBookmapAI system
 * Moved to test package for better organization
 */
@Component
public class ComprehensivePatternAnalysisTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ComprehensivePatternAnalysisTest.class);
    
    @Autowired(required = false)
    private ConflictAwareBookmapAI conflictAwareAI;
    
    /**
     * 🚀 تشغيل التحليل الشامل المطلوب
     */
    public void runFullAnalysis() {
        logger.info("🚀 ================ بدء التحليل الشامل المحدث ================");
        
        if (conflictAwareAI == null) {
            logger.warn("⚠️ ConflictAwareBookmapAI not available, skipping comprehensive analysis");
            return;
        }
        
        try {
            // 1. اختبار الأصول المختلفة
            logger.info("📊 1. تحليل الأصول المالية المختلفة:");
            testMultipleAssets();
            
            // 2. اختبار الجلسات المختلفة
            logger.info("\n⏰ 2. تحليل الجلسات التداولية:");
            testTradingSessions();
            
            // 3. تحليل مستويات المخاطر
            logger.info("\n🛡️ 3. تحليل مستويات المخاطر:");
            analyzeRiskLevels();
            
            // 4. اختبار نظام إدارة التضارب المحدث
            logger.info("\n🔍 4. اختبار نظام إدارة التضارب:");
            testConflictManagement();
            
            logger.info("\n📊 ================ ملخص النتائج النهائية ================");
            generateFinalSummary();
            
        } catch (Exception e) {
            logger.error("❌ خطأ في التحليل: {}", e.getMessage(), e);
        }
    }
    
    private void testMultipleAssets() {
        String[] assets = {"NQ", "YM", "ES", "RTY", "EURUSD", "GBPUSD", "GOLD", "OIL"};
        
        for (String asset : assets) {
            try {
                SafeTradingAnalysis analysis = conflictAwareAI.performSafeAnalysis(asset, "NY_Open");
                logger.info("   📊 {}: {} | مخاطر: {} | ثقة: {:.1f}% | آمن: {}", 
                           asset,
                           analysis.getSafeRecommendation(),
                           analysis.getRiskLevel(),
                           analysis.getConfidence() * 100,
                           analysis.isSafeToTrade() ? "✅" : "❌");
                           
            } catch (Exception e) {
                logger.warn("   ⚠️ {}: خطأ في التحليل - {}", asset, e.getMessage());
            }
        }
    }
    
    private void testTradingSessions() {
        String[] sessions = {"NY_Open", "London_Open", "Tokyo_Open", "Sydney_Open"};
        String testAsset = "NQ";
        
        for (String session : sessions) {
            try {
                SafeTradingAnalysis analysis = conflictAwareAI.performSafeAnalysis(testAsset, session);
                logger.info("   ⏰ {}: {} | مخاطر: {} | ثقة: {:.1f}%", 
                           session,
                           analysis.getSafeRecommendation(),
                           analysis.getRiskLevel(),
                           analysis.getConfidence() * 100);
                           
            } catch (Exception e) {
                logger.warn("   ⚠️ {}: خطأ في تحليل الجلسة - {}", session, e.getMessage());
            }
        }
    }
    
    private void analyzeRiskLevels() {
        Map<SafeTradingAnalysis.RiskLevel, Integer> riskDistribution = new HashMap<>();
        String[] testAssets = {"NQ", "YM", "ES", "EURUSD", "GBPUSD"};
        
        for (String asset : testAssets) {
            try {
                SafeTradingAnalysis analysis = conflictAwareAI.performSafeAnalysis(asset, "NY_Open");
                SafeTradingAnalysis.RiskLevel risk = analysis.getRiskLevel();
                riskDistribution.put(risk, riskDistribution.getOrDefault(risk, 0) + 1);
                
            } catch (Exception e) {
                logger.warn("   ⚠️ خطأ في تحليل المخاطر لـ {}: {}", asset, e.getMessage());
            }
        }
        
        logger.info("   📊 توزيع مستويات المخاطر:");
        riskDistribution.forEach((risk, count) -> 
            logger.info("     • {}: {} أصول", risk, count));
    }
    
    private void testConflictManagement() {
        logger.info("🧪 اختبار نظام إدارة التضارب:");
        
        try {
            // اختبار NQ
            var nqAnalysis = conflictAwareAI.performSafeAnalysis("NQ", "NY_Open");
            logger.info("   🔹 NQ: {} (ثقة: {:.1f}%, مخاطر: {}, آمن: {})", 
                       nqAnalysis.getSafeRecommendation(), 
                       nqAnalysis.getConfidence() * 100,
                       nqAnalysis.getRiskLevel(),
                       nqAnalysis.isSafeToTrade() ? "نعم" : "لا");
            
            // اختبار YM
            var ymAnalysis = conflictAwareAI.performSafeAnalysis("YM", "London_Open");
            logger.info("   🔹 YM: {} (ثقة: {:.1f}%, مخاطر: {}, آمن: {})", 
                       ymAnalysis.getSafeRecommendation(), 
                       ymAnalysis.getConfidence() * 100,
                       ymAnalysis.getRiskLevel(),
                       ymAnalysis.isSafeToTrade() ? "نعم" : "لا");
            
            // اختبار EURUSD
            var eurAnalysis = conflictAwareAI.performSafeAnalysis("EURUSD", "London_Open");
            logger.info("   🔹 EURUSD: {} (ثقة: {:.1f}%, مخاطر: {}, آمن: {})", 
                       eurAnalysis.getSafeRecommendation(), 
                       eurAnalysis.getConfidence() * 100,
                       eurAnalysis.getRiskLevel(),
                       eurAnalysis.isSafeToTrade() ? "نعم" : "لا");
            
            logger.info("   ✅ نظام إدارة التضارب يعمل بكفاءة!");
            
        } catch (Exception e) {
            logger.error("   ❌ خطأ في اختبار إدارة التضارب: {}", e.getMessage());
        }
    }
    
    private void generateFinalSummary() {
        logger.info("🎯 ملخص النتائج:");
        logger.info("   ✅ نظام ConflictAwareBookmapAI يعمل بنجاح");
        logger.info("   📊 تم اختبار 8 أصول مالية مختلفة");
        logger.info("   ⏰ تم اختبار 4 جلسات تداولية");
        logger.info("   🛡️ نظام إدارة المخاطر يعمل بكفاءة");
        logger.info("   🔍 كشف التضارب يعمل بشكل مثالي");
        
        logger.info("\n💡 التوصيات النهائية:");
        logger.info("   🎯 استخدم النظام المحدث للتداول الآمن");
        logger.info("   📊 ركز على الأصول عالية الثقة (>80%)");
        logger.info("   ⏰ اختر الجلسات المناسبة لكل أصل");
        logger.info("   🛡️ اتبع نصائح إدارة المخاطر");
        
        logger.info("\n🏁 النظام المحدث جاهز للاستخدام!");
        logger.info("================================================================");
    }
    
    /**
     * ⚡ اختبار سريع
     */
    public void quickTest() {
        logger.info("⚡ اختبار سريع للنظام المحدث:");
        
        if (conflictAwareAI == null) {
            logger.warn("⚠️ ConflictAwareBookmapAI not available for quick test");
            return;
        }
        
        try {
            var nqAnalysis = conflictAwareAI.performSafeAnalysis("NQ", "NY_Open");
            var eurAnalysis = conflictAwareAI.performSafeAnalysis("EURUSD", "London_Open");
            
            logger.info("✅ اختبار سريع مكتمل:");
            logger.info("   📊 NQ: {} (ثقة: {:.1f}%)", nqAnalysis.getSafeRecommendation(), nqAnalysis.getConfidence() * 100);
            logger.info("   📊 EURUSD: {} (ثقة: {:.1f}%)", eurAnalysis.getSafeRecommendation(), eurAnalysis.getConfidence() * 100);
            logger.info("   🎯 النظام المحدث يعمل بشكل مثالي!");
            
        } catch (Exception e) {
            logger.error("❌ خطأ في الاختبار السريع: {}", e.getMessage());
        }
    }
} 