package com.bookmaai.test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Very simple dashboard test to verify HTTP server works
 */
public class SimpleDashboardTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 Starting Simple Dashboard Test...");
        
        try {
            // Create HTTP server on port 8081
            HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
            
            // Add handler for root path
            server.createContext("/", new DashboardHandler());
            
            server.setExecutor(null);
            server.start();
            
            System.out.println("✅ Simple Dashboard started successfully!");
            System.out.println("🌐 Visit: http://localhost:8081");
            System.out.println("📊 Dashboard is working!");
            System.out.println("Press Ctrl+C to stop...");
            
            // Keep running
            Thread.sleep(Long.MAX_VALUE);
            
        } catch (Exception e) {
            System.err.println("❌ Simple dashboard test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = generateSimpleHTML();
            
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, html.length());
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(html.getBytes("UTF-8"));
            }
        }
        
        private String generateSimpleHTML() {
            return "<!DOCTYPE html>" +
                   "<html>" +
                   "<head>" +
                   "<title>BookmapAI Dashboard Test</title>" +
                   "<style>" +
                   "body { font-family: Arial, sans-serif; background: #0a0e1a; color: white; padding: 20px; }" +
                   ".header { text-align: center; padding: 20px; background: linear-gradient(135deg, #1e3c72, #2a5298); border-radius: 10px; }" +
                   ".status { background: #1a1f2e; padding: 20px; margin: 20px 0; border-radius: 10px; }" +
                   ".success { color: #4caf50; }" +
                   "</style>" +
                   "</head>" +
                   "<body>" +
                   "<div class='header'>" +
                   "<h1>🚀 BookmapAI Dashboard</h1>" +
                   "<p>Enhanced Trading Intelligence System</p>" +
                   "</div>" +
                   "<div class='status'>" +
                   "<h2 class='success'>✅ Dashboard is Working!</h2>" +
                   "<p>📊 HTTP Server: Active</p>" +
                   "<p>🌐 Port: 8081</p>" +
                   "<p>⚡ Status: Online</p>" +
                   "<p>🎯 System: Ready for Trading Intelligence</p>" +
                   "</div>" +
                   "<div class='status'>" +
                   "<h3>📈 System Features:</h3>" +
                   "<ul>" +
                   "<li>✅ Real-time Market Data Processing</li>" +
                   "<li>✅ AI-Powered Pattern Detection</li>" +
                   "<li>✅ Multi-timeframe Analysis</li>" +
                   "<li>✅ Research-based Accuracy Targets</li>" +
                   "<li>✅ Professional Trading Dashboard</li>" +
                   "</ul>" +
                   "</div>" +
                   "</body>" +
                   "</html>";
        }
    }
} 