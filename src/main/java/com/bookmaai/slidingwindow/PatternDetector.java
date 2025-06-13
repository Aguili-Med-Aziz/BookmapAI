package com.bookmaai.slidingwindow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.bookmaai.services.BookmapConfigurationLoader;

import java.util.*;

/**
 * 🎯 كاشف الأنماط
 * يحلل اللقطات ويكتشف الأنماط المحتملة
 */
@Component
public class PatternDetector {
    
    @Autowired
    private BookmapConfigurationLoader configLoader;
    
    /**
     * كشف الأنماط من لقطة السوق ونتائج الأدوات
     */
    public List<DetectedPattern> detectPatterns(MarketSnapshot snapshot, Map<String, Double> toolResults) {
        List<DetectedPattern> detectedPatterns = new ArrayList<>();
        
        // تحليل كل نمط متاح في قاعدة المعرفة
        Set<String> availablePatterns = configLoader.getAvailablePatterns();
        
        for (String patternName : availablePatterns) {
            BookmapConfigurationLoader.PatternConfig config = configLoader.getPatternConfiguration(patternName);
            if (config != null && checkPatternConditions(config, toolResults)) {
                DetectedPattern pattern = new DetectedPattern(
                    UUID.randomUUID().toString(),
                    patternName,
                    config.getToolName(),
                    config.getSuccessRate()
                );
                detectedPatterns.add(pattern);
            }
        }
        
        // إضافة أنماط أساسية إذا لم تكن موجودة في قاعدة المعرفة
        detectBasicPatterns(snapshot, toolResults, detectedPatterns);
        
        return detectedPatterns;
    }
    
    /**
     * فحص شروط النمط
     */
    private boolean checkPatternConditions(BookmapConfigurationLoader.PatternConfig config, 
                                         Map<String, Double> toolResults) {
        // التحقق من أن الأداة الرئيسية للنمط نشطة
        String mainTool = config.getToolName();
        Double toolScore = toolResults.get(mainTool);
        
        if (toolScore == null || toolScore < 0.6) {
            return false;
        }
        
        // يمكن إضافة شروط إضافية من config.getConditions()
        return true;
    }
    
    /**
     * كشف الأنماط الأساسية
     */
    private void detectBasicPatterns(MarketSnapshot snapshot, Map<String, Double> toolResults, 
                                    List<DetectedPattern> detectedPatterns) {
        
        // نمط PERFECT_STORM - يحتاج 5+ أدوات قوية
        int strongTools = 0;
        for (Double score : toolResults.values()) {
            if (score > 0.7) strongTools++;
        }
        if (strongTools >= 5) {
            detectedPatterns.add(new DetectedPattern(
                UUID.randomUUID().toString(),
                "PERFECT_STORM_" + snapshot.getSymbol(),
                "multiple",
                0.85
            ));
        }
        
        // نمط CVD_DIVERGENCE
        Double cvdScore = toolResults.get("cvd");
        if (cvdScore != null && cvdScore > 0.8) {
            detectedPatterns.add(new DetectedPattern(
                UUID.randomUUID().toString(),
                "CVD_DIVERGENCE_" + snapshot.getSymbol(),
                "cvd",
                0.75
            ));
        }
        
        // نمط VOLUME_SPIKE
        Double volumeScore = toolResults.get("volume");
        if (volumeScore != null && volumeScore > 0.85) {
            detectedPatterns.add(new DetectedPattern(
                UUID.randomUUID().toString(),
                "VOLUME_SPIKE_" + snapshot.getSymbol(),
                "volume",
                0.70
            ));
        }
        
        // نمط ICEBERG_ACCUMULATION
        Double iceberg = toolResults.get("large_lot");
        Double absorption = toolResults.get("absorption");
        if (iceberg != null && iceberg > 0.7 && absorption != null && absorption > 0.6) {
            detectedPatterns.add(new DetectedPattern(
                UUID.randomUUID().toString(),
                "ICEBERG_ACCUMULATION_" + snapshot.getSymbol(),
                "iceberg_detector",
                0.80
            ));
        }
    }
}

/**
 * النمط المكتشف
 */
class DetectedPattern {
    private final String patternId;
    private final String patternName;
    private final String primaryTool;
    private final double baseSuccessRate;
    
    public DetectedPattern(String patternId, String patternName, String primaryTool, double baseSuccessRate) {
        this.patternId = patternId;
        this.patternName = patternName;
        this.primaryTool = primaryTool;
        this.baseSuccessRate = baseSuccessRate;
    }
    
    public String getPatternId() { return patternId; }
    public String getPatternName() { return patternName; }
    public String getPrimaryTool() { return primaryTool; }
    public double getBaseSuccessRate() { return baseSuccessRate; }
} 