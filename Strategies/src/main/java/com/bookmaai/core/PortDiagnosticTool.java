package com.bookmaai.core;

/**
 * Port Diagnostic Tool
 * 
 * Simple utility to check port availability and diagnose conflicts.
 * Can be run standalone or integrated into BookmapAI.
 */
public class PortDiagnosticTool {
    
    private static final BookmapAILogger logger = BookmapAILogger.getLogger("PortDiagnostic");
    
    public static void main(String[] args) {
        System.out.println("🔍 BookmapAI Port Diagnostic Tool");
        System.out.println("==================================");
        
        try {
            // Run comprehensive port diagnosis
            runFullDiagnosis();
            
            // Show dashboard manager status
            showDashboardStatus();
            
            // Show recommendations
            showRecommendations();
            
        } catch (Exception e) {
            System.err.println("❌ Diagnostic failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n✅ Diagnostic completed. Check logs for details.");
    }
    
    private static void runFullDiagnosis() {
        logger.info("🔍 Starting comprehensive port diagnosis...");
        
        // Check default port
        boolean port8080Available = DashboardPortManager.isPortAvailable(8080);
        System.out.println("Port 8080 (default): " + (port8080Available ? "✅ Available" : "❌ In Use"));
        
        if (!port8080Available) {
            System.out.println("⚠️  Port 8080 is currently in use by another application");
        }
        
        // Check alternative ports
        System.out.println("\n📊 Alternative Ports:");
        for (int port = 8081; port <= 8090; port++) {
            boolean available = DashboardPortManager.isPortAvailable(port);
            System.out.println("Port " + port + ": " + (available ? "✅ Available" : "❌ In Use"));
        }
        
        // Find recommended port
        int recommendedPort = DashboardPortManager.getRecommendedPort();
        if (recommendedPort != -1) {
            System.out.println("\n🎯 Recommended Port: " + recommendedPort);
        } else {
            System.out.println("\n❌ No available ports found in range 8080-8100");
        }
        
        // Run detailed diagnosis
        DashboardPortManager.diagnosePortConflicts();
    }
    
    private static void showDashboardStatus() {
        System.out.println("\n📊 Dashboard Status:");
        System.out.println("===================");
        
        boolean dashboardRunning = DashboardPortManager.isDashboardRunning();
        System.out.println("Dashboard Running: " + (dashboardRunning ? "✅ YES" : "❌ NO"));
        
        if (dashboardRunning) {
            int activePort = DashboardPortManager.getActiveDashboardPort();
            String dashboardURL = DashboardPortManager.getDashboardURL();
            
            System.out.println("Active Port: " + activePort);
            System.out.println("Dashboard URL: " + dashboardURL);
        }
        
        // Show full status report
        System.out.println("\nDetailed Status:");
        System.out.println(DashboardPortManager.getStatusReport());
    }
    
    private static void showRecommendations() {
        System.out.println("\n💡 Recommendations:");
        System.out.println("===================");
        
        boolean port8080Available = DashboardPortManager.isPortAvailable(8080);
        boolean dashboardRunning = DashboardPortManager.isDashboardRunning();
        
        if (dashboardRunning) {
            String url = DashboardPortManager.getDashboardURL();
            System.out.println("✅ Dashboard is already running at: " + url);
            System.out.println("   No action needed. Access the dashboard in your browser.");
        } else if (port8080Available) {
            System.out.println("✅ Port 8080 is available for BookmapAI dashboard");
            System.out.println("   You can start the BookmapAI addon in Bookmap.");
        } else {
            int recommendedPort = DashboardPortManager.getRecommendedPort();
            if (recommendedPort != -1) {
                System.out.println("⚠️  Port 8080 is busy, but port " + recommendedPort + " is available");
                System.out.println("   BookmapAI will automatically use port " + recommendedPort);
                System.out.println("   Dashboard will be at: http://localhost:" + recommendedPort);
            } else {
                System.out.println("❌ No available ports found");
                System.out.println("   Please close other applications using ports 8080-8100");
            }
        }
        
        System.out.println("\n📁 Log Files:");
        System.out.println("   All activities are logged to: C:\\Bookmap\\Logs\\");
        System.out.println("   Check the logs for detailed troubleshooting information.");
    }
    
    /**
     * Quick port check utility method
     */
    public static boolean isPort8080Available() {
        return DashboardPortManager.isPortAvailable(8080);
    }
    
    /**
     * Get next available port for dashboard
     */
    public static int getNextAvailablePort() {
        return DashboardPortManager.findAvailablePort();
    }
    
    /**
     * Force close any running dashboards (emergency use)
     */
    public static void forceCloseDashboards() {
        System.out.println("🚨 Force closing any running dashboards...");
        DashboardPortManager.forceShutdown();
        System.out.println("✅ All dashboards forcefully closed");
    }
} 