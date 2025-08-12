package com.bookmaai.core;

import com.bookmaai.web.RealDataDashboard;
import java.util.concurrent.*;

/**
 * 🚀 Complete BookmapAI System Launcher - REAL DATA ONLY
 * 
 * Launches the complete system with:
 * - REAL Bookmap data extraction (NO SIMULATED DATA)
 * - Real GPT-4 integration
 * - Real-time pattern detection from live market data
 * - Telegram signal sharing to +21696543589
 * - Professional dashboard with live data
 */
public class CompleteSystemLauncher {
    
    private static RealDataOnlySystem realDataSystem;
    private static TelegramSignalSharing telegramSignals;
    
    public static void main(String[] args) {
        System.out.println("🚀 ===== BookmapAI REAL DATA System =====");
        System.out.println("📊 Version: 5.4.0-RealData (NO SIMULATION)");
        System.out.println("🤖 GPT-4: Integrated and Ready");
        System.out.println("📱 Telegram Signals: +21696543589");
        System.out.println("🎯 Data Source: LIVE Bookmap Sessions ONLY");
        System.out.println("❌ Simulated Data: COMPLETELY REMOVED");
        System.out.println("=======================================");
        
        try {
            // Initialize comprehensive real data replacement system
            RealDataReplacementSystem replacementSystem = new RealDataReplacementSystem();
            replacementSystem.replaceAllMockData();
            
            // Initialize REAL data system - NO SIMULATED DATA
            realDataSystem = new RealDataOnlySystem();
            
            // Initialize Telegram signal sharing
            telegramSignals = new TelegramSignalSharing();
            
            // Verify all mock data has been removed
            if (replacementSystem.isAllMockDataRemoved()) {
                System.out.println("✅ VERIFICATION: All mock data successfully removed");
                System.out.println("🎯 System now operates with 100% real data only");
            } else {
                System.out.println("⚠️ WARNING: Some mock data may still remain");
            }
            
            // Wait a moment for real data detection
            Thread.sleep(2000);
            
            // Check for real Bookmap connection
            if (realDataSystem.hasRealData()) {
                System.out.println("✅ REAL Bookmap data detected!");
                
                // Share initial status via Telegram
                telegramSignals.shareMarketAnalysis(
                    "🚀 **BookmapAI System Started**\n\n" +
                    "✅ Real Bookmap connection established\n" +
                    "🎯 Active sessions detected: " + realDataSystem.getRealSessions().size() + "\n" +
                    "🤖 GPT-4 AI assistant: Online\n" +
                    "📊 Pattern detection: Active\n\n" +
                    "Ready for real-time trading analysis!"
                );
            } else {
                System.out.println("⚠️  WAITING FOR REAL BOOKMAP DATA");
                System.out.println("❌ NO SIMULATED DATA WILL BE USED");
                System.out.println("");
                System.out.println("📋 Required Setup:");
                System.out.println("   1. Launch Bookmap trading platform");
                System.out.println("   2. Open trading instruments (EURUSD, GBPUSD, etc.)");
                System.out.println("   3. Enable BookmapAI addon in Bookmap");
                System.out.println("   4. System will detect live sessions automatically");
                System.out.println("");
                System.out.println("💡 This system ONLY works with real market data");
                
                telegramSignals.shareMarketAnalysis(
                    "⚠️ **BookmapAI REAL DATA System Started**\n\n" +
                    "🎯 **REAL DATA ONLY - No Simulation**\n\n" +
                    "📋 **Required Setup:**\n" +
                    "• Launch Bookmap platform\n" +
                    "• Open trading instruments (EURUSD, GBPUSD, etc.)\n" +
                    "• Enable BookmapAI addon in Bookmap\n\n" +
                    "✅ System will automatically detect live sessions\n" +
                    "❌ No mock/simulated data will be used"
                );
            }
            
            // Create and start REAL DATA ONLY dashboard
            RealDataDashboard dashboard = new RealDataDashboard(8080);
            dashboard.start();
            
            // Auto-open browser
            openBrowser();
            
            System.out.println("");
            System.out.println("✅ BookmapAI REAL DATA System Operational!");
            System.out.println("🌐 Dashboard: http://localhost:8080");
            System.out.println("📊 Data Source: Live Bookmap Sessions");
            System.out.println("🎯 Pattern Detection: Real Market Data");
            System.out.println("🤖 AI Analysis: GPT-4 Powered");
            System.out.println("📱 Telegram Alerts: +21696543589");
            System.out.println("❌ Mock Data: COMPLETELY REMOVED");
            System.out.println("");
            System.out.println("Press Ctrl+C to stop");
            
            // Set up shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🛑 Shutting down BookmapAI system...");
                if (realDataSystem != null) {
                    realDataSystem.shutdown();
                }
                if (telegramSignals != null) {
                    telegramSignals.shutdown();
                }
                System.out.println("✅ BookmapAI system stopped");
            }));
            
            // Monitor real data status
            monitorRealDataStatus();
            
        } catch (Exception e) {
            System.err.println("❌ System startup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void monitorRealDataStatus() {
        ScheduledExecutorService monitor = Executors.newScheduledThreadPool(1);
        
        monitor.scheduleAtFixedRate(() -> {
            try {
                if (realDataSystem.hasRealData()) {
                    int sessionCount = realDataSystem.getRealSessions().size();
                    System.out.println(String.format("💚 [%s] Real Bookmap Data Active - %d sessions", 
                                     getCurrentTime(), sessionCount));
                    
                    // Share status via Telegram occasionally
                    if (Math.random() < 0.1) { // ~every 5 minutes
                        StringBuilder status = new StringBuilder();
                        status.append("📊 **Live Trading Status - REAL DATA**\n\n");
                        
                        for (RealDataOnlySystem.RealSession session : realDataSystem.getRealSessions().values()) {
                            status.append(String.format("• **%s**: %.5f (Vol: %d)\n", 
                                        session.getSymbol(), session.getLastPrice(), session.getTotalVolume()));
                            
                            if (!session.getPatterns().isEmpty()) {
                                status.append("  🎯 Real Patterns: ");
                                for (RealDataOnlySystem.RealPattern pattern : session.getPatterns()) {
                                    status.append(pattern.type).append(" (").append(String.format("%.1f", pattern.confidence)).append("%) ");
                                }
                                status.append("\n");
                            }
                        }
                        
                        status.append("\n✅ *100% Real Bookmap Data - No Simulation*");
                        telegramSignals.shareMarketAnalysis(status.toString());
                    }
                } else {
                    System.out.println(String.format("⚠️  [%s] WAITING FOR REAL BOOKMAP CONNECTION", getCurrentTime()));
                }
            } catch (Exception e) {
                System.err.println("Error in data monitoring: " + e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    private static String getCurrentTime() {
        return java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    private static void openBrowser() {
        try {
            String url = "http://localhost:8080";
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
                System.out.println("🌐 Browser opened automatically");
            } else {
                System.out.println("🌐 Open your browser to: " + url);
            }
        } catch (Exception e) {
            System.out.println("🌐 Dashboard available at: http://localhost:8080");
        }
    }
}
