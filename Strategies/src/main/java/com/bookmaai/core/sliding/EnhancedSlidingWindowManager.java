package com.bookmaai.core.sliding;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * EnhancedSlidingWindowManager aggregates ticks into multi-timeframe sliding windows per instrument.
 * Additive-only: does not change existing behavior elsewhere.
 */
public final class EnhancedSlidingWindowManager {

    private static final EnhancedSlidingWindowManager INSTANCE = new EnhancedSlidingWindowManager();
    public static EnhancedSlidingWindowManager getInstance() { return INSTANCE; }

    // Instrument -> timeframe -> window
    private final Map<String, Map<String, SlidingWindow>> symbolToWindows = new ConcurrentHashMap<>();

    private final ExecutorService calculationPool = Executors.newFixedThreadPool(4);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private final AtomicLong totalTicksProcessed = new AtomicLong(0);

    private static final Map<String, TimeframeConfig> TIMEFRAME_CONFIGS;
    static {
        Map<String, TimeframeConfig> m = new HashMap<>();
        m.put("1M", new TimeframeConfig(Duration.ofSeconds(60), 600));
        m.put("5M", new TimeframeConfig(Duration.ofSeconds(300), 1200));
        m.put("15M", new TimeframeConfig(Duration.ofSeconds(900), 1800));
        m.put("1H", new TimeframeConfig(Duration.ofSeconds(3600), 2400));
        TIMEFRAME_CONFIGS = Collections.unmodifiableMap(m);
    }

    private EnhancedSlidingWindowManager() {
        // Periodic calculation for all windows
        scheduler.scheduleAtFixedRate(this::calculateAll, 1, 1, TimeUnit.SECONDS);
    }

    public void initializeInstrument(String symbol) {
        symbolToWindows.computeIfAbsent(symbol, s -> {
            Map<String, SlidingWindow> tfs = new ConcurrentHashMap<>();
            for (Map.Entry<String, TimeframeConfig> e : TIMEFRAME_CONFIGS.entrySet()) {
                tfs.put(e.getKey(), new SlidingWindow(symbol, e.getKey(), e.getValue()));
            }
            return tfs;
        });
    }

    public void processTick(String symbol, double price, long volume, long timestamp, boolean isBid) {
        Objects.requireNonNull(symbol, "symbol");
        initializeInstrument(symbol);
        Map<String, SlidingWindow> tfs = symbolToWindows.get(symbol);
        if (tfs == null) return;
        SlidingWindow.TickData tick = new SlidingWindow.TickData(price, volume, timestamp, isBid);
        for (SlidingWindow w : tfs.values()) {
            w.addTick(tick);
        }
        totalTicksProcessed.incrementAndGet();
    }

    public Optional<SlidingWindow.OHLCVData> getAggregatedData(String symbol, String timeframe) {
        Map<String, SlidingWindow> tfs = symbolToWindows.get(symbol);
        if (tfs == null) return Optional.empty();
        SlidingWindow w = tfs.get(timeframe);
        return w != null ? w.getCurrentOHLCV() : Optional.empty();
    }

    public Set<String> getAvailableTimeframes(String symbol) {
        Map<String, SlidingWindow> tfs = symbolToWindows.get(symbol);
        return tfs != null ? tfs.keySet() : Collections.emptySet();
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("symbols", symbolToWindows.size());
        stats.put("totalTicksProcessed", totalTicksProcessed.get());
        return stats;
    }

    private void calculateAll() {
        for (Map.Entry<String, Map<String, SlidingWindow>> entry : symbolToWindows.entrySet()) {
            String symbol = entry.getKey();
            Map<String, SlidingWindow> tfs = entry.getValue();
            for (Map.Entry<String, SlidingWindow> e : tfs.entrySet()) {
                String timeframe = e.getKey();
                SlidingWindow w = e.getValue();
                w.calculateIfDue();
                // Export snapshot to CSV via PatternRecorder as a lightweight log
                w.getCurrentOHLCV().ifPresent(ohlcv -> {
                    try {
                        String detailsJson = "{\"timeframe\":\"" + timeframe + "\"," +
                                "\"open\":" + ohlcv.open + "," +
                                "\"high\":" + ohlcv.high + "," +
                                "\"low\":" + ohlcv.low + "," +
                                "\"close\":" + ohlcv.close + "," +
                                "\"volume\":" + ohlcv.volume + "," +
                                "\"priceChange\":" + ohlcv.priceChange + "," +
                                "\"timestamp\":" + ohlcv.timestamp + "}";
                        com.bookmaai.core.PatternRecorder.getInstance().writePattern(
                                symbol, "SLIDING_WINDOW_" + timeframe, 1.0, detailsJson);
                    } catch (Throwable ignored) {}
                });
            }
        }
    }

    public static final class TimeframeConfig {
        public final Duration calculationInterval;
        public final int maxTicks;
        public TimeframeConfig(Duration calculationInterval, int maxTicks) {
            this.calculationInterval = calculationInterval;
            this.maxTicks = maxTicks;
        }
    }
}


