package com.bookmaai.test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Random;

/**
 * Simple test to demonstrate real-time dashboard with 1-second refresh and colorization
 */
public class RealTimeTest {
    
    private static Random random = new Random();
    private static long startTime = System.currentTimeMillis();
    
    public static void main(String[] args) {
        System.out.println("Starting Real-Time Dashboard Test...");
        System.out.println("Features: 1-second refresh, colorized value changes");
        
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8082), 0);
            server.createContext("/", new DashboardHandler());
            server.createContext("/api/data", new DataHandler());
            server.setExecutor(null);
            server.start();
            
            System.out.println("Real-time test dashboard started!");
            System.out.println("Access at: http://localhost:8082");
            System.out.println("Watch for colorized value changes every second!");
            System.out.println("Press Ctrl+C to stop...");
            
            // Keep running
            Thread.sleep(Long.MAX_VALUE);
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = generateTestHTML();
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, html.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(html.getBytes("UTF-8"));
            }
        }
        
        private String generateTestHTML() {
            return "<!DOCTYPE html>" +
                   "<html><head>" +
                   "<title>Real-Time Dashboard Test</title>" +
                   "<style>" +
                   "body { font-family: Arial, sans-serif; background: #0a0e1a; color: white; padding: 20px; }" +
                   ".header { text-align: center; padding: 20px; background: linear-gradient(135deg, #1e3c72, #2a5298); border-radius: 10px; margin-bottom: 20px; }" +
                   ".metrics { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; }" +
                   ".panel { background: #1a1f2e; padding: 20px; border-radius: 10px; border: 1px solid #2a3441; }" +
                   ".metric { display: flex; justify-content: space-between; margin: 10px 0; padding: 8px 0; }" +
                   ".value { font-weight: bold; font-size: 1.2em; }" +
                   ".status { position: fixed; top: 10px; right: 10px; background: #4fc3f7; color: #0a0e1a; padding: 8px 16px; border-radius: 20px; font-size: 0.8em; }" +
                   ".counter { position: fixed; bottom: 10px; right: 10px; background: #2a3441; color: #4fc3f7; padding: 8px 16px; border-radius: 20px; }" +
                   
                   // Animation styles for colorization
                   "@keyframes valueIncreased { 0% { background: #4caf50; color: white; transform: scale(1.05); } 100% { background: transparent; color: #4caf50; transform: scale(1); } }" +
                   "@keyframes valueDecreased { 0% { background: #f44336; color: white; transform: scale(1.05); } 100% { background: transparent; color: #f44336; transform: scale(1); } }" +
                   "@keyframes valueUpdated { 0% { background: #4fc3f7; color: #0a0e1a; transform: scale(1.05); } 100% { background: transparent; color: inherit; transform: scale(1); } }" +
                   ".value-increased { animation: valueIncreased 2s ease-out; color: #4caf50 !important; }" +
                   ".value-decreased { animation: valueDecreased 2s ease-out; color: #f44336 !important; }" +
                   ".value-updated { animation: valueUpdated 2s ease-out; }" +
                   
                   "</style>" +
                   "</head><body>" +
                   
                   "<div class='status' id='status'>LIVE</div>" +
                   "<div class='counter' id='counter'>Updates: 0</div>" +
                   
                   "<div class='header'>" +
                   "<h1>Real-Time Dashboard Test</h1>" +
                   "<p>1-Second Refresh with Colorized Value Changes</p>" +
                   "</div>" +
                   
                   "<div class='metrics'>" +
                   "<div class='panel'>" +
                   "<h3>System Metrics</h3>" +
                   "<div class='metric'><span>Accuracy</span><span class='value' id='accuracy'>---%</span></div>" +
                   "<div class='metric'><span>Active Signals</span><span class='value' id='signals'>---</span></div>" +
                   "<div class='metric'><span>P&L</span><span class='value' id='pnl'>$---</span></div>" +
                   "<div class='metric'><span>CPU Usage</span><span class='value' id='cpu'>---%</span></div>" +
                   "</div>" +
                   
                   "<div class='panel'>" +
                   "<h3>Trading Activity</h3>" +
                   "<div class='metric'><span>Total Trades</span><span class='value' id='trades'>---</span></div>" +
                   "<div class='metric'><span>Win Rate</span><span class='value' id='winrate'>---%</span></div>" +
                   "<div class='metric'><span>Risk Level</span><span class='value' id='risk'>---</span></div>" +
                   "<div class='metric'><span>Market Status</span><span class='value' id='market'>---</span></div>" +
                   "</div>" +
                   
                   "<div class='panel'>" +
                   "<h3>Real-Time Features</h3>" +
                   "<div class='metric'><span>Update Frequency</span><span class='value'>1 Second</span></div>" +
                   "<div class='metric'><span>Green Animation</span><span class='value' style='color: #4caf50;'>Increased Values</span></div>" +
                   "<div class='metric'><span>Red Animation</span><span class='value' style='color: #f44336;'>Decreased Values</span></div>" +
                   "<div class='metric'><span>Blue Animation</span><span class='value' style='color: #4fc3f7;'>Updated Values</span></div>" +
                   "</div>" +
                   "</div>" +
                   
                   "<script>" +
                   "let updateCount = 0;" +
                   "let previousValues = {};" +
                   
                   "function updateValue(id, newValue) {" +
                   "  const element = document.getElementById(id);" +
                   "  if (!element) return;" +
                   "  const oldValue = previousValues[id];" +
                   "  const currentText = element.textContent;" +
                   "  if (oldValue !== undefined && currentText !== newValue.toString()) {" +
                   "    element.classList.remove('value-increased', 'value-decreased', 'value-updated');" +
                   "    const isNumeric = !isNaN(parseFloat(newValue.toString().replace(/[^0-9.-]/g, '')));" +
                   "    if (isNumeric) {" +
                   "      const oldNum = parseFloat(oldValue.replace(/[^0-9.-]/g, ''));" +
                   "      const newNum = parseFloat(newValue.toString().replace(/[^0-9.-]/g, ''));" +
                   "      if (newNum > oldNum) element.classList.add('value-increased');" +
                   "      else if (newNum < oldNum) element.classList.add('value-decreased');" +
                   "      else element.classList.add('value-updated');" +
                   "    } else { element.classList.add('value-updated'); }" +
                   "    setTimeout(() => element.classList.remove('value-increased', 'value-decreased', 'value-updated'), 2000);" +
                   "  }" +
                   "  element.textContent = newValue;" +
                   "  previousValues[id] = newValue.toString();" +
                   "}" +
                   
                   "function updateData() {" +
                   "  fetch('/api/data')" +
                   "    .then(r => r.json())" +
                   "    .then(data => {" +
                   "      updateValue('accuracy', data.accuracy + '%');" +
                   "      updateValue('signals', data.signals);" +
                   "      updateValue('pnl', '$' + data.pnl);" +
                   "      updateValue('cpu', data.cpu + '%');" +
                   "      updateValue('trades', data.trades);" +
                   "      updateValue('winrate', data.winrate + '%');" +
                   "      updateValue('risk', data.risk);" +
                   "      updateValue('market', data.market);" +
                   "      updateCount++;" +
                   "      document.getElementById('counter').textContent = 'Updates: ' + updateCount;" +
                   "    })" +
                   "    .catch(e => console.log('API error:', e));" +
                   "}" +
                   
                   "// Start 1-second updates" +
                   "setInterval(updateData, 1000);" +
                   "updateData();" +
                   "</script>" +
                   "</body></html>";
        }
    }
    
    static class DataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Generate dynamic data with realistic variations
            long elapsed = System.currentTimeMillis() - startTime;
            double timeVar = Math.sin(elapsed / 10000.0) * 2;
            double quickVar = Math.sin(elapsed / 2000.0) * 0.5;
            
            double accuracy = Math.max(80, Math.min(95, 87.5 + timeVar * 0.5));
            int signals = Math.max(10, (int)(25 + timeVar * 3 + quickVar * 2));
            int pnl = (int)(1250 + timeVar * 100 + quickVar * 50);
            double cpu = Math.max(5, Math.min(25, 12.5 + quickVar * 3));
            int trades = Math.max(50, (int)(150 + timeVar * 20));
            double winrate = Math.max(70, Math.min(95, 85 + timeVar * 1));
            String risk = (winrate > 80) ? "Low" : (winrate > 75) ? "Medium" : "High";
            String market = (elapsed % 30000 < 15000) ? "Active" : "Volatile";
            
            String json = String.format(
                "{\"accuracy\":%.1f,\"signals\":%d,\"pnl\":%d,\"cpu\":%.1f," +
                "\"trades\":%d,\"winrate\":%.1f,\"risk\":\"%s\",\"market\":\"%s\"}",
                accuracy, signals, pnl, cpu, trades, winrate, risk, market
            );
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(json.getBytes("UTF-8"));
            }
        }
    }
} 