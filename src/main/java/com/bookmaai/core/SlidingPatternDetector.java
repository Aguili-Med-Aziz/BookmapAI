package com.bookmaai.core;

import com.bookmaai.services.SlidingWindowAggregator;
import com.bookmaai.services.WindowHistoryManager;
import com.bookmaai.services.WindowSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 🎯 Sliding Pattern Detector - كاشف الأنماط بالنافذة المتحركة
 * 
 * ينسق العمل بين:
 * - استقبال بيانات السوق من BookmapDataProcessor
 * - استخدام SlidingWindowAggregator لتجميع البيانات ضمن نوافذ 15 دقيقة
 * - دمج نتائج الأدوات الـ12 مع نظام النافذة المتحركة
 * - استخدام WindowHistoryManager لحفظ التاريخ والتحليل
 * - تسجيل الاكتشافات في CSV والسجلات
 */
@Service
public class SlidingPatternDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(SlidingPatternDetector.class);
    
    @Autowired
    private PatternEngineAdvanced patternEngine;
    
    private SlidingWindowAggregator aggregator;
    private WindowHistoryManager historyManager;
    private boolean csvOutputEnabled;
    private String csvFilePath;
    
    // إحصائيات النظام
    private long totalSnapshotsProcessed;
    private long systemStartTime;
    
    public SlidingPatternDetector() {
        // تهيئة المكونات بالقيم الافتراضية
        this.aggregator = new SlidingWindowAggregator(15); // 15 دقيقة
        this.historyManager = new WindowHistoryManager(20); // آخر 20 نافذة
        this.csvOutputEnabled = true;
        this.csvFilePath = "pattern_detections.csv";
        this.totalSnapshotsProcessed = 0;
        this.systemStartTime = System.currentTimeMillis();
        
        // إنشاء header للـ CSV
        initializeCsvFile();
        
        logger.info("🎯 SlidingPatternDetector initialized successfully");
    }
    
    /**
     * تهيئة ملف CSV
     */
    private void initializeCsvFile() {
        if (!csvOutputEnabled) return;
        
        try (FileWriter writer = new FileWriter(csvFilePath, false)) {
            // إضافة header للـ CSV
            writer.write("WindowStart,WindowEnd,PatternName,Detected,Confidence,Outcome,PipsGained,");
            writer.write("TotalVolume,PeakCVD,TotalAbsorption,IcebergVolume,");
            writer.write("Tier1Score,Tier2Score,Tier3Score,Tier4Score,OverallScore\n");
            
            logger.info("📄 CSV file initialized: {}", csvFilePath);
        } catch (IOException e) {
            logger.error("❌ Failed to initialize CSV file: {}", e.getMessage());
            csvOutputEnabled = false;
        }
    }
    
    /**
     * معالجة بيانات السوق الجديدة
     * يُستدعى من BookmapDataProcessor عند وصول بيانات جديدة
     */
    public void processMarketData(String symbol, double price, double volume, double vwap) {
        try {
            totalSnapshotsProcessed++;
            
            // تحليل البيانات باستخدام الأدوات الـ12
            AnalysisResult analysis = patternEngine.analyzePatterns(symbol, price, volume, vwap);
            
            if (analysis == null) {
                logger.warn("⚠️ Analysis returned null for symbol: {}", symbol);
                return;
            }
            
            // إنشاء لقطة سوق للنافذة المتحركة
            SlidingWindowAggregator.MarketSnapshot snapshot = createMarketSnapshot(
                price, volume, vwap, analysis
            );
            
            // إضافة اللقطة للمجمع
            aggregator.addSnapshot(snapshot);
            
            // التحقق من اكتمال النافذة
            if (aggregator.isWindowReady()) {
                finalizeCurrentWindow(symbol, analysis);
            }
            
            logger.debug("📊 Market data processed: {} - price: {:.5f}, volume: {:.2f}", 
                        symbol, price, volume);
            
        } catch (Exception e) {
            logger.error("❌ Error processing market data for {}: {}", symbol, e.getMessage(), e);
        }
    }
    
    /**
     * إنشاء لقطة سوق من البيانات والتحليل
     */
    private SlidingWindowAggregator.MarketSnapshot createMarketSnapshot(
            double price, double volume, double vwap, AnalysisResult analysis) {
        
        LocalDateTime timestamp = LocalDateTime.now();
        double bidPrice = price;
        double askPrice = price + 0.0001; // تقريب بسيط
        
        // استخراج المقاييس من نتيجة التحليل
        double absorptionMeasure = calculateAbsorptionFromAnalysis(analysis);
        double cvd = calculateCVDFromAnalysis(analysis);
        double icebergVolume = calculateIcebergVolumeFromAnalysis(analysis);
        
        return new SlidingWindowAggregator.MarketSnapshot(
            timestamp, bidPrice, askPrice, volume, 
            absorptionMeasure, cvd, icebergVolume
        );
    }
    
    /**
     * حساب مقياس الامتصاص من التحليل
     */
    private double calculateAbsorptionFromAnalysis(AnalysisResult analysis) {
        // استخراج من إشارات التحليل
        String signals = String.join(" ", analysis.getSignals());
        
        if (signals.contains("Absorption") || signals.contains("امتصاص")) {
            return 0.8; // امتصاص قوي
        } else if (signals.contains("Support") || signals.contains("دعم")) {
            return 0.5; // امتصاص متوسط
        }
        
        return 0.1; // امتصاص ضعيف
    }
    
    /**
     * حساب CVD من التحليل
     */
    private double calculateCVDFromAnalysis(AnalysisResult analysis) {
        String direction = analysis.getDirection();
        double confidence = analysis.getConfidence();
        
        if ("BULLISH".equals(direction)) {
            return confidence * 1000; // CVD إيجابي
        } else if ("BEARISH".equals(direction)) {
            return -confidence * 1000; // CVD سلبي
        }
        
        return 0.0; // محايد
    }
    
    /**
     * حساب حجم الآيسبرج من التحليل
     */
    private double calculateIcebergVolumeFromAnalysis(AnalysisResult analysis) {
        String signals = String.join(" ", analysis.getSignals());
        
        if (signals.contains("Iceberg") || signals.contains("آيسبرج")) {
            return 500.0; // حجم آيسبرج مكتشف
        } else if (signals.contains("Large") || signals.contains("كبير")) {
            return 200.0; // حجم متوسط
        }
        
        return 0.0; // لا يوجد آيسبرج
    }
    
    /**
     * إنهاء النافذة الحالية وإنشاء الملخص
     */
    private void finalizeCurrentWindow(String symbol, AnalysisResult currentAnalysis) {
        try {
            // تحديد نوع النمط من التحليل الحالي
            String patternName = determinePatternName(currentAnalysis);
            
            // إنهاء النافذة وإنشاء الملخص
            WindowSummary summary = aggregator.finalizeWindow(patternName);
            
            if (summary != null) {
                // إضافة الملخص للتاريخ
                historyManager.addCompletedWindow(summary);
                
                // تسجيل النتائج في CSV
                recordWindowResults(summary, currentAnalysis);
                
                // تحليل الأداء وإنشاء تقارير
                analyzePerformanceAndReport();
                
                logger.info("✅ Window finalized: {} - Pattern: {}", 
                           summary.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                           patternName);
            }
            
        } catch (Exception e) {
            logger.error("❌ Error finalizing window: {}", e.getMessage(), e);
        }
    }
    
    /**
     * تحديد نوع النمط من التحليل
     */
    private String determinePatternName(AnalysisResult analysis) {
        String signal = analysis.getSignal();
        String direction = analysis.getDirection();
        
        if ("STRONG_BUY".equals(signal) || "STRONG_SELL".equals(signal)) {
            return "strong_momentum";
        } else if ("BUY".equals(signal) || "SELL".equals(signal)) {
            return "volume_confirmation";
        } else if ("BULLISH".equals(direction) || "BEARISH".equals(direction)) {
            return "accumulation_distribution";
        } else {
            return "consolidation";
        }
    }
    
    /**
     * تسجيل نتائج النافذة في CSV
     */
    private void recordWindowResults(WindowSummary summary, AnalysisResult analysis) {
        if (!csvOutputEnabled) return;
        
        try (FileWriter writer = new FileWriter(csvFilePath, true)) {
            // بيانات النافذة
            writer.write(String.format("%s,%s,", 
                summary.getStartTime().toString(),
                summary.getEndTime().toString()));
            
            // بيانات النمط
            writer.write(String.format("%s,%s,%.3f,%s,%.1f,",
                summary.getPatternName(),
                summary.isPatternDetected() ? "YES" : "NO",
                summary.getPatternConfidence(),
                summary.getOutcome(),
                summary.getPipsGained()));
            
            // بيانات السوق
            writer.write(String.format("%.2f,%.2f,%.2f,%.2f,",
                summary.getAverageVolume(),
                summary.getPeakCVD(),
                summary.getTotalAbsorption(),
                summary.getTotalIcebergVolume()));
            
            // نتائج الأدوات الـ12 (محاكاة)
            writer.write(String.format("%.3f,%.3f,%.3f,%.3f,%.3f\n",
                analysis.getConfidence() * 0.9, // Tier 1
                analysis.getConfidence() * 0.8, // Tier 2
                analysis.getConfidence() * 0.7, // Tier 3
                analysis.getConfidence() * 0.6, // Tier 4
                analysis.getConfidence()));     // Overall
            
        } catch (IOException e) {
            logger.error("❌ Failed to write to CSV: {}", e.getMessage());
        }
    }
    
    /**
     * تحليل الأداء وإنشاء التقارير
     */
    private void analyzePerformanceAndReport() {
        // كل 10 نوافذ، أنشئ تقرير أداء
        if (historyManager.getTotalWindows() % 10 == 0) {
            double successRate = historyManager.getOverallSuccessRate();
            double totalPips = historyManager.getTotalPipsGained();
            
            logger.info("📈 Performance Report: Success: {:.1f}%, Total Pips: {:.1f}", 
                       successRate * 100, totalPips);
            logger.info("🏆 Top Patterns: {}", historyManager.getTopPatterns(3));
            
            // تحليل الاتجاهات
            WindowHistoryManager.TrendAnalysis trends = historyManager.analyzeRecentTrends();
            logger.info("📊 Market Trends: {}", trends);
        }
    }
    
    /**
     * الحصول على إحصائيات النظام
     */
    public SystemStats getSystemStats() {
        long uptimeMs = System.currentTimeMillis() - systemStartTime;
        double processingRate = totalSnapshotsProcessed > 0 ? 
            (double) totalSnapshotsProcessed / (uptimeMs / 1000.0) : 0.0;
        
        return new SystemStats(
            totalSnapshotsProcessed,
            historyManager.getTotalWindows(),
            historyManager.getSuccessfulWindows(),
            historyManager.getTotalPipsGained(),
            processingRate,
            uptimeMs / 1000.0,
            String.format("Active: %s, Snapshots: %d", 
                aggregator.isWindowActive(), aggregator.getCurrentSnapshotCount())
        );
    }
    
    /**
     * تنظيف النظام
     */
    public void cleanup() {
        if (csvOutputEnabled) {
            logger.info("📄 CSV file saved to: {}", csvFilePath);
        }
        logger.info("🧹 SlidingPatternDetector cleaned up");
        logger.info("📊 Final Stats: {}", getSystemStats());
    }
    
    /**
     * فئة إحصائيات النظام
     */
    public static class SystemStats {
        private final long totalSnapshots;
        private final int totalWindows;
        private final int totalPatterns;
        private final double totalPips;
        private final double processingRate;
        private final double uptimeSeconds;
        private final String currentWindowInfo;
        
        public SystemStats(long totalSnapshots, int totalWindows, int totalPatterns,
                          double totalPips, double processingRate, double uptimeSeconds,
                          String currentWindowInfo) {
            this.totalSnapshots = totalSnapshots;
            this.totalWindows = totalWindows;
            this.totalPatterns = totalPatterns;
            this.totalPips = totalPips;
            this.processingRate = processingRate;
            this.uptimeSeconds = uptimeSeconds;
            this.currentWindowInfo = currentWindowInfo;
        }
        
        // Getters
        public long getTotalSnapshots() { return totalSnapshots; }
        public int getTotalWindows() { return totalWindows; }
        public int getTotalPatterns() { return totalPatterns; }
        public double getTotalPips() { return totalPips; }
        public double getProcessingRate() { return processingRate; }
        public double getUptimeSeconds() { return uptimeSeconds; }
        public String getCurrentWindowInfo() { return currentWindowInfo; }
        
        @Override
        public String toString() {
            return String.format("SystemStats{snapshots=%d, windows=%d, patterns=%d, " +
                               "pips=%.1f, rate=%.1f/s, uptime=%.0fs}",
                               totalSnapshots, totalWindows, totalPatterns, 
                               totalPips, processingRate, uptimeSeconds);
        }
    }
} 