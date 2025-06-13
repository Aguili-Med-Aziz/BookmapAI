package com.bookmaai.knowledge;

import lombok.Builder;
import lombok.Data;

/**
 * تقرير جودة قاعدة المعرفة
 * يحتوي على مقاييس مختلفة لتقييم جودة القواعد والأداء
 */
@Data
@Builder
public class QualityReport {
    
    // ═══════════════════════════════════════════
    // مقاييس الجودة الأساسية
    // ═══════════════════════════════════════════
    
    /**
     * عدد القواعد الكلي
     */
    private final int ruleCount;
    
    /**
     * نسبة تغطية القواعد للحالات المختلفة (0-1)
     */
    private final double coverage;
    
    /**
     * درجة تناسق القواعد (0-1)
     */
    private final double consistency;
    
    /**
     * مقياس الأداء العام (0-1)
     */
    private final double performance;
    
    /**
     * مقياس قدرة النظام على التكيف (0-1)
     */
    private final double adaptability;
    
    // ═══════════════════════════════════════════
    // مقاييس الجودة المشتقة
    // ═══════════════════════════════════════════
    
    /**
     * الدرجة الكلية لجودة قاعدة المعرفة
     * @return درجة من 0 إلى 1
     */
    public double getOverallScore() {
        return (coverage * 0.25 +
                consistency * 0.25 +
                performance * 0.3 +
                adaptability * 0.2);
    }
    
    /**
     * تصنيف جودة قاعدة المعرفة
     * @return تصنيف الجودة
     */
    public QualityGrade getGrade() {
        double score = getOverallScore();
        
        if (score >= 0.95) return QualityGrade.EXCELLENT;
        if (score >= 0.85) return QualityGrade.VERY_GOOD;
        if (score >= 0.75) return QualityGrade.GOOD;
        if (score >= 0.65) return QualityGrade.FAIR;
        return QualityGrade.POOR;
    }
    
    /**
     * التوصيات لتحسين جودة قاعدة المعرفة
     * @return قائمة بالتوصيات
     */
    public List<Recommendation> getRecommendations() {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // فحص التغطية
        if (coverage < 0.8) {
            recommendations.add(new Recommendation(
                "تحسين التغطية",
                "إضافة قواعد لتغطية المزيد من الحالات",
                RecommendationPriority.HIGH
            ));
        }
        
        // فحص التناسق
        if (consistency < 0.85) {
            recommendations.add(new Recommendation(
                "تحسين التناسق",
                "مراجعة وحل التعارضات بين القواعد",
                RecommendationPriority.HIGH
            ));
        }
        
        // فحص الأداء
        if (performance < 0.75) {
            recommendations.add(new Recommendation(
                "تحسين الأداء",
                "تحليل وتحسين القواعد ذات الأداء الضعيف",
                RecommendationPriority.CRITICAL
            ));
        }
        
        // فحص التكيف
        if (adaptability < 0.7) {
            recommendations.add(new Recommendation(
                "تحسين التكيف",
                "زيادة مرونة القواعد وقدرتها على التعلم",
                RecommendationPriority.MEDIUM
            ));
        }
        
        return recommendations;
    }
    
    /**
     * تحويل التقرير إلى نص مفصل
     */
    @Override
    public String toString() {
        StringBuilder report = new StringBuilder();
        
        report.append("📊 تقرير جودة قاعدة المعرفة\n");
        report.append("══════════════════════════\n\n");
        
        // المقاييس الأساسية
        report.append(String.format("📈 عدد القواعد: %d\n", ruleCount));
        report.append(String.format("📊 التغطية: %.1f%%\n", coverage * 100));
        report.append(String.format("🔄 التناسق: %.1f%%\n", consistency * 100));
        report.append(String.format("⚡ الأداء: %.1f%%\n", performance * 100));
        report.append(String.format("🔧 التكيف: %.1f%%\n\n", adaptability * 100));
        
        // الدرجة الكلية
        report.append(String.format("🏆 الدرجة الكلية: %.1f%% (%s)\n\n",
            getOverallScore() * 100,
            getGrade()
        ));
        
        // التوصيات
        List<Recommendation> recommendations = getRecommendations();
        if (!recommendations.isEmpty()) {
            report.append("💡 التوصيات:\n");
            for (Recommendation rec : recommendations) {
                report.append(String.format("- %s (%s)\n  %s\n",
                    rec.getTitle(),
                    rec.getPriority(),
                    rec.getDescription()
                ));
            }
        }
        
        return report.toString();
    }
}

/**
 * تصنيفات جودة قاعدة المعرفة
 */
enum QualityGrade {
    EXCELLENT("ممتاز"),
    VERY_GOOD("جيد جداً"),
    GOOD("جيد"),
    FAIR("مقبول"),
    POOR("ضعيف");
    
    private final String arabicName;
    
    QualityGrade(String arabicName) {
        this.arabicName = arabicName;
    }
    
    @Override
    public String toString() {
        return arabicName;
    }
}

/**
 * أولويات التوصيات
 */
enum RecommendationPriority {
    CRITICAL("حرجة"),
    HIGH("عالية"),
    MEDIUM("متوسطة"),
    LOW("منخفضة");
    
    private final String arabicName;
    
    RecommendationPriority(String arabicName) {
        this.arabicName = arabicName;
    }
    
    @Override
    public String toString() {
        return arabicName;
    }
}

/**
 * توصية لتحسين جودة قاعدة المعرفة
 */
@Data
class Recommendation {
    private final String title;
    private final String description;
    private final RecommendationPriority priority;
} 