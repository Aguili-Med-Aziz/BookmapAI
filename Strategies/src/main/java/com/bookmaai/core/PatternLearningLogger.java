package com.bookmaai.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.io.*;
import java.text.SimpleDateFormat;

/**
 * 📝 Pattern Learning Logger - مسجل بيانات التعلم
 */
public class PatternLearningLogger {
    
    public static class LearningEvent {
        private final String patternId;
        private final String patternType;
        private final String eventType;
        private final boolean success;
        private final Map<String, Double> features;
        private final Map<String, Object> metadata;
        private final long timestamp;
        
        public LearningEvent(String patternId, String patternType, String eventType, 
                           boolean success, Map<String, Double> features) {
            this.patternId = patternId;
            this.patternType = patternType;
            this.eventType = eventType;
            this.success = success;
            this.features = new HashMap<>(features);
            this.metadata = new HashMap<>();
            this.timestamp = System.currentTimeMillis();
        }
        
        public void addMetadata(String key, Object value) {
            metadata.put(key, value);
        }
        
        public String toCSVLine() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            StringBuilder sb = new StringBuilder();
            
            sb.append(sdf.format(new Date(timestamp))).append(",");
            sb.append(patternId).append(",");
            sb.append(patternType).append(",");
            sb.append(eventType).append(",");
            sb.append(success).append(",");
            
            // Add feature values
            sb.append(features.getOrDefault("confidence", 0.0)).append(",");
            sb.append(features.getOrDefault("progress", 0.0)).append(",");
            sb.append(features.getOrDefault("volume", 0.0)).append(",");
            sb.append(features.getOrDefault("price_action", 0.0)).append(",");
            
            // Add metadata
            sb.append(metadata.getOrDefault("session", "unknown")).append(",");
            sb.append(metadata.getOrDefault("volatility", 0.0));
            
            return sb.toString();
        }
        
        // Getters
        public String getPatternId() { return patternId; }
        public String getPatternType() { return patternType; }
        public String getEventType() { return eventType; }
        public boolean isSuccess() { return success; }
        public Map<String, Double> getFeatures() { return new HashMap<>(features); }
        public long getTimestamp() { return timestamp; }
    }
    
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final BlockingQueue<LearningEvent> eventQueue = new LinkedBlockingQueue<>();
    private final ExecutorService loggerExecutor = Executors.newSingleThreadExecutor();
    private final AtomicLong totalEvents = new AtomicLong(0);
    private final AtomicLong eventsWritten = new AtomicLong(0);
    
    // File management
    private final String logDirectory = "bookmap_learning_logs";
    private PrintWriter currentLogWriter;
    private String currentLogFile;
    private final SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    // Configuration
    private final int maxQueueSize = 10000;
    private final long flushIntervalMs = 10000; // 10 seconds
    
    public PatternLearningLogger() {
        System.out.println("📝 [PatternLearningLogger] Initializing Pattern Learning Logger...");
        createLogDirectory();
    }
    
    public void initialize() {
        isRunning.set(true);
        openCurrentLogFile();
        startLoggingProcessor();
        System.out.println("📝 [PatternLearningLogger] Logger initialized and running");
    }
    
    private void createLogDirectory() {
        File logDir = new File(logDirectory);
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            if (created) {
                System.out.println("📝 [PatternLearningLogger] Log directory created: " + logDir.getAbsolutePath());
            } else {
                System.err.println("📝 [PatternLearningLogger] Failed to create log directory");
            }
        }
    }
    
    private void openCurrentLogFile() {
        try {
            String dateStr = fileNameFormat.format(new Date());
            currentLogFile = logDirectory + "/learning_events_" + dateStr + ".csv";
            
            File logFile = new File(currentLogFile);
            boolean isNewFile = !logFile.exists();
            
            currentLogWriter = new PrintWriter(new FileWriter(logFile, true));
            
            // Write header if new file
            if (isNewFile) {
                writeCSVHeader();
            }
            
            System.out.println("📝 [PatternLearningLogger] Opened log file: " + currentLogFile);
            
        } catch (IOException e) {
            System.err.println("📝 [PatternLearningLogger] Failed to open log file: " + e.getMessage());
        }
    }
    
    private void writeCSVHeader() {
        if (currentLogWriter != null) {
            currentLogWriter.println("timestamp,pattern_id,pattern_type,event_type,success," +
                                   "confidence,progress,volume,price_action,session,volatility");
            currentLogWriter.flush();
        }
    }
    
    private void startLoggingProcessor() {
        loggerExecutor.submit(() -> {
            long lastFlushTime = System.currentTimeMillis();
            
            while (isRunning.get()) {
                try {
                    LearningEvent event = eventQueue.poll(1, TimeUnit.SECONDS);
                    
                    if (event != null) {
                        writeEvent(event);
                        eventsWritten.incrementAndGet();
                    }
                    
                    // Periodic flush
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastFlushTime > flushIntervalMs) {
                        flushLogs();
                        lastFlushTime = currentTime;
                    }
                    
                    // Check if we need to rotate log file (daily rotation)
                    checkLogRotation();
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("📝 [PatternLearningLogger] Error processing event: " + e.getMessage());
                }
            }
        });
    }
    
    private void writeEvent(LearningEvent event) {
        if (currentLogWriter != null) {
            currentLogWriter.println(event.toCSVLine());
        }
    }
    
    private void flushLogs() {
        if (currentLogWriter != null) {
            currentLogWriter.flush();
        }
    }
    
    private void checkLogRotation() {
        String expectedFileName = logDirectory + "/learning_events_" + 
                                fileNameFormat.format(new Date()) + ".csv";
        
        if (!expectedFileName.equals(currentLogFile)) {
            // Need to rotate to new file
            closeCurrentLogFile();
            openCurrentLogFile();
        }
    }
    
    private void closeCurrentLogFile() {
        if (currentLogWriter != null) {
            currentLogWriter.flush();
            currentLogWriter.close();
            currentLogWriter = null;
            System.out.println("📝 [PatternLearningLogger] Closed log file: " + currentLogFile);
        }
    }
    
    // Public logging methods
    public void logPatternDetection(AdvancedPatternEngine.DetectedPattern pattern) {
        if (!isRunning.get()) return;
        
        Map<String, Double> features = new HashMap<>();
        features.put("confidence", (double) pattern.getConfidence());
        features.put("progress", (double) pattern.getProgress());
        features.put("tools_count", (double) pattern.getTools().size());
        
        // Add pattern indicators
        for (Map.Entry<String, Double> indicator : pattern.getIndicators().entrySet()) {
            features.put("indicator_" + indicator.getKey(), indicator.getValue());
        }
        
        LearningEvent event = new LearningEvent(pattern.getId(), pattern.getType().name(),
                                              "DETECTION", false, features);
        event.addMetadata("symbol", pattern.getSymbol());
        event.addMetadata("price", pattern.getPrice());
        event.addMetadata("stage", pattern.getStage().name());
        
        queueEvent(event);
    }
    
    public void logLearningEvent(AdvancedPatternEngine.DetectedPattern pattern, boolean success,
                               Map<String, Double> features) {
        if (!isRunning.get()) return;
        
        LearningEvent event = new LearningEvent(pattern.getId(), pattern.getType().name(),
                                              "LEARNING", success, features);
        event.addMetadata("symbol", pattern.getSymbol());
        event.addMetadata("final_confidence", pattern.getConfidence());
        event.addMetadata("final_progress", pattern.getProgress());
        
        queueEvent(event);
    }
    
    public void logPatternOutcome(AdvancedPatternEngine.DetectedPattern pattern, boolean success,
                                double profit, long durationMinutes) {
        if (!isRunning.get()) return;
        
        Map<String, Double> features = new HashMap<>();
        features.put("final_confidence", (double) pattern.getConfidence());
        features.put("final_progress", (double) pattern.getProgress());
        features.put("profit", profit);
        features.put("duration_minutes", (double) durationMinutes);
        
        LearningEvent event = new LearningEvent(pattern.getId(), pattern.getType().name(),
                                              "OUTCOME", success, features);
        event.addMetadata("symbol", pattern.getSymbol());
        event.addMetadata("profit", profit);
        event.addMetadata("duration", durationMinutes);
        
        queueEvent(event);
    }
    
    public void logSystemEvent(String component, String eventType, Map<String, Object> data) {
        if (!isRunning.get()) return;
        
        Map<String, Double> features = new HashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof Number) {
                features.put(entry.getKey(), ((Number) entry.getValue()).doubleValue());
            }
        }
        
        LearningEvent event = new LearningEvent("SYSTEM", component, eventType, true, features);
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            event.addMetadata(entry.getKey(), entry.getValue());
        }
        
        queueEvent(event);
    }
    
    private void queueEvent(LearningEvent event) {
        totalEvents.incrementAndGet();
        
        boolean queued = eventQueue.offer(event);
        if (!queued) {
            System.err.println("📝 [PatternLearningLogger] Event queue full, dropping event");
        }
    }
    
    // Analysis methods
    public Map<String, Object> generateLearningReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("total_events", totalEvents.get());
        report.put("events_written", eventsWritten.get());
        report.put("queue_size", eventQueue.size());
        report.put("current_log_file", currentLogFile);
        report.put("write_success_rate", calculateWriteSuccessRate());
        
        return report;
    }
    
    private double calculateWriteSuccessRate() {
        long total = totalEvents.get();
        return total > 0 ? (double) eventsWritten.get() / total * 100.0 : 0.0;
    }
    
    public List<String> getRecentLogFiles() {
        File logDir = new File(logDirectory);
        List<String> logFiles = new ArrayList<>();
        
        if (logDir.exists() && logDir.isDirectory()) {
            File[] files = logDir.listFiles((dir, name) -> name.startsWith("learning_events_"));
            if (files != null) {
                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                for (File file : files) {
                    logFiles.add(file.getName());
                }
            }
        }
        
        return logFiles;
    }
    
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("is_running", isRunning.get());
        stats.put("total_events", totalEvents.get());
        stats.put("events_written", eventsWritten.get());
        stats.put("queue_size", eventQueue.size());
        stats.put("current_log_file", currentLogFile);
        stats.put("write_success_rate", calculateWriteSuccessRate());
        stats.put("recent_log_files", getRecentLogFiles());
        return stats;
    }
    
    public void shutdown() {
        isRunning.set(false);
        
        // Process remaining events
        while (!eventQueue.isEmpty()) {
            LearningEvent event = eventQueue.poll();
            if (event != null) {
                writeEvent(event);
                eventsWritten.incrementAndGet();
            }
        }
        
        closeCurrentLogFile();
        loggerExecutor.shutdown();
        
        try {
            if (!loggerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                loggerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            loggerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("📝 [PatternLearningLogger] Logger shutdown completed");
    }
} 