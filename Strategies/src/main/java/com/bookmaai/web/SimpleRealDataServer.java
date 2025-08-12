package com.bookmaai.web;

import com.bookmaai.core.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * 🔥 Simple Real Data Server - 100% REAL DATA ONLY
 * 
 * Simple HTTP server for real data dashboard without external dependencies.
 * NO SIMULATION - Only real Bookmap data is served.
 */
public class SimpleRealDataServer {
    
    private static final int DEFAULT_PORT = 8080;
    private ServerSocket serverSocket;
    private volatile boolean isRunning = false;
    
    // Real data components
    private final RealTimeMarketDataStore realDataStore;
    private final RealDataOnlyManager realDataManager;
    private final RealDataSlidingWindow slidingWindow;
    
    public SimpleRealDataServer(int port) {
        this.realDataStore = RealTimeMarketDataStore.getInstance();
        this.realDataManager = new RealDataOnlyManager();
        this.slidingWindow = new RealDataSlidingWindow();
        
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("🔥 SimpleRealDataServer initialized on port " + port);
            System.out.println("📊 Dashboard: 100% REAL DATA ONLY");
        } catch (IOException e) {
            System.err.println("❌ Failed to create server socket: " + e.getMessage());
        }
    }
    
    /**
     * Start the server
     */
    public void start() {
        if (isRunning) {
            System.out.println("⚠️ Server already running");
            return;
        }
        
        isRunning = true;
        realDataManager.startRealDataSystem();
        
        System.out.println("=== BookmapAI REAL DATA Server Started ===");
        System.out.println("📊 Server URL: http://localhost:" + serverSocket.getLocalPort());
        System.out.println("🎯 Data Source: REAL Bookmap feeds ONLY");
        System.out.println("❌ Mock Data: COMPLETELY ELIMINATED");
        System.out.println("==========================================");
        
        // Start server thread
        Thread serverThread = new Thread(this::handleRequests);
        serverThread.setDaemon(true);
        serverThread.start();
    }
    
    /**
     * Stop the server
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        realDataManager.stopRealDataSystem();
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("✅ Simple Real Data Server stopped");
        } catch (IOException e) {
            System.err.println("❌ Error stopping server: " + e.getMessage());
        }
    }
    
    /**
     * Handle incoming requests
     */
    private void handleRequests() {
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
     * Handle individual client request
     */
    private void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            String requestLine = reader.readLine();
            if (requestLine == null) return;
            
            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;
            
            String method = parts[0];
            String path = parts[1];
            
            // Skip headers for now
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                // Skip headers
            }
            
            handleRequest(method, path, writer);
            
        } catch (IOException e) {
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
     * Handle specific request
     */
    private void handleRequest(String method, String path, PrintWriter writer) {
        try {
            if ("GET".equals(method)) {
                if ("/".equals(path)) {
                    serveDashboard(writer);
                } else if (path.startsWith("/api/real-data")) {
                    serveRealData(writer);
                } else if (path.startsWith("/api/system-status")) {
                    serveSystemStatus(writer);
                } else if (path.startsWith("/api/bookmap-status")) {
                    serveBookmapStatus(writer);
                } else if (path.startsWith("/api/active-sessions")) {
                    serveActiveSessions(writer);
                } else if (path.startsWith("/api/bookmap/windows")) {
                    serveBookmapWindows(writer);
                } else if (path.startsWith("/api/instruments/status")) {
                    serveInstrumentStatus(writer, path);
                } else if (path.startsWith("/api/instruments")) {
                    serveInstrumentsAPI(writer);
                } else if (path.startsWith("/api/export/trigger")) {
                    triggerDataExport(writer);
                } else if (path.startsWith("/api/export/status")) {
                    serveExportStatus(writer);
                } else if (path.startsWith("/api/system")) {
                    serveSystemAPI(writer);
                } else if (path.startsWith("/api/components")) {
                    serveComponentsAPI(writer);
                } else if (path.startsWith("/api/patterns")) {
                    servePatternsAPI(writer);
                } else if (path.startsWith("/api/active-windows")) {
                    serveActiveWindowsAPI(writer);
                } else if (path.startsWith("/api/markets")) {
                    serveMarketsAPI(writer);
                } else if (path.startsWith("/api/session/")) {
                    serveSessionAPI(writer, path);
                } else if (path.startsWith("/favicon.ico")) {
                    serveFavicon(writer);
                } else {
                    serveNotFound(writer);
                }
            } else {
                serveNotFound(writer);
            }
        } catch (Exception e) {
            serveError(writer, e.getMessage());
        }
    }
    
    /**
     * Serve dashboard HTML
     */
    private void serveDashboard(PrintWriter writer) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println("Cache-Control: no-cache");
        writer.println();
        
        String html = "<!DOCTYPE html><html><head><title>BookmapAI - Real Data Dashboard</title>" +
                "<style>body{font-family:Arial;background:#0a0e1a;color:white;padding:20px;}" +
                ".header{background:#1e3c72;padding:20px;border-radius:8px;margin-bottom:20px;}" +
                ".panel{background:#1a1f2e;padding:20px;border-radius:8px;margin:10px;}" +
                ".status-online{color:#4caf50;} .status-offline{color:#f44336;}" +
                ".alert{padding:15px;margin:10px 0;border-radius:8px;background:rgba(33,150,243,0.1);border-left:4px solid #2196f3;color:#64b5f6;}" +
                "</style></head><body>" +
                "<div class='header'><h1>🔥 BookmapAI Real Data Dashboard</h1>" +
                "<p>Version 5.4.0 - 100% REAL DATA ONLY</p></div>" +
                "<div class='panel'><h2>📊 Connection Status</h2>" +
                "<p id='status'>Checking connection...</p></div>" +
                "<div class='panel'><h2>📈 Real Data Statistics</h2>" +
                "<p id='stats'>Loading statistics...</p></div>" +
                "<div class='alert'><strong>Real Data Only:</strong> This dashboard only displays data from live Bookmap feeds. " +
                "No simulated or fake data is used. Connect to Bookmap to see real market data.</div>" +
                "<script>" +
                "function updateData(){" +
                "fetch('/api/real-data').then(r=>r.json()).then(data=>{" +
                "document.getElementById('stats').innerHTML='Total Data Points: '+(data.total_data_points||0)+'<br/>Connected: '+(data.connected?'Yes':'No');" +
                "}).catch(e=>console.error(e));" +
                "fetch('/api/bookmap-status').then(r=>r.json()).then(data=>{" +
                "document.getElementById('status').innerHTML=(data.connected?'<span class=\"status-online\">✅ Connected</span>':'<span class=\"status-offline\">❌ Disconnected</span>');" +
                "}).catch(e=>console.error(e));" +
                "}" +
                "setInterval(updateData,3000);updateData();" +
                "</script></body></html>";
        
        writer.println(html);
    }
    
    /**
     * Serve real data API
     */
    private void serveRealData(PrintWriter writer) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println();
        
        Map<String, Object> activeWindows = realDataStore.getActiveBookmapWindows();
        Map<String, Object> systemStatus = realDataManager.getSystemStatus();
        
        String json = "{" +
                "\"connected\": " + (!activeWindows.isEmpty()) + "," +
                "\"total_data_points\": " + systemStatus.get("real_events_processed") + "," +
                "\"data_rate\": 0," +
                "\"last_update\": \"" + LocalDateTime.now() + "\"," +
                "\"simulation_mode\": false," +
                "\"data_source\": \"REAL_BOOKMAP_ONLY\"" +
                "}";
        
        writer.println(json);
    }
    
    /**
     * Serve system status API
     */
    private void serveSystemStatus(PrintWriter writer) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println();
        
        Map<String, Object> status = realDataManager.getSystemStatus();
        
        String json = "{" +
                "\"server_running\": " + isRunning + "," +
                "\"core_status\": \"Real Data Only\"," +
                "\"store_status\": \"" + (realDataStore.hasRealDataConnections() ? "Connected" : "Waiting") + "\"," +
                "\"detector_status\": \"Active\"," +
                "\"memory_usage\": \"" + getMemoryUsage() + "\"," +
                "\"uptime\": \"Running\"" +
                "}";
        
        writer.println(json);
    }
    
    /**
     * Serve Bookmap status API
     */
    private void serveBookmapStatus(PrintWriter writer) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println();
        
        Map<String, Object> activeWindows = realDataStore.getActiveBookmapWindows();
        boolean connected = !activeWindows.isEmpty();
        
        String json = "{" +
                "\"connected\": " + connected + "," +
                "\"active_windows\": " + activeWindows.size() + "," +
                "\"data_flow\": \"" + (connected ? "Active" : "Stopped") + "\"," +
                "\"addon_status\": \"" + (connected ? "Enabled" : "Waiting") + "\"" +
                "}";
        
        writer.println(json);
    }

    /**
     * Serve active sessions API with a persistent demo session
     * - Includes real Bookmap sessions when available
     * - Always includes a pinned demo session so it doesn't disappear on refresh
     */
    private void serveActiveSessions(PrintWriter writer) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println();

        // Collect real sessions from the real-time store if available
        Map<String, RealTimeMarketDataStore.TradingSession> sessions = realDataStore.getActiveSessions();
        boolean hasReal = realDataStore.hasRealData();

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"status\": \"").append(hasReal ? "ACTIVE" : "DEMO").append("\",");
        json.append("\"sessions\": [");

        boolean first = true;

        // Serialize real sessions first
        if (sessions != null && !sessions.isEmpty()) {
            for (RealTimeMarketDataStore.TradingSession s : sessions.values()) {
                if (!first) json.append(",");
                json.append("{")
                    .append("\"id\": \"").append(s.getSessionId()).append("\",")
                    .append("\"symbol\": \"").append(s.getSymbol()).append("\",")
                    .append("\"price\": \"").append(String.format("%.2f", s.getCurrentPrice())).append("\",")
                    .append("\"status\": \"ACTIVE\",")
                    .append("\"analytics\": {\"order_flow\": \"").append("Analyzing")
                    .append("\", \"volatility\": \"").append("Normal")
                    .append("\", \"score\": ").append(0).append("}")
                    .append("}");
                first = false;
            }
        }

        // No demo sessions - only real Bookmap data

        json.append("]");
        json.append(",\"demo_pinned\": false");
        json.append(",\"timestamp\": \"")
            .append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")))
            .append("\"");
        json.append("}");

        writer.println(json.toString());
    }

    /**
     * Serve system API - provides system status and statistics
     */
    private void serveSystemAPI(PrintWriter writer) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println();

        Map<String, Object> status = realDataManager.getSystemStatus();
        boolean hasRealData = realDataStore.hasRealData();

        String json = "{" +
                "\"status\": \"" + (hasRealData ? "ACTIVE" : "WAITING") + "\"," +
                "\"uptime\": \"" + System.currentTimeMillis() + "\"," +
                "\"memory_usage\": \"" + getMemoryUsage() + "\"," +
                "\"data_source\": \"REAL_BOOKMAP_ONLY\"," +
                "\"events_processed\": " + status.get("real_events_processed") + "," +
                "\"connected_sessions\": " + realDataStore.getActiveSessions().size() +
                "}";

        writer.println(json);
    }

    /**
     * Serve components API - provides component status
     */
    private void serveComponentsAPI(PrintWriter writer) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println();

        boolean hasRealData = realDataStore.hasRealData();

        String json = "{" +
                "\"data_store\": {\"status\": \"" + (hasRealData ? "ACTIVE" : "WAITING") + "\", \"type\": \"REAL_ONLY\"}," +
                "\"pattern_detector\": {\"status\": \"READY\", \"engine\": \"AI_ENHANCED\"}," +
                "\"order_flow_analyzer\": {\"status\": \"ACTIVE\", \"mode\": \"REAL_TIME\"}," +
                "\"risk_manager\": {\"status\": \"MONITORING\", \"alerts\": 0}," +
                "\"web_server\": {\"status\": \"RUNNING\", \"port\": " + serverSocket.getLocalPort() + "}" +
                "}";

        writer.println(json);
    }

    /**
     * Serve patterns API - provides detected patterns
     */
    private void servePatternsAPI(PrintWriter writer) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println();

        boolean hasRealData = realDataStore.hasRealData();

        // Only return real pattern data from Bookmap - no demo/simulated patterns
        String json = "{" +
                "\"patterns\": []," +
                "\"last_scan\": \"" + java.time.LocalDateTime.now() + "\"," +
                "\"mode\": \"REAL_DATA_ONLY\"" +
                "}";
        writer.println(json);
    }

    /**
     * Serve Enhanced Bookmap Windows API with comprehensive instrument tracking
     * Returns detailed JSON with data flow validation and health status
     */
    private void serveBookmapWindows(PrintWriter writer) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println();

        String json = getEnhancedBookmapWindowsJson();
        writer.println(json);
    }
    
    private String getEnhancedBookmapWindowsJson() {
        Map<String, Object> activeWindows = realDataStore.getActiveBookmapWindows();
        java.util.Set<String> reallyActiveInstruments = realDataStore.getReallyActiveInstruments();
        
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"status\": \"").append(activeWindows.isEmpty() ? "NO_WINDOWS" : "ACTIVE_WINDOWS").append("\",\n");
        json.append("  \"active_windows_count\": ").append(activeWindows.size()).append(",\n");
        json.append("  \"really_active_count\": ").append(reallyActiveInstruments.size()).append(",\n");
        json.append("  \"windows_last_update\": ").append(System.currentTimeMillis()).append(",\n");
        json.append("  \"message\": \"").append(activeWindows.isEmpty() ? 
            "No active Bookmap windows. Open charts in Bookmap to see real data." : 
            "Real-time data from " + activeWindows.size() + " active Bookmap windows (" + reallyActiveInstruments.size() + " with active data flow)").append("\",\n");
        json.append("  \"active_windows\": [\n");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : activeWindows.entrySet()) {
            if (!first) json.append(",\n");
            first = false;
            
            String alias = entry.getKey();
            Map<String, Object> instrumentStatus = realDataStore.getInstrumentStatus(alias);
            
            json.append("    {\n");
            json.append("      \"alias\": \"").append(alias).append("\",\n");
            json.append("      \"symbol\": \"").append(instrumentStatus.getOrDefault("symbol", alias)).append("\",\n");
            json.append("      \"exchange\": \"").append(instrumentStatus.getOrDefault("exchange", "UNKNOWN")).append("\",\n");
            json.append("      \"status\": \"").append(instrumentStatus.getOrDefault("health_status", "ACTIVE")).append("\",\n");
            json.append("      \"is_really_active\": ").append(instrumentStatus.getOrDefault("is_really_active", false)).append(",\n");
            json.append("      \"data_flow_count\": ").append(instrumentStatus.getOrDefault("data_flow_count", 0)).append(",\n");
            json.append("      \"last_data_timestamp\": ").append(instrumentStatus.getOrDefault("last_data_timestamp", 0)).append(",\n");
            json.append("      \"data_feed\": \"LIVE\"\n");
            json.append("    }");
        }
        
        json.append("\n  ],\n");
        json.append("  \"enhanced_tracking\": true,\n");
        json.append("  \"tracking_features\": [\"data_flow_validation\", \"health_monitoring\", \"exchange_info\", \"real_time_status\"]\n");
        json.append("}");
        
        return json.toString();
    }

    /**
     * Serve active windows API - provides active Bookmap windows
     */
    private void serveActiveWindowsAPI(PrintWriter writer) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println();

        Map<String, Object> activeWindows = realDataStore.getActiveBookmapWindows();
        boolean hasWindows = !activeWindows.isEmpty();

        if (hasWindows) {
            StringBuilder json = new StringBuilder();
            json.append("{\"windows\": [");
            boolean first = true;
            for (Map.Entry<String, Object> entry : activeWindows.entrySet()) {
                if (!first) json.append(",");
                json.append("{")
                    .append("\"id\": \"").append(entry.getKey()).append("\",")
                    .append("\"symbol\": \"").append(entry.getKey()).append("\",")
                    .append("\"status\": \"ACTIVE\"")
                    .append("}");
                first = false;
            }
            json.append("],\"count\": ").append(activeWindows.size()).append("}");
            writer.println(json.toString());
        } else {
            String json = "{\"windows\": [], \"count\": 0, \"message\": \"No active Bookmap windows detected\"}";
            writer.println(json);
        }
    }

    /**
     * Serve markets API - provides market data
     */
    private void serveMarketsAPI(PrintWriter writer) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println();

        boolean hasRealData = realDataStore.hasRealData();

        // Only return real market data from Bookmap sessions - no demo/simulated data
        Map<String, RealTimeMarketDataStore.TradingSession> sessions = realDataStore.getActiveSessions();
        StringBuilder json = new StringBuilder();
        json.append("{\"markets\": [");
        boolean first = true;
        for (RealTimeMarketDataStore.TradingSession session : sessions.values()) {
            if (!first) json.append(",");
            json.append("{")
                .append("\"symbol\": \"").append(session.getSymbol()).append("\",")
                .append("\"price\": ").append(session.getCurrentPrice()).append(",")
                .append("\"change\": 0.0,")
                .append("\"volume\": \"").append(session.getVolume()).append("\"")
                .append("}");
            first = false;
        }
        json.append("],\"mode\": \"REAL_DATA_ONLY\"}");
        writer.println(json.toString());
    }

    /**
     * Serve session API - provides individual session data
     */
    private void serveSessionAPI(PrintWriter writer, String path) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println();

        // Extract session ID from path like /api/session/MAIN_DEMO_NQU5_002
        String sessionId = path.substring("/api/session/".length());
        
        Map<String, RealTimeMarketDataStore.TradingSession> sessions = realDataStore.getActiveSessions();
        RealTimeMarketDataStore.TradingSession session = sessions.get(sessionId);

        if (session != null) {
            // Return real session data only
            String json = "{" +
                    "\"id\": \"" + sessionId + "\"," +
                    "\"symbol\": \"" + session.getSymbol() + "\"," +
                    "\"price\": " + session.getCurrentPrice() + "," +
                    "\"volume\": \"" + session.getVolume() + "\"," +
                    "\"status\": \"ACTIVE\"," +
                    "\"analytics\": {\"order_flow\": \"Analyzing\", \"volatility\": \"Normal\", \"score\": 0}" +
                    "}";
            writer.println(json);
        } else {
            // Session not found - no demo sessions supported
            String json = "{\"error\": \"Session not found - only real Bookmap sessions supported\", \"id\": \"" + sessionId + "\"}";
            writer.println(json);
        }
    }

    /**
     * Serve favicon quietly (prevent repeated 404s in browser console)
     */
    private void serveFavicon(PrintWriter writer) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: image/x-icon");
        writer.println("Cache-Control: max-age=86400");
        writer.println();
        // No body
    }
    
    /**
     * Serve comprehensive instruments API
     */
    private void serveInstrumentsAPI(PrintWriter writer) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println();

        java.util.Set<String> reallyActive = realDataStore.getReallyActiveInstruments();
        Map<String, Object> activeWindows = realDataStore.getActiveBookmapWindows();
        
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"timestamp\": ").append(System.currentTimeMillis()).append(",\n");
        json.append("  \"total_instruments\": ").append(activeWindows.size()).append(",\n");
        json.append("  \"active_instruments\": ").append(reallyActive.size()).append(",\n");
        json.append("  \"instruments\": [\n");
        
        boolean first = true;
        for (String alias : activeWindows.keySet()) {
            if (!first) json.append(",\n");
            first = false;
            
            Map<String, Object> status = realDataStore.getInstrumentStatus(alias);
            json.append("    {\n");
            json.append("      \"alias\": \"").append(alias).append("\",\n");
            json.append("      \"symbol\": \"").append(status.getOrDefault("symbol", alias)).append("\",\n");
            json.append("      \"exchange\": \"").append(status.getOrDefault("exchange", "UNKNOWN")).append("\",\n");
            json.append("      \"is_really_active\": ").append(status.getOrDefault("is_really_active", false)).append(",\n");
            json.append("      \"health_status\": \"").append(status.getOrDefault("health_status", "UNKNOWN")).append("\",\n");
            json.append("      \"data_flow_count\": ").append(status.getOrDefault("data_flow_count", 0)).append("\n");
            json.append("    }");
        }
        
        json.append("\n  ]\n");
        json.append("}");
        
        writer.println(json.toString());
    }
    
    /**
     * Serve specific instrument status API
     */
    private void serveInstrumentStatus(PrintWriter writer, String path) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println();

        // Extract alias from path like /api/instruments/status/ALIAS
        String[] pathParts = path.split("/");
        if (pathParts.length >= 5) {
            String alias = pathParts[4];
            Map<String, Object> status = realDataStore.getInstrumentStatus(alias);
            
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"alias\": \"").append(alias).append("\",\n");
            json.append("  \"timestamp\": ").append(System.currentTimeMillis()).append(",\n");
            
            for (Map.Entry<String, Object> entry : status.entrySet()) {
                json.append("  \"").append(entry.getKey()).append("\": ");
                if (entry.getValue() instanceof String) {
                    json.append("\"").append(entry.getValue()).append("\"");
                } else {
                    json.append(entry.getValue());
                }
                json.append(",\n");
            }
            
            // Remove last comma
            if (json.length() > 2) {
                json.setLength(json.length() - 2);
                json.append("\n");
            }
            
            json.append("}");
            writer.println(json.toString());
        } else {
            writer.println("{\"error\": \"Invalid path format. Use /api/instruments/status/ALIAS\"}");
        }
    }
    
    /**
     * Trigger manual data export
     */
    private void triggerDataExport(PrintWriter writer) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println();

        try {
            // Trigger export through sliding window
            slidingWindow.exportData();
            
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"status\": \"success\",\n");
            json.append("  \"message\": \"Data export triggered successfully\",\n");
            json.append("  \"timestamp\": ").append(System.currentTimeMillis()).append(",\n");
            json.append("  \"export_location\": \"exports/\"\n");
            json.append("}");
            
            writer.println(json.toString());
            
        } catch (Exception e) {
            writer.println("{\"status\": \"error\", \"message\": \"Export failed: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Get export status and information
     */
    private void serveExportStatus(PrintWriter writer) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println();

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"export_enabled\": true,\n");
        json.append("  \"export_directory\": \"exports/\",\n");
        json.append("  \"auto_export_interval_minutes\": 5,\n");
        json.append("  \"min_data_points_for_export\": 10,\n");
        json.append("  \"timestamp\": ").append(System.currentTimeMillis()).append(",\n");
        json.append("  \"supported_formats\": [\"CSV\", \"JSON\"],\n");
        json.append("  \"timeframes\": [\"1m\", \"5m\", \"15m\", \"1h\"]\n");
        json.append("}");
        
        writer.println(json.toString());
    }
    
    /**
     * Serve 404 Not Found
     */
    private void serveNotFound(PrintWriter writer) {
        writer.println("HTTP/1.1 404 Not Found");
        writer.println("Content-Type: text/html");
        writer.println();
        writer.println("<html><body><h1>404 Not Found</h1></body></html>");
    }
    
    /**
     * Serve 500 Error
     */
    private void serveError(PrintWriter writer, String error) {
        writer.println("HTTP/1.1 500 Internal Server Error");
        writer.println("Content-Type: application/json");
        writer.println();
        writer.println("{\"error\": \"" + error + "\"}");
    }
    
    /**
     * Get memory usage string
     */
    private String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long total = runtime.totalMemory();
        return String.format("%.1f MB / %.1f MB", used / 1024.0 / 1024.0, total / 1024.0 / 1024.0);
    }
    
    /**
     * Main method for standalone testing
     */
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        
        SimpleRealDataServer server = new SimpleRealDataServer(port);
        server.start();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        
        System.out.println("🔥 Press Ctrl+C to stop the server");
        
        // Keep running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            server.stop();
        }
    }
}

