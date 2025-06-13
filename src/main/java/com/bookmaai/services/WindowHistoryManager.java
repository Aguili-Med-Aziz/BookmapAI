package com.bookmaai.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 📚 مدير تاريخ النوافذ - يحفظ ويحلل آخر N نافذة مكتملة
 */
public class WindowHistoryManager {
    
    private static final Logger logger = LoggerFactory.getLogger(WindowHistoryManager.class);
    
    private final int maxWindows;
    private final LinkedList<WindowSummary> completedWindows;
    private final Map<String, List<WindowSummary>> patternHistory;
    
    // إحصائيات عامة
    private double totalPipsGained;
    private int successfulWindows;
    private int totalWindows;
    
    public WindowHistoryManager(int maxWindows) {
        this.maxWindows = maxWindows;
        this.completedWindows = new LinkedList<>();
        this.patternHistory = new HashMap<>();
        this.totalPipsGained = 0.0;
        this.successfulWindows = 0;
        this.totalWindows = 0;
        
        logger.info("📚 WindowHistoryManager initialized with capacity: {}", maxWindows);
    }
    
    /**
     * إضافة نافذة مكتملة للتاريخ
     */
    public void addCompletedWindow(WindowSummary window) {
        // إضافة للقائمة الرئيسية
        completedWindows.addLast(window);
        
        // إزالة النوافذ القديمة إذا تجاوزنا الحد الأقصى
        if (completedWindows.size() > maxWindows) {
            WindowSummary removed = completedWindows.removeFirst();
            updateStatsForRemovedWindow(removed);
        }
        
        // إضافة لتاريخ النمط
        String patternName = window.getPatternName();
        patternHistory.computeIfAbsent(patternName, k -> new ArrayList<>()).add(window);
        
        // تحديث الإحصائيات
        updateStats(window);
        
        logger.debug("📝 Added window to history: {} | Total windows: {}", 
                    window.getPatternName(), completedWindows.size());
    }
    
    /**
     * الحصول على آخر N نوافذ
     */
    public List<WindowSummary> getRecentWindows(int count) {
        int actualCount = Math.min(count, completedWindows.size());
        return completedWindows.subList(completedWindows.size() - actualCount, completedWindows.size());
    }
    
    /**
     * الحصول على تاريخ نمط معين
     */
    public List<WindowSummary> getPatternHistory(String patternName) {
        return patternHistory.getOrDefault(patternName, new ArrayList<>());
    }
    
    /**
     * حساب معدل نجاح نمط معين
     */
    public double getPatternSuccessRate(String patternName) {
        List<WindowSummary> windows = getPatternHistory(patternName);
        if (windows.isEmpty()) {
            return 0.0;
        }
        
        long successCount = windows.stream()
                .mapToLong(w -> w.isSuccessful() ? 1 : 0)
                .sum();
        
        return (double) successCount / windows.size();
    }
    
    /**
     * الحصول على أفضل الأنماط (حسب معدل النجاح)
     */
    public List<Map.Entry<String, Double>> getTopPatterns(int limit) {
        return patternHistory.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= 3) // على الأقل 3 نوافذ
                .map(entry -> Map.entry(entry.getKey(), getPatternSuccessRate(entry.getKey())))
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * تحليل الاتجاهات الحديثة
     */
    public TrendAnalysis analyzeRecentTrends() {
        List<WindowSummary> recent = getRecentWindows(10);
        
        if (recent.isEmpty()) {
            return new TrendAnalysis(0.0, 0.0, "NO_DATA", new HashMap<>());
        }
        
        // حساب متوسط النجاح الحديث
        double recentSuccessRate = recent.stream()
                .mapToDouble(w -> w.isSuccessful() ? 1.0 : 0.0)
                .average()
                .orElse(0.0);
        
        // حساب متوسط النقاط المكتسبة
        double averagePips = recent.stream()
                .mapToDouble(WindowSummary::getPipsGained)
                .average()
                .orElse(0.0);
        
        // تحديد الاتجاه
        String trend = determineTrend(recent);
        
        // تحليل تكرار الأنماط
        Map<String, Long> patternFrequency = recent.stream()
                .collect(Collectors.groupingBy(
                    WindowSummary::getPatternName,
                    Collectors.counting()
                ));
        
        return new TrendAnalysis(recentSuccessRate, averagePips, trend, patternFrequency);
    }
    
    /**
     * تحديد الاتجاه العام
     */
    private String determineTrend(List<WindowSummary> windows) {
        if (windows.size() < 5) {
            return "INSUFFICIENT_DATA";
        }
        
        // مقارنة النصف الأول بالنصف الثاني
        int midPoint = windows.size() / 2;
        List<WindowSummary> firstHalf = windows.subList(0, midPoint);
        List<WindowSummary> secondHalf = windows.subList(midPoint, windows.size());
        
        double firstHalfSuccess = firstHalf.stream()
                .mapToDouble(w -> w.isSuccessful() ? 1.0 : 0.0)
                .average()
                .orElse(0.0);
        
        double secondHalfSuccess = secondHalf.stream()
                .mapToDouble(w -> w.isSuccessful() ? 1.0 : 0.0)
                .average()
                .orElse(0.0);
        
        double improvement = secondHalfSuccess - firstHalfSuccess;
        
        if (improvement > 0.2) return "IMPROVING";
        if (improvement < -0.2) return "DECLINING";
        return "STABLE";
    }
    
    /**
     * تحديث الإحصائيات عند إضافة نافذة
     */
    private void updateStats(WindowSummary window) {
        totalWindows++;
        totalPipsGained += window.getPipsGained();
        
        if (window.isSuccessful()) {
            successfulWindows++;
        }
    }
    
    /**
     * تحديث الإحصائيات عند إزالة نافذة قديمة
     */
    private void updateStatsForRemovedWindow(WindowSummary window) {
        totalPipsGained -= window.getPipsGained();
        
        if (window.isSuccessful()) {
            successfulWindows--;
        }
        
        // إزالة من تاريخ النمط
        String patternName = window.getPatternName();
        List<WindowSummary> patternWindows = patternHistory.get(patternName);
        if (patternWindows != null) {
            patternWindows.remove(window);
            if (patternWindows.isEmpty()) {
                patternHistory.remove(patternName);
            }
        }
    }
    
    // Getters للإحصائيات
    public double getOverallSuccessRate() {
        return totalWindows > 0 ? (double) successfulWindows / totalWindows : 0.0;
    }
    
    public double getTotalPipsGained() { return totalPipsGained; }
    public int getSuccessfulWindows() { return successfulWindows; }
    public int getTotalWindows() { return totalWindows; }
    public int getCurrentWindowCount() { return completedWindows.size(); }
    public List<WindowSummary> getAllWindows() { return new ArrayList<>(completedWindows); }
    
    /**
     * 📈 تحليل الاتجاهات - فئة البيانات
     */
    public static class TrendAnalysis {
        private final double recentSuccessRate;
        private final double averagePips;
        private final String trend;
        private final Map<String, Long> patternFrequency;
        
        public TrendAnalysis(double recentSuccessRate, double averagePips, String trend, 
                           Map<String, Long> patternFrequency) {
            this.recentSuccessRate = recentSuccessRate;
            this.averagePips = averagePips;
            this.trend = trend;
            this.patternFrequency = patternFrequency;
        }
        
        public double getRecentSuccessRate() { return recentSuccessRate; }
        public double getAveragePips() { return averagePips; }
        public String getTrend() { return trend; }
        public Map<String, Long> getPatternFrequency() { return patternFrequency; }
        
        @Override
        public String toString() {
            return String.format("TrendAnalysis[Success: %.1f%%, Avg Pips: %.1f, Trend: %s]",
                    recentSuccessRate * 100, averagePips, trend);
        }
    }
}
