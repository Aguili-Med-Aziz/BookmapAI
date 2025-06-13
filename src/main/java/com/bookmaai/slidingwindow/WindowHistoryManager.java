package com.bookmaai.slidingwindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 📚 مدير تاريخ النوافذ
 * يحتفظ بآخر 20 نافذة للتحليل التاريخي
 */
@Component
public class WindowHistoryManager {
    
    private static final Logger logger = LoggerFactory.getLogger(WindowHistoryManager.class);
    private static final int MAX_WINDOWS = 20;
    
    private LinkedList<WindowSummary> windowHistory = new LinkedList<>();
    
    /**
     * إضافة نافذة جديدة للتاريخ
     */
    public synchronized void addWindow(WindowSummary window) {
        windowHistory.addLast(window);
        
        // الحفاظ على حد أقصى 20 نافذة
        if (windowHistory.size() > MAX_WINDOWS) {
            WindowSummary removed = windowHistory.removeFirst();
            logger.debug("📚 Removed old window from {} (keeping {} windows)", 
                        removed.getStartTime(), MAX_WINDOWS);
        }
        
        logger.info("📚 Added window to history: {} - {} (Total: {} windows)", 
                   window.getStartTime(), window.getEndTime(), windowHistory.size());
    }
    
    /**
     * الحصول على آخر N نوافذ
     */
    public List<WindowSummary> getLastNWindows(int n) {
        int size = windowHistory.size();
        int fromIndex = Math.max(0, size - n);
        return new ArrayList<>(windowHistory.subList(fromIndex, size));
    }
    
    /**
     * الحصول على جميع النوافذ
     */
    public List<WindowSummary> getAllWindows() {
        return new ArrayList<>(windowHistory);
    }
    
    /**
     * البحث عن نوافذ في فترة زمنية معينة
     */
    public List<WindowSummary> getWindowsInTimeRange(LocalDateTime start, LocalDateTime end) {
        List<WindowSummary> result = new ArrayList<>();
        for (WindowSummary window : windowHistory) {
            if (!window.getStartTime().isBefore(start) && !window.getEndTime().isAfter(end)) {
                result.add(window);
            }
        }
        return result;
    }
    
    /**
     * حساب إحصائيات التاريخ
     */
    public HistoryStatistics calculateStatistics() {
        if (windowHistory.isEmpty()) {
            return new HistoryStatistics();
        }
        
        double totalVolume = 0;
        double maxCVD = 0;
        double totalAbsorption = 0;
        int totalPatterns = 0;
        Map<String, Integer> patternFrequency = new HashMap<>();
        
        for (WindowSummary window : windowHistory) {
            totalVolume += window.getTotalVolume();
            maxCVD = Math.max(maxCVD, window.getPeakCVD());
            totalAbsorption += window.getTotalAbsorption();
            
            for (PatternCarryState pattern : window.getPatterns()) {
                totalPatterns++;
                patternFrequency.merge(pattern.getPatternName(), 1, Integer::sum);
            }
        }
        
        return new HistoryStatistics(
            windowHistory.size(),
            totalVolume / windowHistory.size(),
            maxCVD,
            totalAbsorption / windowHistory.size(),
            totalPatterns,
            patternFrequency
        );
    }
    
    /**
     * مسح التاريخ
     */
    public void clearHistory() {
        windowHistory.clear();
        logger.info("📚 Window history cleared");
    }
    
    public int getHistorySize() {
        return windowHistory.size();
    }
}

/**
 * ملخص النافذة
 */
class WindowSummary {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final int snapshotCount;
    private final double totalVolume;
    private final double peakCVD;
    private final double totalAbsorption;
    private final double icebergVolume;
    private final List<PatternCarryState> patterns;
    
    public WindowSummary(LocalDateTime startTime, LocalDateTime endTime, int snapshotCount,
                        double totalVolume, double peakCVD, double totalAbsorption,
                        double icebergVolume, List<PatternCarryState> patterns) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.snapshotCount = snapshotCount;
        this.totalVolume = totalVolume;
        this.peakCVD = peakCVD;
        this.totalAbsorption = totalAbsorption;
        this.icebergVolume = icebergVolume;
        this.patterns = patterns;
    }
    
    // Getters
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public int getSnapshotCount() { return snapshotCount; }
    public double getTotalVolume() { return totalVolume; }
    public double getPeakCVD() { return peakCVD; }
    public double getTotalAbsorption() { return totalAbsorption; }
    public double getIcebergVolume() { return icebergVolume; }
    public List<PatternCarryState> getPatterns() { return patterns; }
}

/**
 * إحصائيات التاريخ
 */
class HistoryStatistics {
    private final int windowCount;
    private final double averageVolume;
    private final double maxCVD;
    private final double averageAbsorption;
    private final int totalPatterns;
    private final Map<String, Integer> patternFrequency;
    
    public HistoryStatistics() {
        this.windowCount = 0;
        this.averageVolume = 0;
        this.maxCVD = 0;
        this.averageAbsorption = 0;
        this.totalPatterns = 0;
        this.patternFrequency = new HashMap<>();
    }
    
    public HistoryStatistics(int windowCount, double averageVolume, double maxCVD,
                           double averageAbsorption, int totalPatterns,
                           Map<String, Integer> patternFrequency) {
        this.windowCount = windowCount;
        this.averageVolume = averageVolume;
        this.maxCVD = maxCVD;
        this.averageAbsorption = averageAbsorption;
        this.totalPatterns = totalPatterns;
        this.patternFrequency = patternFrequency;
    }
    
    // Getters
    public int getWindowCount() { return windowCount; }
    public double getAverageVolume() { return averageVolume; }
    public double getMaxCVD() { return maxCVD; }
    public double getAverageAbsorption() { return averageAbsorption; }
    public int getTotalPatterns() { return totalPatterns; }
    public Map<String, Integer> getPatternFrequency() { return patternFrequency; }
} 