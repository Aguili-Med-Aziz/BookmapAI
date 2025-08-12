package com.bookmaai.web;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * Simple HTTP server to serve the original dashboard HTML
 * This server runs when the Bookmap addon is loaded
 */
public class DashboardServer {
    
    private static final int DEFAULT_PORT = 8080;
    private ServerSocket serverSocket;
    private volatile boolean isRunning = false;
    private String dashboardHTML;
    
    public DashboardServer() {
        loadDashboardHTML();
    }
    
    public DashboardServer(int port) {
        this();
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("📊 BookmapAI Dashboard Server initialized on port " + port);
        } catch (IOException e) {
            System.err.println("❌ Failed to create server socket: " + e.getMessage());
        }
    }
    
    /**
     * Load the original dashboard HTML from resources
     */
    private void loadDashboardHTML() {
        try {
            // Try to load from static resources
            InputStream htmlStream = getClass().getResourceAsStream("/static/original-dashboard.html");
            if (htmlStream != null) {
                dashboardHTML = readInputStream(htmlStream);
                System.out.println("✅ Original dashboard HTML loaded from resources");
            } else {
                // Fallback to minimal dashboard
                dashboardHTML = createMinimalDashboard();
                System.out.println("⚠️ Using minimal dashboard - original HTML not found");
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading dashboard HTML: " + e.getMessage());
            dashboardHTML = createMinimalDashboard();
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
     * Create minimal dashboard if original not found
     */
    private String createMinimalDashboard() {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"en\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <title>BookmapAI Dashboard</title>\n" +
               "    <style>\n" +
               "        body { font-family: Arial, sans-serif; background: #0a0e1a; color: white; margin: 0; padding: 20px; }\n" +
               "        .header { background: linear-gradient(135deg, #1e3c72, #2a5298); padding: 20px; border-radius: 10px; text-align: center; }\n" +
               "        .panel { background: #1a1f2e; border-radius: 10px; padding: 20px; margin: 20px 0; }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class=\"header\">\n" +
               "        <h1>📊 BookmapAI Dashboard</h1>\n" +
               "        <p>Dashboard server is running successfully!</p>\n" +
               "    </div>\n" +
               "    <div class=\"panel\">\n" +
               "        <h3>🔗 Status</h3>\n" +
               "        <p><strong>Server:</strong> Running on port " + DEFAULT_PORT + "</p>\n" +
               "        <p><strong>Bookmap Integration:</strong> Active</p>\n" +
               "        <p><strong>Data Source:</strong> Real Bookmap feeds only</p>\n" +
               "    </div>\n" +
               "</body>\n" +
               "</html>";
    }
    
    /**
     * Start the dashboard server
     */
    public void start() {
        if (isRunning) {
            System.out.println("⚠️ Dashboard server already running");
            return;
        }
        
        isRunning = true;
        
        System.out.println("=== BookmapAI Dashboard Server Started ===");
        System.out.println("📊 Dashboard URL: http://localhost:" + (serverSocket != null ? serverSocket.getLocalPort() : DEFAULT_PORT));
        System.out.println("🎯 Serving: Original Dashboard HTML");
        System.out.println("==========================================");
        
        // Start server thread
        Thread serverThread = new Thread(this::handleRequests);
        serverThread.setDaemon(true);
        serverThread.start();
    }
    
    /**
     * Stop the dashboard server
     */
    public void stop() {
        if (!isRunning) return;
        
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
     * Handle incoming HTTP requests
     */
    private void handleRequests() {
        if (serverSocket == null) {
            System.err.println("❌ Server socket not initialized");
            return;
        }
        
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.setDaemon(true);
                clientThread.start();
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("❌ Error accepting client: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Handle individual client requests
     */
    private void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream())) {
            
            String requestLine = reader.readLine();
            if (requestLine == null) return;
            
            // Parse the request
            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;
            
            String method = parts[0];
            String path = parts[1];
            
            // Serve the dashboard for any GET request
            if ("GET".equals(method)) {
                serveDashboard(writer);
            } else {
                serve404(writer);
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
     * Serve the original dashboard HTML
     */
    private void serveDashboard(PrintWriter writer) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println("Cache-Control: no-cache");
        writer.println("Connection: close");
        writer.println();
        writer.println(dashboardHTML);
        writer.flush();
    }
    
    /**
     * Serve 404 error
     */
    private void serve404(PrintWriter writer) {
        writer.println("HTTP/1.1 404 Not Found");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println("Connection: close");
        writer.println();
        writer.println("<html><body><h1>404 - Not Found</h1><p>BookmapAI Dashboard Server</p></body></html>");
        writer.flush();
    }
    
    /**
     * Main method for standalone testing
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default: " + DEFAULT_PORT);
            }
        }
        
        DashboardServer server = new DashboardServer(port);
        server.start();
        
        // Keep running
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            server.stop();
        }
    }
}





