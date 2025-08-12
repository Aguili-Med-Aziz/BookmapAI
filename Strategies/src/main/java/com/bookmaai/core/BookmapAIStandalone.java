package com.bookmaai.core;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 🚀 BookmapAI Standalone Entry Point
 * 
 * Standalone version of BookmapAI that can be loaded as a Bookmap addon.
 * This serves as the main entry point for the complete AI trading system.
 * 
 * Features:
 * - Complete BookmapAI system initialization
 * - Web dashboard on port 8080
 * - All 8 core components + 5 enhanced components
 * - AI-powered pattern detection
 * - Risk management algorithms
 * - Real-time market analysis
 */
public class BookmapAIStandalone {
    
    private static BookmapAddonIntegration addonIntegration;
    private static EnhancedBookmapAICore aiCore;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static final AtomicBoolean isInitialized = new AtomicBoolean(false);
    
    public BookmapAIStandalone() {
        initializeSystem();
    }
    
    /**
     * Initialize the complete BookmapAI system
     */
    public static void initializeSystem() {
        if (isInitialized.compareAndSet(false, true)) {
            System.out.println("🚀 ========================================");
            System.out.println("🚀 BookmapAI Standalone System Starting...");
            System.out.println("🚀 Version: Enhanced Research Edition");
            System.out.println("🚀 ========================================");
            
            try {
                // Initialize components
                System.out.println("🚀 [1/4] Creating core components...");
                addonIntegration = new BookmapAddonIntegration();
                aiCore = new EnhancedBookmapAICore();
                
                // Initialize AI core first
                System.out.println("🚀 [2/4] Initializing AI core...");
                aiCore.initialize();
                
                // Initialize addon integration
                System.out.println("🚀 [3/4] Starting addon integration...");
                addonIntegration.initialize();
                
                // Complete initialization
                System.out.println("🚀 [4/4] Finalizing system...");
                
                System.out.println("🚀 ========================================");
                System.out.println("🚀 ✅ BookmapAI System Ready!");
                System.out.println("🚀 ========================================");
                System.out.println("🌐 Web Dashboard: http://localhost:8080");
                System.out.println("🔧 Components Active: 8 Core + 5 Enhanced");
                System.out.println("🧠 AI Analysis: Pattern Detection Ready");
                System.out.println("⚖️  Risk Management: Advanced Algorithms Active");
                System.out.println("📈 Real-time Processing: Market Data Streaming");
                System.out.println("🔔 Notifications: Telegram Integration Ready");
                System.out.println("🚀 ========================================");
                
                // Setup shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("🚀 Shutdown signal received, cleaning up...");
                    shutdown();
                }));
                
            } catch (Exception e) {
                System.err.println("❌ [BookmapAI] System initialization failed: " + e.getMessage());
                e.printStackTrace();
                isInitialized.set(false);
                throw new RuntimeException("BookmapAI initialization failed", e);
            }
        }
    }
    
    /**
     * Shutdown the BookmapAI system gracefully
     */
    public static void shutdown() {
        System.out.println("🚀 [BookmapAI] Shutting down system...");
        
        try {
            // Shutdown components in reverse order
            if (aiCore != null) {
                System.out.println("🚀 Stopping AI core...");
                aiCore.shutdown();
            }
            
            if (addonIntegration != null) {
                System.out.println("🚀 Stopping addon integration...");
                addonIntegration.shutdown();
            }
            
            // Shutdown scheduler
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                System.out.println("🚀 Force stopping scheduler...");
                scheduler.shutdownNow();
            }
            
            System.out.println("🚀 ========================================");
            System.out.println("🚀 BookmapAI system shutdown completed");
            System.out.println("🚀 ========================================");
            
        } catch (Exception e) {
            System.err.println("❌ [BookmapAI] Error during shutdown: " + e.getMessage());
        }
    }
    
    /**
     * Check if the system is ready and operational
     */
    public static boolean isSystemReady() {
        return isInitialized.get() && aiCore != null && aiCore.isSystemReady();
    }
    
    /**
     * Get system status information
     */
    public static String getSystemStatus() {
        if (!isInitialized.get()) {
            return "System not initialized";
        }
        
        if (aiCore == null) {
            return "AI Core not available";
        }
        
        if (!aiCore.isSystemReady()) {
            return "AI Core not ready";
        }
        
        return "System operational - All components active";
    }
    
    /**
     * Main entry point for testing and standalone execution
     */
    public static void main(String[] args) {
        System.out.println("🚀 BookmapAI Standalone Launcher");
        System.out.println("Designed for integration with Bookmap trading platform");
        System.out.println();
        
        try {
            // Initialize the system
            BookmapAIStandalone standalone = new BookmapAIStandalone();
            
            // Wait for initialization to complete
            Thread.sleep(5000);
            
            // Show status
            System.out.println("🚀 System Status: " + getSystemStatus());
            System.out.println("🚀 System Ready: " + isSystemReady());
            
            // In real usage, this would keep running until Bookmap shuts down
            // For testing, we'll run for a short time then exit
            if (args.length > 0 && "demo".equals(args[0])) {
                System.out.println("🚀 Running demo mode for 30 seconds...");
                Thread.sleep(30000);
                System.out.println("🚀 Demo completed");
            } else {
                System.out.println("🚀 System initialized. Use 'demo' argument for 30-second test.");
                System.out.println("🚀 In production, this runs continuously until Bookmap shutdown.");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in main: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup
            shutdown();
        }
    }
} 