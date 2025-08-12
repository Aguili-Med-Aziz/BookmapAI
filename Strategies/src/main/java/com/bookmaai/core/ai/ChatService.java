package com.bookmaai.core.ai;

import com.bookmaai.core.RealTimeMarketDataStore;
import com.bookmaai.core.sliding.EnhancedSlidingWindowManager;
import com.bookmaai.core.sliding.SlidingWindow;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Lightweight AI-style responder that crafts intelligent replies
 * using current market data, active sessions, and recent patterns.
 * This avoids external dependencies and works offline.
 */
public final class ChatService {

    private static final ChatService INSTANCE = new ChatService();
    public static ChatService getInstance() { return INSTANCE; }

    private ChatService() {}

    public String respond(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "I can analyze symbols, sessions, and patterns. Ask about a symbol (e.g., 'ES 1M'), sessions, or trends.";
        }
        String m = message.trim();
        String ml = m.toLowerCase(Locale.US);

        // Try to extract a symbol token (simple heuristic: upper case word with letters/numbers)
        String symbol = extractSymbolLikeToken(m);
        String timeframe = extractTimeframeToken(ml);

        RealTimeMarketDataStore store = RealTimeMarketDataStore.getInstance();

        if (ml.contains("session")) {
            int active = store.getActiveBookmapWindows().size();
            return "There are " + active + " active Bookmap sessions. Use the Sessions panel to select an instrument.";
        }

        if (symbol != null) {
            // Compose instrument-focused answer
            StringBuilder sb = new StringBuilder();
            sb.append("Analysis for ").append(symbol);
            if (timeframe != null) sb.append(" (" + timeframe + ")");
            sb.append(": ");

            // Sliding window snapshot
            String tf = timeframe != null ? timeframe : "1M";
            Optional<SlidingWindow.OHLCVData> o = EnhancedSlidingWindowManager.getInstance().getAggregatedData(symbol, tf);
            if (o.isPresent()) {
                SlidingWindow.OHLCVData d = o.get();
                sb.append(String.format(Locale.US, "O=%.5f H=%.5f L=%.5f C=%.5f Vol=%d Δ=%.2f%%. ",
                        d.open, d.high, d.low, d.close, d.volume, d.priceChange));
                if (Math.abs(d.priceChange) >= 0.5) {
                    sb.append(d.priceChange > 0 ? "Momentum is positive; " : "Momentum is negative; ");
                }
            } else {
                sb.append("no recent OHLCV snapshot yet. ");
            }

            // Pattern highlights (if any)
            Map<String, RealTimeMarketDataStore.PatternData> patterns = store.getPatternData();
            long count = patterns.values().stream().filter(p -> symbol.equals(p.getSymbol())).count();
            if (count > 0) {
                sb.append("Detected ").append(count).append(" pattern(s) recently. ");
            }

            sb.append("You can view live charts on the dashboard and switch timeframes in the instrument panel.");
            return sb.toString();
        }

        if (ml.contains("pattern") || ml.contains("signal")) {
            int total = store.getPatternData().size();
            return total > 0 ? ("There are " + total + " recent patterns across instruments. Select a session to see details.")
                             : "No patterns detected yet. Keep charts open to accumulate data.";
        }

        if (ml.contains("help") || ml.contains("how")) {
            return "Try: 'ES 1M', 'sessions', or 'patterns'. Click a session card to focus an instrument and see charts update in real time.";
        }

        // Fallback generic reply
        return "I analyze active sessions, sliding-window OHLCV, and recent patterns. Ask about a symbol (e.g., 'NQ 5M') or 'sessions'.";
    }

    private String extractSymbolLikeToken(String message) {
        String[] parts = message.split("\\s+");
        for (String p : parts) {
            String t = p.replaceAll("[^A-Za-z0-9._-]", "");
            if (t.length() >= 2 && t.equals(t.toUpperCase(Locale.US)) && t.matches("[A-Z0-9._-]+")) {
                return t;
            }
        }
        return null;
    }

    private String extractTimeframeToken(String ml) {
        if (ml.contains(" 1m") || ml.endsWith(" 1m") || ml.startsWith("1m ") || ml.equals("1m")) return "1M";
        if (ml.contains(" 5m") || ml.endsWith(" 5m") || ml.startsWith("5m ") || ml.equals("5m")) return "5M";
        if (ml.contains(" 15m")|| ml.equals("15m")) return "15M";
        if (ml.contains(" 1h") || ml.equals("1h")) return "1H";
        return null;
    }
}



