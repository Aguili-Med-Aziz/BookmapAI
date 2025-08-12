package com.bookmaai.core.session;

import com.bookmaai.core.RealTimeMarketDataStore;
import com.bookmaai.core.sliding.EnhancedSlidingWindowManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-window sessions. Additive-only and integrates with RealTimeMarketDataStore events.
 */
public final class BookmapWindowSessionManager implements RealTimeMarketDataStore.BookmapWindowListener {

    private static final BookmapWindowSessionManager INSTANCE = new BookmapWindowSessionManager();
    public static BookmapWindowSessionManager getInstance() { return INSTANCE; }

    private final Map<String, WindowSession> windowIdToSession = new ConcurrentHashMap<>();
    private final Map<String, String> symbolToWindowId = new ConcurrentHashMap<>();

    private BookmapWindowSessionManager() {
        RealTimeMarketDataStore.getInstance().registerBookmapWindowListener(this);
    }

    @Override
    public void onWindowOpened(RealTimeMarketDataStore.BookmapWindow window) {
        WindowSession session = new WindowSession(window.getWindowId(), window.getSymbol(), Instant.now(), window.getStatus());
        windowIdToSession.put(window.getWindowId(), session);
        symbolToWindowId.put(window.getSymbol(), window.getWindowId());
        EnhancedSlidingWindowManager.getInstance().initializeInstrument(window.getSymbol());
        System.out.println("Session opened for symbol " + window.getSymbol());
    }

    @Override
    public void onWindowClosed(String windowId, String symbol) {
        windowIdToSession.remove(windowId);
        symbolToWindowId.remove(symbol);
        System.out.println("Session closed for symbol " + symbol);
    }

    public List<WindowSession> getActiveSessions() {
        return new ArrayList<>(windowIdToSession.values());
    }

    public Optional<WindowSession> getSessionBySymbol(String symbol) {
        String win = symbolToWindowId.get(symbol);
        return win == null ? Optional.empty() : Optional.ofNullable(windowIdToSession.get(win));
    }

    public static final class WindowSession {
        public final String windowId;
        public final String symbol;
        public final Instant createdAt;
        public final String status;
        public WindowSession(String windowId, String symbol, Instant createdAt, String status) {
            this.windowId = windowId;
            this.symbol = symbol;
            this.createdAt = createdAt;
            this.status = status;
        }
    }
}



