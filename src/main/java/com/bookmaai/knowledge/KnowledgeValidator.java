package com.bookmaai.knowledge;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * نظام التحقق من جودة قاعدة المعرفة
 * يقوم بفحص وتقييم جودة القواعد والأداء
 */
public class KnowledgeValidator {

    // ═══════════════════════════════════════════
    // ثوابت التحقق
    // ═══════════════════════════════════════════
    
    private static final double MIN_CONSISTENCY_SCORE = 0.85;
    private static final double MIN_COVERAGE_SCORE = 0.80;
    private static final double MIN_PERFORMANCE_SCORE = 0.75;
    
    // ═══════════════════════════════════════════
    // التحقق من التناسق
    // ═══════════════════════════════════════════
    
    /**
     * التحقق من تناسق القواعد
     * @param staticRules القواعد الثابتة
     * @param dynamicRules القواعد الديناميكية
     * @throws ValidationException في حالة وجود تعارض
     */
    public void checkConsistency(
        Map<String, Rule> staticRules,
        Map<String, DynamicRule> dynamicRules
    ) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // 1. التحقق من التعارض بين القواعد الثابتة
        checkStaticRulesConflicts(staticRules, issues);
        
        // 2. التحقق من التعارض بين القواعد الديناميكية
        checkDynamicRulesConflicts(dynamicRules, issues);
        
        // 3. التحقق من التعارض بين القواعد الثابتة والديناميكية
        checkCrossRulesConflicts(staticRules, dynamicRules, issues);
        
        // إذا وجدت مشاكل، نرفع استثناء
        if (!issues.isEmpty()) {
            throw new ValidationException("تعارض في القواعد", issues);
        }
    }
    
    /**
     * حساب درجة تناسق القواعد
     * @return درجة التناسق من 0 إلى 1
     */
    public double calculateConsistencyScore(
        Map<String, Rule> staticRules,
        Map<String, DynamicRule> dynamicRules
    ) {
        int totalRules = staticRules.size() + dynamicRules.size();
        int conflictCount = countConflicts(staticRules, dynamicRules);
        
        return 1.0 - ((double) conflictCount / totalRules);
    }
    
    // ═══════════════════════════════════════════
    // التحقق من الأداء
    // ═══════════════════════════════════════════
    
    /**
     * التحقق من أداء القواعد
     * @param stats إحصائيات الأداء
     * @throws ValidationException في حالة ضعف الأداء
     */
    public void checkPerformance(PerformanceStats stats) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // 1. التحقق من معدل النجاح
        if (stats.getSuccessRate() < MIN_PERFORMANCE_SCORE) {
            issues.add(new ValidationIssue(
                "معدل النجاح منخفض",
                String.format("%.2f%%", stats.getSuccessRate() * 100)
            ));
        }
        
        // 2. التحقق من نسبة الربح/الخسارة
        if (stats.getProfitFactor() < 1.5) {
            issues.add(new ValidationIssue(
                "نسبة الربح/الخسارة منخفضة",
                String.format("%.2f", stats.getProfitFactor())
            ));
        }
        
        // 3. التحقق من الاتساق في الأداء
        if (stats.getConsistencyScore() < MIN_CONSISTENCY_SCORE) {
            issues.add(new ValidationIssue(
                "عدم اتساق في الأداء",
                String.format("%.2f%%", stats.getConsistencyScore() * 100)
            ));
        }
        
        if (!issues.isEmpty()) {
            throw new ValidationException("مشاكل في الأداء", issues);
        }
    }
    
    // ═══════════════════════════════════════════
    // التحقق من التغطية
    // ═══════════════════════════════════════════
    
    /**
     * التحقق من تغطية القواعد
     * @param staticRules القواعد الثابتة
     * @param dynamicRules القواعد الديناميكية
     * @throws ValidationException في حالة نقص التغطية
     */
    public void checkCoverage(
        Map<String, Rule> staticRules,
        Map<String, DynamicRule> dynamicRules
    ) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // 1. التحقق من تغطية الأنماط
        checkPatternCoverage(staticRules, dynamicRules, issues);
        
        // 2. التحقق من تغطية حالات السوق
        checkMarketConditionsCoverage(staticRules, dynamicRules, issues);
        
        // 3. التحقق من تغطية الأدوات
        checkToolsCoverage(staticRules, dynamicRules, issues);
        
        if (!issues.isEmpty()) {
            throw new ValidationException("نقص في التغطية", issues);
        }
    }
    
    // ═══════════════════════════════════════════
    // الطرق المساعدة
    // ═══════════════════════════════════════════
    
    private void checkStaticRulesConflicts(
        Map<String, Rule> rules,
        List<ValidationIssue> issues
    ) {
        // التحقق من التعارض بين القواعد الثابتة
        for (Map.Entry<String, Rule> entry1 : rules.entrySet()) {
            for (Map.Entry<String, Rule> entry2 : rules.entrySet()) {
                if (!entry1.getKey().equals(entry2.getKey()) &&
                    areRulesConflicting(entry1.getValue(), entry2.getValue())) {
                    issues.add(new ValidationIssue(
                        "تعارض بين القواعد الثابتة",
                        String.format("%s vs %s", entry1.getKey(), entry2.getKey())
                    ));
                }
            }
        }
    }
    
    private void checkDynamicRulesConflicts(
        Map<String, DynamicRule> rules,
        List<ValidationIssue> issues
    ) {
        // التحقق من التعارض بين القواعد الديناميكية
        for (Map.Entry<String, DynamicRule> entry1 : rules.entrySet()) {
            for (Map.Entry<String, DynamicRule> entry2 : rules.entrySet()) {
                if (!entry1.getKey().equals(entry2.getKey()) &&
                    areDynamicRulesConflicting(entry1.getValue(), entry2.getValue())) {
                    issues.add(new ValidationIssue(
                        "تعارض بين القواعد الديناميكية",
                        String.format("%s vs %s", entry1.getKey(), entry2.getKey())
                    ));
                }
            }
        }
    }
    
    private void checkCrossRulesConflicts(
        Map<String, Rule> staticRules,
        Map<String, DynamicRule> dynamicRules,
        List<ValidationIssue> issues
    ) {
        // التحقق من التعارض بين القواعد الثابتة والديناميكية
        for (Map.Entry<String, Rule> staticEntry : staticRules.entrySet()) {
            for (Map.Entry<String, DynamicRule> dynamicEntry : dynamicRules.entrySet()) {
                if (areRulesConflicting(staticEntry.getValue(), dynamicEntry.getValue())) {
                    issues.add(new ValidationIssue(
                        "تعارض بين قاعدة ثابتة وديناميكية",
                        String.format("%s vs %s", staticEntry.getKey(), dynamicEntry.getKey())
                    ));
                }
            }
        }
    }
    
    private boolean areRulesConflicting(Rule rule1, Rule rule2) {
        // التحقق من وجود تعارض بين قاعدتين
        return rule1.getConditions().stream()
            .anyMatch(condition ->
                rule2.getConditions().stream()
                    .anyMatch(condition2 ->
                        condition.conflicts(condition2)
                    )
            );
    }
    
    private boolean areDynamicRulesConflicting(DynamicRule rule1, DynamicRule rule2) {
        // التحقق من وجود تعارض بين قاعدتين ديناميكيتين
        return rule1.getConditions().stream()
            .anyMatch(condition ->
                rule2.getConditions().stream()
                    .anyMatch(condition2 ->
                        condition.conflicts(condition2) &&
                        Math.abs(rule1.getConfidence() - rule2.getConfidence()) < 0.1
                    )
            );
    }
    
    private int countConflicts(
        Map<String, Rule> staticRules,
        Map<String, DynamicRule> dynamicRules
    ) {
        int conflicts = 0;
        
        // عد التعارضات بين القواعد الثابتة
        for (Rule rule1 : staticRules.values()) {
            for (Rule rule2 : staticRules.values()) {
                if (areRulesConflicting(rule1, rule2)) {
                    conflicts++;
                }
            }
        }
        
        // عد التعارضات بين القواعد الديناميكية
        for (DynamicRule rule1 : dynamicRules.values()) {
            for (DynamicRule rule2 : dynamicRules.values()) {
                if (areDynamicRulesConflicting(rule1, rule2)) {
                    conflicts++;
                }
            }
        }
        
        // عد التعارضات بين القواعد الثابتة والديناميكية
        for (Rule staticRule : staticRules.values()) {
            for (DynamicRule dynamicRule : dynamicRules.values()) {
                if (areRulesConflicting(staticRule, dynamicRule)) {
                    conflicts++;
                }
            }
        }
        
        return conflicts;
    }
} 