package com.bookmaai.test;

// import com.bookmaai.core.AnalysisResult; // Not used
// import com.bookmaai.core.AnalysisResult.SignalDirection; // Not used
// import com.bookmaai.core.AnalysisResult.SignalStrength; // Not used
import com.bookmaai.services.ConflictAwareBookmapAI.SafeTradingAnalysis;
import com.bookmaai.services.ConflictAwareBookmapAI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// import java.time.Instant; // Not used
// import java.util.ArrayList; // Not used
// import java.util.List; // Not used

/**
 * 🧪 اختبار نظام إدارة التضارب
 * يعرض كيفية عمل النظام مع سيناريوهات مختلفة من التضارب
 * Moved to test package for better organization
 */
@Component
public class ConflictManagementTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ConflictManagementTest.class);
    
    @Autowired
    private ConflictAwareBookmapAI conflictAwareAI;
    
    /**
     * 🧪 اختبار شامل لنظام إدارة التضارب
     */
    public void runConflictManagementTests() {
        
        logger.info("🧪 Starting Conflict Management System Tests...");
        
        // 1. اختبار بدون تضارب
        testNoConflictScenario();
        
        // 2. اختبار النظام الآمن الشامل
        testSafeTradingAnalysis();
        
        // 3. اختبار التحليل الأسبوعي
        testWeeklyAnalysis();
        
        logger.info("✅ All conflict management tests completed!");
    }
    
    /**
     * ✅ اختبار عدم وجود تضارب
     */
    private void testNoConflictScenario() {
        logger.info("🟢 Testing NO CONFLICT scenario...");
        
        // استخدام النظام الموحد الجديد
        SafeTradingAnalysis analysis = conflictAwareAI.performComprehensiveAnalysis("NQ", "NY_Open");
        
        logger.info("📊 No Conflict Result:");
        logger.info("   - Safe to trade: {}", analysis.isSafeToTrade());
        logger.info("   - Risk level: {}", analysis.getRiskLevel());
        logger.info("   - Recommendation: {}", analysis.getSafeRecommendation());
        logger.info("   - Confidence: {:.1f}%", analysis.getConfidence() * 100);
    }
    
    /**
     * 📊 اختبار التحليل الأسبوعي
     */
    private void testWeeklyAnalysis() {
        logger.info("📊 Testing Weekly Frequency Analysis...");
        
        try {
            ConflictAwareBookmapAI.WeeklyFrequencyReport report = conflictAwareAI.generateWeeklyFrequencyAnalysis();
            
            logger.info("📅 Weekly Analysis Results:");
            logger.info("   - Total weekly opportunities: {}", report.totalWeeklyOpportunities);
            logger.info("   - Average success rate: {:.1f}%", report.averageSuccessRate * 100);
            logger.info("   - Best trading days: {}", report.bestTradingDays);
            
        } catch (Exception e) {
            logger.warn("⚠️ Weekly analysis failed: {}", e.getMessage());
        }
    }
    
    /**
     * 🛡️ اختبار النظام الآمن الشامل
     */
    private void testSafeTradingAnalysis() {
        logger.info("🛡️ Testing SAFE TRADING ANALYSIS...");
        
        // تشغيل تحليل آمن لـ NQ
        SafeTradingAnalysis safeAnalysis = conflictAwareAI.performComprehensiveAnalysis("NQ", "NY_Open");
        
        if (safeAnalysis != null) {
            logger.info("📊 Safe Trading Analysis Result:");
            logger.info("   - Asset: {}", safeAnalysis.getAsset());
            logger.info("   - Session: {}", safeAnalysis.getSession());
            logger.info("   - Safe recommendation: {}", safeAnalysis.getSafeRecommendation());
            logger.info("   - Risk level: {}", safeAnalysis.getRiskLevel());
            logger.info("   - Confidence: {:.1f}%", safeAnalysis.getConfidence() * 100);
            logger.info("   - Position size: {:.1f}", safeAnalysis.getPositionSizeMultiplier());
            logger.info("   - Safe to trade: {}", safeAnalysis.isSafeToTrade());
            logger.info("   - Safety message: {}", safeAnalysis.getSafetyMessage());
            
            if (safeAnalysis.getConflictWarning() != null) {
                logger.info("   ⚠️ Conflict warning: {}", safeAnalysis.getConflictWarning());
            }
        } else {
            logger.warn("⚠️ No analysis result returned");
        }
    }
    
    /**
     * 🧪 اختبار سيناريوهات متقدمة
     */
    public void runAdvancedConflictTests() {
        logger.info("🧪 Running ADVANCED conflict scenarios...");
        
        // سيناريو 1: اختبار التحليل الشامل
        testFullAnalysis();
        
        // سيناريو 2: اختبار الأنماط المتضاربة
        testConflictingPatterns();
    }
    
    private void testFullAnalysis() {
        logger.info("🎯 Testing full analysis run...");
        
        try {
            conflictAwareAI.runFullAnalysis();
            logger.info("✅ Full analysis completed successfully");
        } catch (Exception e) {
            logger.warn("⚠️ Full analysis failed: {}", e.getMessage());
        }
    }
    
    private void testConflictingPatterns() {
        logger.info("📊 Testing conflicting patterns analysis...");
        
        try {
            ConflictAwareBookmapAI.ConflictingPatternsReport report = conflictAwareAI.analyzeConflictingPatterns();
            
            logger.info("📊 Conflicting patterns result:");
            logger.info("   - Total patterns: {}", report.totalPatterns);
            logger.info("   - Total conflicts: {}", report.totalConflicts);
            logger.info("   - Conflict percentage: {:.1f}%", report.conflictPercentage);
            
        } catch (Exception e) {
            logger.warn("⚠️ Conflicting patterns analysis failed: {}", e.getMessage());
        }
    }
} 