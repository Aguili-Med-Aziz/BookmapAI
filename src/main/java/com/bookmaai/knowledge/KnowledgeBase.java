package com.bookmaai.knowledge;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * قاعدة المعرفة المتقدمة للنظام
 * تجمع بين القواعد الثابتة والديناميكية مع التعلم المستمر
 */
public class KnowledgeBase {
    
    // ═══════════════════════════════════════════
    // المكونات الأساسية لقاعدة المعرفة
    // ═══════════════════════════════════════════
    
    private final ConcurrentHashMap<String, Rule> staticRules;
    private final ConcurrentHashMap<String, DynamicRule> learnedRules;
    private final MarketContext currentContext;
    private final RuleEngine ruleEngine;
    private final KnowledgeValidator validator;
    
    // إحصائيات وتتبع الأداء
    private final PerformanceTracker performanceTracker;
    
    /**
     * تهيئة قاعدة المعرفة مع التحقق من صحة القواعد
     */
    public KnowledgeBase() {
        this.staticRules = new ConcurrentHashMap<>();
        this.learnedRules = new ConcurrentHashMap<>();
        this.currentContext = new MarketContext();
        this.ruleEngine = new RuleEngine();
        this.validator = new KnowledgeValidator();
        this.performanceTracker = new PerformanceTracker();
        
        loadInitialKnowledge();
        validateKnowledge();
    }
    
    // ═══════════════════════════════════════════
    // طرق اتخاذ القرار
    // ═══════════════════════════════════════════
    
    /**
     * اتخاذ قرار باستخدام النظام الهجين
     * @param pattern النمط المكتشف
     * @return قرار التداول
     */
    public Decision makeDecision(Pattern pattern) {
        // 1. تحليل السياق الحالي
        MarketContext context = currentContext.update();
        
        // 2. تطبيق القواعد الثابتة
        double staticScore = evaluateStaticRules(pattern, context);
        
        // 3. تطبيق القواعد الديناميكية
        double dynamicScore = evaluateDynamicRules(pattern, context);
        
        // 4. دمج النتائج مع الأوزان
        Decision decision = hybridDecisionEngine.decide(
            staticScore,
            dynamicScore,
            context
        );
        
        // 5. تتبع الأداء
        performanceTracker.trackDecision(decision);
        
        return decision;
    }
    
    // ═══════════════════════════════════════════
    // التعلم والتحديث
    // ═══════════════════════════════════════════
    
    /**
     * تحديث القواعد بناءً على نتائج التداول
     * @param result نتيجة التداول
     */
    public void updateKnowledge(TradeResult result) {
        // 1. تحديث إحصائيات الأداء
        performanceTracker.update(result);
        
        // 2. تحديث القواعد الديناميكية
        updateDynamicRules(result);
        
        // 3. اقتراح قواعد جديدة
        proposeNewRules(result);
        
        // 4. التحقق من صحة القواعد المحدثة
        validateKnowledge();
    }
    
    // ═══════════════════════════════════════════
    // التحقق من صحة المعرفة
    // ═══════════════════════════════════════════
    
    /**
     * التحقق من صحة وجودة قاعدة المعرفة
     */
    private void validateKnowledge() {
        // 1. التحقق من تناسق القواعد
        validator.checkConsistency(staticRules, learnedRules);
        
        // 2. التحقق من الأداء
        validator.checkPerformance(performanceTracker.getStats());
        
        // 3. التحقق من التغطية
        validator.checkCoverage(staticRules, learnedRules);
        
        // 4. تحديث مؤشرات الجودة
        updateQualityMetrics();
    }
    
    // ═══════════════════════════════════════════
    // مؤشرات الجودة
    // ═══════════════════════════════════════════
    
    /**
     * الحصول على مؤشرات جودة قاعدة المعرفة
     * @return تقرير الجودة
     */
    public QualityReport getQualityMetrics() {
        return QualityReport.builder()
            .ruleCount(staticRules.size() + learnedRules.size())
            .coverage(calculateCoverage())
            .consistency(calculateConsistency())
            .performance(performanceTracker.getOverallPerformance())
            .adaptability(calculateAdaptability())
            .build();
    }
    
    // ═══════════════════════════════════════════
    // الطرق المساعدة
    // ═══════════════════════════════════════════
    
    private void loadInitialKnowledge() {
        // تحميل القواعد الأولية من الملفات
        loadRulesFromJson();
        loadHistoricalPatterns();
        initializeBaseStrategies();
    }
    
    private double calculateCoverage() {
        // حساب نسبة تغطية القواعد للحالات المختلفة
        int totalCases = PatternRegistry.getTotalPatterns();
        int coveredCases = staticRules.size() + learnedRules.size();
        return (double) coveredCases / totalCases;
    }
    
    private double calculateConsistency() {
        // حساب مدى تناسق القواعد
        return validator.calculateConsistencyScore(
            staticRules,
            learnedRules
        );
    }
    
    private double calculateAdaptability() {
        // حساب مدى قدرة النظام على التكيف
        return performanceTracker.getAdaptabilityScore();
    }
} 