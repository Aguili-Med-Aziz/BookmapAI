package com.bookmaai.launcher;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Simple launcher that serves ONLY the original-dashboard.html
 * Lightweight and focused - just launches the dashboard on port 8080
 */
public class OriginalDashboardLauncher {
    
    private static final int PORT = 8080;
    private ServerSocket serverSocket;
    private volatile boolean isRunning = false;
    private String dashboardHTML;
    
    public OriginalDashboardLauncher() {
        loadOriginalDashboard();
    }
    
    /**
     * Load the original dashboard HTML
     */
    private void loadOriginalDashboard() {
        try {
            // Try to load from resources first
            InputStream htmlStream = getClass().getResourceAsStream("/static/original-dashboard.html");
            if (htmlStream != null) {
                dashboardHTML = readInputStream(htmlStream);
                System.out.println("✅ Original dashboard loaded from JAR resources");
                return;
            }
            
            // Try to load from file system (for development)
            if (Files.exists(Paths.get("bin/main/original-dashboard.html"))) {
                dashboardHTML = new String(Files.readAllBytes(Paths.get("bin/main/original-dashboard.html")), StandardCharsets.UTF_8);
                System.out.println("✅ Original dashboard loaded from file system");
                return;
            }
            
            // Fallback - create basic dashboard
            dashboardHTML = createBasicDashboard();
            System.out.println("⚠️ Using basic dashboard - original-dashboard.html not found");
            
        } catch (Exception e) {
            System.err.println("❌ Error loading original dashboard: " + e.getMessage());
            dashboardHTML = createBasicDashboard();
        }
    }
    
    /**
     * Read InputStream to String
     */
    private String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    /**
     * Create basic dashboard if original not found
     */
    private String createBasicDashboard() {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"en\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <title>BookmapAI Original Dashboard</title>\n" +
               "    <style>\n" +
               "        body { font-family: Arial, sans-serif; background: #0a0e1a; color: white; margin: 0; padding: 20px; text-align: center; }\n" +
               "        .header { background: linear-gradient(135deg, #1e3c72, #2a5298); padding: 30px; border-radius: 15px; margin-bottom: 30px; }\n" +
               "        .status { background: #1a1f2e; border-radius: 10px; padding: 20px; margin: 20px auto; max-width: 600px; }\n" +
               "        .success { color: #4caf50; }\n" +
               "        .info { color: #2196f3; }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class=\"header\">\n" +
               "        <h1>📊 BookmapAI Original Dashboard</h1>\n" +
               "        <p class=\"success\">✅ Dashboard Server Running Successfully!</p>\n" +
               "    </div>\n" +
               "    <div class=\"status\">\n" +
               "        <h3 class=\"info\">🚀 Server Status</h3>\n" +
               "        <p><strong>Port:</strong> " + PORT + "</p>\n" +
               "        <p><strong>Status:</strong> <span class=\"success\">Running</span></p>\n" +
               "        <p><strong>Dashboard:</strong> Original HTML Ready</p>\n" +
               "        <p><strong>Data Source:</strong> Real Bookmap feeds only</p>\n" +
               "    </div>\n" +
               "    <div class=\"status\">\n" +
               "        <h3>📋 Instructions</h3>\n" +
               "        <p>The original dashboard HTML should be loaded above.</p>\n" +
               "        <p>If you see this message, the original-dashboard.html file was not found in the JAR.</p>\n" +
               "        <p>Please ensure the JAR was built correctly with the original dashboard included.</p>\n" +
               "    </div>\n" +
               "</body>\n" +
               "</html>";
    }
    
    /**
     * Start the server
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            
            System.out.println("=== Original Dashboard Launcher ===");
            System.out.println("📊 Dashboard URL: http://localhost:" + PORT);
            System.out.println("🎯 Serving: original-dashboard.html");
            System.out.println("==================================");
            
            // Handle requests
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("❌ Error accepting client: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("❌ Failed to start server on port " + PORT + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle client request
     */
    private void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream())) {
            
            String requestLine = reader.readLine();
            if (requestLine != null) {
                System.out.println("📡 Request: " + requestLine);
                
                // Serve the dashboard for any request
                writer.println("HTTP/1.1 200 OK");
                writer.println("Content-Type: text/html; charset=UTF-8");
                writer.println("Cache-Control: no-cache");
                writer.println("Connection: close");
                writer.println();
                writer.println(dashboardHTML);
                writer.flush();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    /**
     * Stop the server
     */
    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("✅ Dashboard server stopped");
        } catch (IOException e) {
            System.err.println("❌ Error stopping server: " + e.getMessage());
        }
    }
    
    /**
     * Main method - Entry point for JAR
     */
    public static void main(String[] args) {
        System.out.println("🚀 Starting Original Dashboard Launcher...");
        
        OriginalDashboardLauncher launcher = new OriginalDashboardLauncher();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(launcher::stop));
        
        // Start server
        launcher.start();
    }
}








