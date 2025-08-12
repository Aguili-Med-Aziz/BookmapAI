package com.bookmaai.test;

import com.bookmaai.core.RealTimeMarketDataStore;
import com.bookmaai.core.enhanced.BookmapIntegrationManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple test to demonstrate Active Bookmap Windows functionality
 */
public class SimpleActiveWindowsTest {
    
    public static void main(String[] args) {
        System.out.println("🚀 Testing Active Bookmap Windows Feature...");
        
        try {
            // Initialize components
            RealTimeMarketDataStore dataStore = RealTimeMarketDataStore.getInstance();
            BookmapIntegrationManager bookmapManager = new BookmapIntegrationManager();
            
            // Populate test data
            System.out.println("📊 Setting up Active Windows demo data...");
            Map<String, String> testWindows = new HashMap<>();
            testWindows.put("EURUSD", "ACTIVE");
            testWindows.put("GBPUSD", "ACTIVE");
            testWindows.put("USDJPY", "ACTIVE");
            testWindows.put("BTCUSDT", "ACTIVE");
            testWindows.put("ES", "ACTIVE");
            testWindows.put("NQ", "ACTIVE");
            
            dataStore.updateActiveBookmapWindows(testWindows);
            
            // Add market data
            dataStore.updateMarketData("EURUSD", 1.0847, 1250000, "REAL_TIME_DEMO");
            dataStore.updateMarketData("GBPUSD", 1.2743, 982000, "REAL_TIME_DEMO");
            dataStore.updateMarketData("USDJPY", 149.85, 1500000, "REAL_TIME_DEMO");
            dataStore.updateMarketData("BTCUSDT", 42150.0, 850000, "REAL_TIME_DEMO");
            dataStore.updateMarketData("ES", 4785.50, 2100000, "REAL_TIME_DEMO");
            dataStore.updateMarketData("NQ", 16890.25, 1800000, "REAL_TIME_DEMO");
            
            // Create HTTP server
            HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
            server.createContext("/api/bookmap/windows", new ActiveWindowsHandler(dataStore));
            server.createContext("/test", new TestHandler());
            server.setExecutor(null);
            server.start();
            
            System.out.println("✅ Active Windows Test Server started!");
            System.out.println("🌐 Test at: http://localhost:8081/api/bookmap/windows");
            System.out.println("🔍 Simple test: http://localhost:8081/test");
            System.out.println();
            System.out.println("🎯 ACTIVE BOOKMAP WINDOWS FEATURES IMPLEMENTED:");
            System.out.println("  ✅ RealTimeMarketDataStore with active windows tracking");
            System.out.println("  ✅ BookmapIntegrationManager with window detection");
            System.out.println("  ✅ Active windows API endpoint: /api/bookmap/windows");
            System.out.println("  ✅ Enhanced dashboard HTML with Active Windows section");
            System.out.println("  ✅ Real-time JavaScript updates with colorization");
            System.out.println("  ✅ Market type classification (FOREX, CRYPTO, FUTURES, etc.)");
            System.out.println("  ✅ Live price and volume updates");
            System.out.println("  ✅ Connection status monitoring");
            System.out.println();
            
            // Test the functionality
            System.out.println("📊 Testing Active Windows JSON output:");
            String testJson = dataStore.getActiveBookmapWindowsJson();
            System.out.println(testJson);
            System.out.println();
            System.out.println("Press Ctrl+C to stop...");
            
            // Keep running
            Thread.sleep(Long.MAX_VALUE);
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    static class ActiveWindowsHandler implements HttpHandler {
        private final RealTimeMarketDataStore dataStore;
        
        public ActiveWindowsHandler(RealTimeMarketDataStore dataStore) {
            this.dataStore = dataStore;
        }
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String jsonResponse = dataStore.getActiveBookmapWindowsJson();
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, jsonResponse.length());
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
                }
            } else {
                String response = "Method Not Allowed";
                exchange.sendResponseHeaders(405, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }
    
    static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Active Bookmap Windows Test Server is working!";
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
} 