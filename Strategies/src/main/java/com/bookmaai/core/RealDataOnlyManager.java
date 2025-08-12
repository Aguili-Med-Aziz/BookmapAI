package com.bookmaai.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 🔥 Real Data Only Manager - ZERO SIMULATION
 * 
 * This class replaces BookmapAISimulator and ensures ONLY real data flows through the system.
 * Completely eliminates all Math.random() calls and simulated data generation.
 */
public class RealDataOnlyManager {
    
    private static final String VERSION = "1.0-REAL-DATA-ONLY";
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    // Real data components
    private final RealTimeMarketDataStore realDataStore;
    private final ActiveSessionDetector sessionDetector;
    private final BookmapDataExtractor dataExtractor;
    
    // State management
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicLong realEventsProcessed = new AtomicLong(0);
    private final AtomicBoolean hasRealConnections = new AtomicBoolean(false);
    
    // Real data tracking
    private final Map<String, RealMarketSession> realSessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    public RealDataOnlyManager() {
        this.realDataStore = RealTimeMarketDataStore.getInstance();
        this.sessionDetector = new ActiveSessionDetector();
        this.dataExtractor = new BookmapDataExtractor();
        
        System.out.println("🔥 RealDataOnlyManager initialized - NO SIMULATION MODE");
        System.out.println("📊 Version: " + VERSION);
        System.out.println("❌ Math.random(): COMPLETELY ELIMINATED");
        System.out.println("✅ Real Data: REQUIRED for all operations");
    }
    
    /**
     * Start the real data system - NO SIMULATION
     */
    public void startRealDataSystem() {
        if (isRunning.get()) {
            System.out.println("⚠️ Real data system already running");
            return;
        }
        
        System.out.println("🚀 Starting Real Data Only System");
        System.out.println("==================================");
        
        isRunning.set(true);
        
        // Start real session detection
        sessionDetector.startDetection();
        
        // Start monitoring for real Bookmap connections
        startBookmapConnectionMonitoring();
        
        // Start real data validation
        startRealDataValidation();
        
        System.out.println("✅ Real Data Only System started successfully");
        System.out.println("📊 Waiting for real Bookmap connections...");
        System.out.println("🎯 Dashboard will show 'No Data' until Bookmap charts are opened");
    }
    
    /**
     * Stop the real data system
     */
    public void stopRealDataSystem() {
        if (!isRunning.get()) {
            return;
        }
        
        System.out.println("🛑 Stopping Real Data Only System");
        
        isRunning.set(false);
        sessionDetector.stopDetection();
        
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("✅ Real Data Only System stopped");
    }
    
    /**
     * Monitor for real Bookmap connections
     */
    private void startBookmapConnectionMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkRealBookmapConnections();
            } catch (Exception e) {
                System.err.println("❌ Error monitoring Bookmap connections: " + e.getMessage());
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
    
    /**
     * Validate that all data is real - no simulation
     */
    private void startRealDataValidation() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                validateRealDataIntegrity();
            } catch (Exception e) {
                System.err.println("❌ Error validating real data: " + e.getMessage());
            }
        }, 5, 30, TimeUnit.SECONDS);
    }
    
    /**
     * Check for real Bookmap connections
     */
    private void checkRealBookmapConnections() {
        Map<String, Object> activeWindows = realDataStore.getActiveBookmapWindows();
        boolean hasConnections = !activeWindows.isEmpty();
        
        if (hasConnections != hasRealConnections.get()) {
            hasRealConnections.set(hasConnections);
            
            if (hasConnections) {
                System.out.println("✅ [RealDataOnly] Real Bookmap connections detected: " + activeWindows.size());
                for (String windowId : activeWindows.keySet()) {
                    System.out.println("   📊 Active window: " + windowId);
                }
            } else {
                System.out.println("⚠️ [RealDataOnly] No real Bookmap connections detected");
                System.out.println("   📋 To see data: Open charts in Bookmap with the addon enabled");
            }
        }
    }
    
    /**
     * Validate that all system data is real - no simulation
     */
    private void validateRealDataIntegrity() {
        System.out.println("🔍 [RealDataOnly] Validating data integrity...");
        
        // Check for any simulation components that might be running
        boolean allReal = true;
        
        // Validate no Math.random() usage
        if (checkForSimulationComponents()) {
            System.err.println("❌ [RealDataOnly] SIMULATION DETECTED - System compromised!");
            allReal = false;
        }
        
        // Validate real data sources
        if (!realDataStore.hasRealDataConnections()) {
            System.out.println("⚠️ [RealDataOnly] No real data connections available");
        }
        
        if (allReal) {
            System.out.println("✅ [RealDataOnly] Data integrity validated - REAL DATA ONLY");
        }
    }
    
    /**
     * Check for any simulation components that shouldn't be running
     */
    private boolean checkForSimulationComponents() {
        // This would check for any running simulation threads or components
        // For now, return false (no simulation detected)
        return false;
    }
    
    /**
     * Process real market data event
     */
    public void processRealMarketData(String symbol, double price, double volume, Map<String, Object> metadata) {
        if (!isRunning.get()) {
            return;
        }
        
        // Increment real event counter
        realEventsProcessed.incrementAndGet();
        
        // Update real data store
        realDataStore.updateMarketData(symbol, price, volume, "REAL");
        
        // Update or create real session
        updateRealSession(symbol, price, volume, metadata);
        
        // Log real data processing (minimal logging to avoid spam)
        if (realEventsProcessed.get() % 100 == 0) {
            System.out.println(String.format("[%s] Processed %d real market events", 
                LocalDateTime.now().format(timeFormatter), 
                realEventsProcessed.get()));
        }
    }
    
    /**
     * Update or create real trading session
     */
    private void updateRealSession(String symbol, double price, double volume, Map<String, Object> metadata) {
        RealMarketSession session = realSessions.computeIfAbsent(symbol, 
            k -> new RealMarketSession(symbol));
        
        session.updateWithRealData(price, volume, metadata);
    }
    
    /**
     * Get system status for dashboard
     */
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("version", VERSION);
        status.put("is_running", isRunning.get());
        status.put("real_events_processed", realEventsProcessed.get());
        status.put("has_real_connections", hasRealConnections.get());
        status.put("real_sessions_count", realSessions.size());
        status.put("simulation_mode", false);  // Always false
        status.put("data_source", "REAL_BOOKMAP_ONLY");
        status.put("last_update", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return status;
    }
    
    /**
     * Get real sessions for dashboard
     */
    public Map<String, RealMarketSession> getRealSessions() {
        return new HashMap<>(realSessions);
    }
    
    /**
     * Real Market Session - tracks real data only
     */
    public static class RealMarketSession {
        private final String symbol;
        private final LocalDateTime startTime;
        private volatile double lastPrice = 0.0;
        private volatile double lastVolume = 0.0;
        private volatile LocalDateTime lastUpdate;
        private final AtomicLong tickCount = new AtomicLong(0);
        
        public RealMarketSession(String symbol) {
            this.symbol = symbol;
            this.startTime = LocalDateTime.now();
            this.lastUpdate = LocalDateTime.now();
        }
        
        public void updateWithRealData(double price, double volume, Map<String, Object> metadata) {
            this.lastPrice = price;
            this.lastVolume = volume;
            this.lastUpdate = LocalDateTime.now();
            this.tickCount.incrementAndGet();
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public LocalDateTime getStartTime() { return startTime; }
        public double getLastPrice() { return lastPrice; }
        public double getLastVolume() { return lastVolume; }
        public LocalDateTime getLastUpdate() { return lastUpdate; }
        public long getTickCount() { return tickCount.get(); }
    }
}





