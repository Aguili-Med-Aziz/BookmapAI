package com.bookmaai.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Dashboard Port Manager
 * 
 * Ensures only one BookmapAI dashboard instance runs across all entry points.
 * Handles port conflicts, automatic port discovery, and singleton management.
 */
public class DashboardPortManager {
    
    private static final int DEFAULT_PORT = 8080;
    private static final int MAX_PORT_SCAN = 8100;
    private static final String SINGLETON_KEY = "BookmapAI_Dashboard_Instance";
    
    // Singleton management
    private static final AtomicBoolean isDashboardRunning = new AtomicBoolean(false);
    private static final AtomicInteger activeDashboardPort = new AtomicInteger(-1);
    private static final ReentrantLock dashboardLock = new ReentrantLock();
    private static volatile Object runningDashboardInstance = null;
    
    // Logger
    private static final BookmapAILogger logger = BookmapAILogger.getLogger("PortManager");
    
    /**
     * Check if a dashboard is already running
     */
    public static boolean isDashboardRunning() {
        boolean running = isDashboardRunning.get();
        logger.debug("Dashboard running check: " + running + " on port " + activeDashboardPort.get());
        return running;
    }
    
    /**
     * Get the port of the currently running dashboard
     */
    public static int getActiveDashboardPort() {
        int port = activeDashboardPort.get();
        logger.debug("Active dashboard port: " + port);
        return port;
    }
    
    /**
     * Register a dashboard instance as running
     */
    public static boolean registerDashboard(Object dashboardInstance, int port) {
        dashboardLock.lock();
        try {
            if (isDashboardRunning.get()) {
                logger.warn("Dashboard already running on port " + activeDashboardPort.get() + 
                           ". Rejecting new instance on port " + port);
                return false;
            }
            
            isDashboardRunning.set(true);
            activeDashboardPort.set(port);
            runningDashboardInstance = dashboardInstance;
            
            logger.success("Dashboard registered successfully on port " + port);
            logger.info("Dashboard instance: " + dashboardInstance.getClass().getSimpleName());
            
            return true;
            
        } finally {
            dashboardLock.unlock();
        }
    }
    
    /**
     * Unregister the dashboard instance
     */
    public static void unregisterDashboard(Object dashboardInstance) {
        dashboardLock.lock();
        try {
            if (runningDashboardInstance == dashboardInstance) {
                isDashboardRunning.set(false);
                int port = activeDashboardPort.get();
                activeDashboardPort.set(-1);
                runningDashboardInstance = null;
                
                logger.info("Dashboard unregistered from port " + port);
                logger.success("Port " + port + " is now available for new instances");
            } else {
                logger.warn("Attempted to unregister unknown dashboard instance");
            }
        } finally {
            dashboardLock.unlock();
        }
    }
    
    /**
     * Find the next available port starting from the default port
     */
    public static int findAvailablePort() {
        return findAvailablePort(DEFAULT_PORT);
    }
    
    /**
     * Find the next available port starting from the specified port
     */
    public static int findAvailablePort(int startPort) {
        logger.info("Scanning for available port starting from " + startPort);
        
        for (int port = startPort; port <= MAX_PORT_SCAN; port++) {
            if (isPortAvailable(port)) {
                logger.success("Found available port: " + port);
                return port;
            } else {
                logger.debug("Port " + port + " is not available");
            }
        }
        
        logger.error("No available ports found in range " + startPort + "-" + MAX_PORT_SCAN);
        return -1;
    }
    
    /**
     * Check if a specific port is available
     */
    public static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            logger.debug("Port " + port + " is available");
            return true;
        } catch (IOException e) {
            logger.debug("Port " + port + " is in use: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get the recommended port for a new dashboard instance
     * Returns existing port if dashboard is running, or finds new available port
     */
    public static int getRecommendedPort() {
        if (isDashboardRunning()) {
            int existingPort = activeDashboardPort.get();
            logger.info("Dashboard already running on port " + existingPort + ". Recommending same port.");
            return existingPort;
        }
        
        // Check if default port is available
        if (isPortAvailable(DEFAULT_PORT)) {
            logger.info("Default port " + DEFAULT_PORT + " is available");
            return DEFAULT_PORT;
        }
        
        // Find alternative port
        int availablePort = findAvailablePort(DEFAULT_PORT + 1);
        if (availablePort != -1) {
            logger.info("Recommending alternative port: " + availablePort);
            return availablePort;
        }
        
        logger.error("No ports available for dashboard");
        return -1;
    }
    
    /**
     * Get dashboard URL for the currently running instance
     */
    public static String getDashboardURL() {
        if (isDashboardRunning()) {
            int port = activeDashboardPort.get();
            String url = "http://localhost:" + port;
            logger.debug("Dashboard URL: " + url);
            return url;
        }
        
        logger.warn("No dashboard is currently running");
        return null;
    }
    
    /**
     * Get status report of dashboard management
     */
    public static String getStatusReport() {
        StringBuilder report = new StringBuilder();
        report.append("📊 Dashboard Port Manager Status:\n");
        report.append("   Dashboard Running: ").append(isDashboardRunning() ? "✅ YES" : "❌ NO").append("\n");
        
        if (isDashboardRunning()) {
            report.append("   Active Port: ").append(activeDashboardPort.get()).append("\n");
            report.append("   Dashboard URL: ").append(getDashboardURL()).append("\n");
            report.append("   Instance: ").append(runningDashboardInstance.getClass().getSimpleName()).append("\n");
        } else {
            report.append("   Default Port Available: ").append(isPortAvailable(DEFAULT_PORT) ? "✅ YES" : "❌ NO").append("\n");
            report.append("   Recommended Port: ").append(getRecommendedPort()).append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * Force shutdown of any running dashboard (emergency use only)
     */
    public static void forceShutdown() {
        dashboardLock.lock();
        try {
            if (isDashboardRunning()) {
                logger.warn("Force shutdown requested for dashboard on port " + activeDashboardPort.get());
                
                // Try to gracefully stop the dashboard if it has a stop method
                if (runningDashboardInstance != null) {
                    try {
                        // Use reflection to call stop() method if available
                        runningDashboardInstance.getClass().getMethod("stop").invoke(runningDashboardInstance);
                        logger.info("Dashboard stopped gracefully via stop() method");
                    } catch (Exception e) {
                        logger.warn("Could not call stop() method on dashboard instance: " + e.getMessage());
                    }
                }
                
                // Clear the registration
                isDashboardRunning.set(false);
                activeDashboardPort.set(-1);
                runningDashboardInstance = null;
                
                logger.success("Force shutdown completed");
            } else {
                logger.info("No dashboard running, nothing to shutdown");
            }
        } finally {
            dashboardLock.unlock();
        }
    }
    
    /**
     * Check for port conflicts and log details
     */
    public static void diagnosePortConflicts() {
        logger.info("🔍 Diagnosing port conflicts...");
        
        // Check default port
        boolean defaultAvailable = isPortAvailable(DEFAULT_PORT);
        logger.info("Port " + DEFAULT_PORT + " (default): " + (defaultAvailable ? "✅ Available" : "❌ In Use"));
        
        // Check next few ports
        for (int port = DEFAULT_PORT + 1; port <= DEFAULT_PORT + 5; port++) {
            boolean available = isPortAvailable(port);
            logger.info("Port " + port + ": " + (available ? "✅ Available" : "❌ In Use"));
        }
        
        // System information
        logger.info("📊 System Information:");
        logger.info("   Current dashboard instances: " + (isDashboardRunning() ? "1" : "0"));
        logger.info("   JVM process: " + System.getProperty("java.vm.name"));
        logger.info("   Operating system: " + System.getProperty("os.name"));
        
        logger.success("Port conflict diagnosis completed");
    }
} 