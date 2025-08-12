package com.bookmaai.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.Socket;

/**
 * 🔍 Backend Server Verifier
 * 
 * Verifies that the dashboard server is properly running and accessible
 */
public class BackendServerVerifier {
    
    public static void main(String[] args) {
        BackendServerVerifier verifier = new BackendServerVerifier();
        verifier.runFullVerification();
    }
    
    public void runFullVerification() {
        System.out.println("🔍 ===== Backend Server Verification =====");
        System.out.println("📊 Checking BookmapAI Dashboard Server...");
        System.out.println("==========================================");
        
        // Check port availability
        boolean portAvailable = checkPortAvailability(8080);
        System.out.println("🔌 Port 8080 Available: " + (portAvailable ? "✅ YES" : "❌ NO"));
        
        // Check if server is running
        boolean serverRunning = checkServerRunning(8080);
        System.out.println("🖥️ Server Running: " + (serverRunning ? "✅ YES" : "❌ NO"));
        
        // Check HTTP response
        boolean httpWorking = checkHttpResponse();
        System.out.println("🌐 HTTP Response: " + (httpWorking ? "✅ WORKING" : "❌ FAILED"));
        
        // Check API endpoints
        checkApiEndpoints();
        
        // Overall status
        System.out.println("==========================================");
        if (serverRunning && httpWorking) {
            System.out.println("✅ BACKEND SERVER: FULLY OPERATIONAL");
            System.out.println("📊 Dashboard URL: http://localhost:8080");
            System.out.println("🌐 Ready for Microsoft Edge access");
        } else {
            System.out.println("❌ BACKEND SERVER: NEEDS ATTENTION");
            System.out.println("🔧 Recommendation: Start dashboard server");
            
            // Auto-start server
            System.out.println("🚀 Attempting to start dashboard server...");
            startDashboardServer();
        }
    }
    
    private boolean checkPortAvailability(int port) {
        try (Socket socket = new Socket("localhost", port)) {
            return true; // Port is occupied (server running)
        } catch (IOException e) {
            return false; // Port is free (no server)
        }
    }
    
    private boolean checkServerRunning(int port) {
        return checkPortAvailability(port);
    }
    
    private boolean checkHttpResponse() {
        try {
            URL url = new URL("http://localhost:8080");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            System.out.println("📡 HTTP Response Code: " + responseCode);
            
            return responseCode == 200;
        } catch (Exception e) {
            System.out.println("📡 HTTP Error: " + e.getMessage());
            return false;
        }
    }
    
    private void checkApiEndpoints() {
        System.out.println("🔍 Checking API Endpoints:");
        
        String[] endpoints = {
            "http://localhost:8080/api/status",
            "http://localhost:8080/api/data",
            "http://localhost:8080/api/real-data",
            "http://localhost:8080/api/system-status"
        };
        
        for (String endpoint : endpoints) {
            boolean working = checkEndpoint(endpoint);
            String endpointName = endpoint.substring(endpoint.lastIndexOf("/") + 1);
            System.out.println("  📍 /" + endpointName + ": " + (working ? "✅ OK" : "❌ FAIL"));
        }
    }
    
    private boolean checkEndpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            
            int responseCode = connection.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void startDashboardServer() {
        try {
            System.out.println("🚀 Starting StandaloneDashboard...");
            
            // Start the dashboard in a separate thread
            Thread serverThread = new Thread(() -> {
                try {
                    StandaloneDashboard dashboard = new StandaloneDashboard();
                    dashboard.start();
                } catch (Exception e) {
                    System.err.println("❌ Failed to start dashboard: " + e.getMessage());
                }
            });
            
            serverThread.setDaemon(true);
            serverThread.start();
            
            // Wait for server to start
            Thread.sleep(3000);
            
            // Verify server started
            if (checkServerRunning(8080)) {
                System.out.println("✅ Dashboard server started successfully!");
                System.out.println("📊 Dashboard URL: http://localhost:8080");
                
                // Open Microsoft Edge
                openMicrosoftEdge();
            } else {
                System.out.println("❌ Failed to start dashboard server");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error starting server: " + e.getMessage());
        }
    }
    
    private void openMicrosoftEdge() {
        try {
            System.out.println("🌐 Opening Microsoft Edge...");
            Runtime.getRuntime().exec("msedge http://localhost:8080");
            System.out.println("✅ Microsoft Edge launched successfully!");
        } catch (Exception e) {
            System.out.println("⚠️ Could not auto-open Microsoft Edge");
            System.out.println("🌐 Please manually open: http://localhost:8080");
        }
    }
    
    public static void verifyAndStart() {
        BackendServerVerifier verifier = new BackendServerVerifier();
        
        if (!verifier.checkServerRunning(8080)) {
            System.out.println("🚀 Starting dashboard server...");
            verifier.startDashboardServer();
        } else {
            System.out.println("✅ Dashboard server already running");
            verifier.openMicrosoftEdge();
        }
    }
}
