package com.bookmaai.test;

import com.bookmaai.core.AccuracyDashboardManager;
import com.bookmaai.web.SimpleDashboard;

/**
 * Simple test to verify the dashboard works
 */
public class DashboardTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 Testing SimpleDashboard...");
        
        try {
            // Initialize dashboard manager
            AccuracyDashboardManager dashboardManager = new AccuracyDashboardManager();
            dashboardManager.initialize();
            
            // Create and start dashboard
            SimpleDashboard dashboard = new SimpleDashboard(8080, dashboardManager);
            dashboard.start();
            
            System.out.println("✅ Dashboard started successfully!");
            System.out.println("🌐 Visit: http://localhost:8080");
            System.out.println("Press Ctrl+C to stop...");
            
            // Keep running
            Thread.sleep(Long.MAX_VALUE);
            
        } catch (Exception e) {
            System.err.println("❌ Dashboard test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 