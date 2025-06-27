package com.bookmaai.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.Collectors;

/**
 * 📊 System Status Monitor - مراقب حالة النظام
 */
public class SystemStatusMonitor {
    
    public enum ComponentStatus {
        RUNNING("يعمل", "🟢"),
        STOPPED("متوقف", "🔴"),
        WARNING("تحذير", "🟡"),
        ERROR("خطأ", "❌"),
        INITIALIZING("تهيئة", "🟦");
        
        private final String arabicName;
        private final String indicator;
        
        ComponentStatus(String arabicName, String indicator) {
            this.arabicName = arabicName;
            this.indicator = indicator;
        }
        
        public String getArabicName() { return arabicName; }
        public String getIndicator() { return indicator; }
    }
    
    public static class ComponentInfo {
        private final String name;
        private volatile ComponentStatus status;
        private final AtomicLong lastUpdate = new AtomicLong(System.currentTimeMillis());
        private final Map<String, Object> stats = new ConcurrentHashMap<>();
        private final List<String> recentLogs = new CopyOnWriteArrayList<>();
        private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
        
        public ComponentInfo(String name) {
            this.name = name;
            this.status = ComponentStatus.INITIALIZING;
        }
        
        public void updateStatus(ComponentStatus status) {
            this.status = status;
            this.lastUpdate.set(System.currentTimeMillis());
        }
        
        public void updateStats(Map<String, Object> newStats) {
            this.stats.clear();
            this.stats.putAll(newStats);
            this.lastUpdate.set(System.currentTimeMillis());
        }
        
        public void addLog(String message) {
            String logEntry = String.format("[%s] %s", new Date(), message);
            recentLogs.add(logEntry);
            
            // Keep only last 50 logs
            if (recentLogs.size() > 50) {
                recentLogs.remove(0);
            }
            
            this.lastUpdate.set(System.currentTimeMillis());
        }
        
        public long getUptimeMinutes() {
            return (System.currentTimeMillis() - startTime.get()) / (60 * 1000);
        }
        
        // Getters
        public String getName() { return name; }
        public ComponentStatus getStatus() { return status; }
        public long getLastUpdate() { return lastUpdate.get(); }
        public Map<String, Object> getStats() { return new HashMap<>(stats); }
        public List<String> getRecentLogs() { return new ArrayList<>(recentLogs); }
    }
    
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final Map<String, ComponentInfo> components = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final AtomicLong systemStartTime = new AtomicLong(System.currentTimeMillis());
    
    // System metrics
    private final AtomicLong totalEvents = new AtomicLong(0);
    private final AtomicLong systemErrors = new AtomicLong(0);
    private final AtomicLong systemWarnings = new AtomicLong(0);
    
    // Health thresholds
    private final long componentTimeoutMs = 5 * 60 * 1000; // 5 minutes
    private final int maxErrorsPerHour = 10;
    
    public SystemStatusMonitor() {
        System.out.println("📊 [SystemStatusMonitor] Initializing System Status Monitor...");
    }
    
    public void initialize() {
        isRunning.set(true);
        startMonitoringTasks();
        System.out.println("📊 [SystemStatusMonitor] Monitor initialized and running");
    }
    
    private void startMonitoringTasks() {
        // Component health check task
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkComponentHealth();
            } catch (Exception e) {
                System.err.println("📊 [SystemStatusMonitor] Error in health check: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.MINUTES);
        
        // System metrics collection task
        scheduler.scheduleAtFixedRate(() -> {
            try {
                collectSystemMetrics();
            } catch (Exception e) {
                System.err.println("📊 [SystemStatusMonitor] Error collecting metrics: " + e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS);
        
        // Status report task
        scheduler.scheduleAtFixedRate(() -> {
            try {
                generateStatusReport();
            } catch (Exception e) {
                System.err.println("📊 [SystemStatusMonitor] Error generating report: " + e.getMessage());
            }
        }, 5, 5, TimeUnit.MINUTES);
    }
    
    // Component management methods
    public void registerComponent(String componentName) {
        components.put(componentName, new ComponentInfo(componentName));
        System.out.println("📊 [SystemStatusMonitor] Registered component: " + componentName);
    }
    
    public void updateComponentStatus(String componentName, boolean isRunning) {
        ComponentInfo component = components.get(componentName);
        if (component != null) {
            ComponentStatus status = isRunning ? ComponentStatus.RUNNING : ComponentStatus.STOPPED;
            component.updateStatus(status);
            
            String statusMsg = String.format("Component %s is now %s", 
                                           componentName, status.getArabicName());
            component.addLog(statusMsg);
            totalEvents.incrementAndGet();
            
            System.out.println("📊 [SystemStatusMonitor] " + statusMsg);
        }
    }
    
    public void updateComponentStats(String componentName, Map<String, Object> stats) {
        ComponentInfo component = components.get(componentName);
        if (component != null) {
            component.updateStats(stats);
            totalEvents.incrementAndGet();
        }
    }
    
    public void logComponentEvent(String componentName, String event, String level) {
        ComponentInfo component = components.get(componentName);
        if (component != null) {
            component.addLog(String.format("[%s] %s", level, event));
            
            if ("ERROR".equals(level)) {
                systemErrors.incrementAndGet();
                component.updateStatus(ComponentStatus.ERROR);
            } else if ("WARNING".equals(level)) {
                systemWarnings.incrementAndGet();
                component.updateStatus(ComponentStatus.WARNING);
            }
            
            totalEvents.incrementAndGet();
        }
    }
    
    private void checkComponentHealth() {
        long currentTime = System.currentTimeMillis();
        
        for (ComponentInfo component : components.values()) {
            long timeSinceLastUpdate = currentTime - component.getLastUpdate();
            
            // Check if component is unresponsive
            if (timeSinceLastUpdate > componentTimeoutMs && 
                component.getStatus() == ComponentStatus.RUNNING) {
                
                component.updateStatus(ComponentStatus.WARNING);
                component.addLog("Component appears unresponsive - no updates for " + 
                               (timeSinceLastUpdate / 1000) + " seconds");
                
                System.out.println("📊 [SystemStatusMonitor] WARNING: " + component.getName() + 
                                 " appears unresponsive");
            }
        }
    }
    
    private void collectSystemMetrics() {
        // Collect JVM metrics
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        // Update system component with metrics
        ComponentInfo systemComponent = components.computeIfAbsent("SYSTEM", ComponentInfo::new);
        
        Map<String, Object> systemStats = new HashMap<>();
        systemStats.put("total_memory_mb", totalMemory / (1024 * 1024));
        systemStats.put("used_memory_mb", usedMemory / (1024 * 1024));
        systemStats.put("free_memory_mb", freeMemory / (1024 * 1024));
        systemStats.put("memory_usage_percent", (usedMemory * 100) / totalMemory);
        systemStats.put("active_threads", Thread.activeCount());
        systemStats.put("uptime_minutes", getSystemUptimeMinutes());
        systemStats.put("total_events", totalEvents.get());
        systemStats.put("system_errors", systemErrors.get());
        systemStats.put("system_warnings", systemWarnings.get());
        
        systemComponent.updateStats(systemStats);
        systemComponent.updateStatus(ComponentStatus.RUNNING);
    }
    
    private void generateStatusReport() {
        StringBuilder report = new StringBuilder();
        report.append("📊 [SystemStatusMonitor] === نقرير حالة النظام ===\n");
        report.append(String.format("⏱️ وقت التشغيل: %d دقيقة\n", getSystemUptimeMinutes()));
        report.append(String.format("📈 المكونات النشطة: %d\n", getActiveComponentsCount()));
        report.append(String.format("📊 إجمالي الأحداث: %d\n", totalEvents.get()));
        report.append(String.format("❌ الأخطاء: %d\n", systemErrors.get()));
        report.append(String.format("⚠️ التحذيرات: %d\n", systemWarnings.get()));
        
        report.append("\n🔧 حالة المكونات:\n");
        for (ComponentInfo component : components.values()) {
            report.append(String.format("  %s %s - %s (منذ %d دقيقة)\n",
                    component.getStatus().getIndicator(),
                    component.getName(),
                    component.getStatus().getArabicName(),
                    (System.currentTimeMillis() - component.getLastUpdate()) / (60 * 1000)));
        }
        
        System.out.println(report.toString());
    }
    
    // Health assessment methods
    public boolean isSystemHealthy() {
        // Check if all critical components are running
        long runningComponents = components.values().stream()
                .filter(c -> c.getStatus() == ComponentStatus.RUNNING)
                .count();
        
        long totalComponents = components.size();
        if (totalComponents == 0) return true; // No components to check
        
        double healthPercentage = (double) runningComponents / totalComponents;
        return healthPercentage >= 0.8; // 80% of components must be running
    }
    
    public ComponentStatus getOverallSystemStatus() {
        if (!isSystemHealthy()) {
            return ComponentStatus.ERROR;
        }
        
        boolean hasWarnings = components.values().stream()
                .anyMatch(c -> c.getStatus() == ComponentStatus.WARNING);
        
        if (hasWarnings) {
            return ComponentStatus.WARNING;
        }
        
        return ComponentStatus.RUNNING;
    }
    
    public int getActiveComponentsCount() {
        return (int) components.values().stream()
                .filter(c -> c.getStatus() == ComponentStatus.RUNNING)
                .count();
    }
    
    public long getSystemUptimeMinutes() {
        return (System.currentTimeMillis() - systemStartTime.get()) / (60 * 1000);
    }
    
    // Query methods
    public ComponentInfo getComponentInfo(String componentName) {
        return components.get(componentName);
    }
    
    public List<ComponentInfo> getAllComponents() {
        return new ArrayList<>(components.values());
    }
    
    public List<ComponentInfo> getComponentsByStatus(ComponentStatus status) {
        return components.values().stream()
                .filter(c -> c.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("is_running", isRunning.get());
        stats.put("system_healthy", isSystemHealthy());
        stats.put("overall_status", getOverallSystemStatus().getArabicName());
        stats.put("uptime_minutes", getSystemUptimeMinutes());
        stats.put("total_components", components.size());
        stats.put("active_components", getActiveComponentsCount());
        stats.put("total_events", totalEvents.get());
        stats.put("system_errors", systemErrors.get());
        stats.put("system_warnings", systemWarnings.get());
        
        // Component status breakdown
        Map<String, Integer> statusBreakdown = new HashMap<>();
        for (ComponentStatus status : ComponentStatus.values()) {
            int count = getComponentsByStatus(status).size();
            statusBreakdown.put(status.name(), count);
        }
        stats.put("status_breakdown", statusBreakdown);
        
        return stats;
    }
    
    public String getSystemHealthReport() {
        StringBuilder report = new StringBuilder();
        report.append("📊 === تقرير صحة النظام ===\n");
        report.append("الحالة العامة: ").append(getOverallSystemStatus().getIndicator())
              .append(" ").append(getOverallSystemStatus().getArabicName()).append("\n");
        report.append("وقت التشغيل: ").append(getSystemUptimeMinutes()).append(" دقيقة\n");
        report.append("المكونات النشطة: ").append(getActiveComponentsCount())
              .append("/").append(components.size()).append("\n");
        
        if (systemErrors.get() > 0) {
            report.append("❌ إجمالي الأخطاء: ").append(systemErrors.get()).append("\n");
        }
        
        if (systemWarnings.get() > 0) {
            report.append("⚠️ إجمالي التحذيرات: ").append(systemWarnings.get()).append("\n");
        }
        
        return report.toString();
    }
    
    public void shutdown() {
        isRunning.set(false);
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("📊 [SystemStatusMonitor] Monitor shutdown completed");
    }
} 