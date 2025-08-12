package com.bookmaai.test;

import com.bookmaai.core.AccuracyDashboardManager;
import com.bookmaai.web.SimpleDashboard;
import java.util.Random;

/**
 * Real-time Dashboard Launcher - Tests 1-second refresh and colorization
 */
public class RealTimeDashboardLauncher {
    
    private static AccuracyDashboardManager dashboardManager;
    private static Random random = new Random();
    
    public static void main(String[] args) {
        System.out.println("Starting Real-Time BookmapAI Dashboard...");
        System.out.println("Features: 1-second refresh, value colorization, change tracking");
        
        try {
            // Initialize dashboard manager
            dashboardManager = new AccuracyDashboardManager();
            dashboardManager.initialize();
            
            // Populate with demo data
            populateDemoData();
            
            // Start web server on port 8080
            SimpleDashboard dashboard = new SimpleDashboard(8080, dashboardManager);
            dashboard.start();
            
            System.out.println("Dashboard launched successfully!");
            System.out.println("Access at: http://localhost:8080");
            System.out.println("REAL-TIME FEATURES ACTIVE:");
            System.out.println("- 1-second data refresh");
            System.out.println("- Green animation for increased values");
            System.out.println("- Red animation for decreased values");
            System.out.println("- Blue animation for updated values");
            System.out.println("- Orange animation for new data");
            System.out.println();
            System.out.println("Press Ctrl+C to stop...");
            
            // Setup shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down dashboard...");
                dashboard.stop();
                System.out.println("Dashboard stopped.");
            }));
            
            // Keep running and update data every 5 seconds
            int updateCounter = 0;
            while (dashboard.isRunning()) {
                Thread.sleep(5000); // Update demo data every 5 seconds
                
                // Update demo data for real-time simulation
                updateDemoData();
                updateCounter++;
                
                // Show periodic status
                if (updateCounter % 12 == 0) { // Every minute
                    System.out.println("Real-time dashboard running... Updates: " + updateCounter);
                    System.out.println("Data refreshing every 1 second with colorized changes");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Dashboard launcher failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void populateDemoData() {
        System.out.println("Populating dashboard with demo data...");
        
        // Add demo patterns
        String[] timeframes = {"1M", "5M", "15M", "1H", "4H", "1D"};
        
        dashboardManager.recordPatternAccuracy("Perfect_Storm_NQ", true, 94.5, timeframes[2]);
        dashboardManager.recordPatternAccuracy("Reversal_Pattern", true, 87.2, timeframes[1]);
        dashboardManager.recordPatternAccuracy("Iceberg_Pattern", true, 91.8, timeframes[3]);
        dashboardManager.recordPatternAccuracy("Absorption_Pattern", false, 85.6, timeframes[0]);
        dashboardManager.recordPatternAccuracy("Momentum_Pattern", true, 89.3, timeframes[4]);
        dashboardManager.recordPatternAccuracy("Breakout_Pattern", true, 82.7, timeframes[2]);
        
        // Add demo component metrics
        dashboardManager.recordComponentAccuracy("OrderFlowAnalyzer", true, 92.1, "High accuracy order flow detection");
        dashboardManager.recordComponentAccuracy("VolumeImbalanceCalculator", true, 81.4, "Strong volume imbalance signals");
        dashboardManager.recordComponentAccuracy("CumulativeDeltaEngine", true, 78.9, "Reliable cumulative delta analysis");
        dashboardManager.recordComponentAccuracy("AdvancedPatternEngine", true, 87.6, "Advanced pattern recognition");
        dashboardManager.recordComponentAccuracy("AdaptiveLearningSystem", true, 84.2, "Machine learning adaptation");
        dashboardManager.recordComponentAccuracy("RiskRewardCalculator", true, 96.3, "Precise risk calculation");
        
        // Add more demo signals
        for (int i = 0; i < 30; i++) {
            boolean success = random.nextDouble() > 0.13; // 87% success rate
            String component = i % 2 == 0 ? "OrderFlowAnalyzer" : "AdvancedPatternEngine";
            double confidence = 75 + random.nextDouble() * 20; // 75-95% confidence
            dashboardManager.recordComponentAccuracy(component, success, confidence, "Demo signal " + i);
        }
        
        System.out.println("Demo data populated successfully!");
    }
    
    private static void updateDemoData() {
        // Simulate new trading activity for real-time testing
        String[] patterns = {"Perfect_Storm_NQ", "Reversal_Pattern", "Iceberg_Pattern", "Absorption_Pattern", 
                           "Momentum_Pattern", "Breakout_Pattern"};
        String[] timeframes = {"1M", "5M", "15M", "1H", "4H", "1D"};
        
        // Add 1-2 new patterns every update to trigger colorization
        int newPatterns = 1 + random.nextInt(2);
        for (int i = 0; i < newPatterns; i++) {
            String pattern = patterns[random.nextInt(patterns.length)];
            String timeframe = timeframes[random.nextInt(timeframes.length)];
            double confidence = 75 + random.nextDouble() * 20; // 75-95% confidence
            boolean success = random.nextDouble() > 0.15; // 85% success rate
            
            dashboardManager.recordPatternAccuracy(pattern, success, confidence, timeframe);
        }
        
        // Update component accuracies to trigger value changes and colorization
        String[] components = {"OrderFlowAnalyzer", "VolumeImbalanceCalculator", "CumulativeDeltaEngine", 
                             "AdvancedPatternEngine", "AdaptiveLearningSystem", "RiskRewardCalculator"};
        
        for (String component : components) {
            // Add new signals for each component to create data changes
            double confidence = 80 + random.nextDouble() * 15; // 80-95% confidence
            boolean success = random.nextDouble() > 0.13; // 87% overall success
            String details = "Live signal at " + System.currentTimeMillis();
            
            dashboardManager.recordComponentAccuracy(component, success, confidence, details);
        }
        
        System.out.println("Demo data updated - should trigger colorized changes in dashboard");
    }
} 