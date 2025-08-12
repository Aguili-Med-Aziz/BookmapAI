package com.bookmaai.core.sliding;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Per-instrument sliding window with OHLCV aggregation.
 */
public final class SlidingWindow {

    private final String symbol;
    private final String timeframe;
    private final EnhancedSlidingWindowManager.TimeframeConfig config;
    private final ConcurrentLinkedQueue<TickData> ticks = new ConcurrentLinkedQueue<>();
    private volatile long lastCalcMs = 0L;
    private volatile OHLCVData current;

    public SlidingWindow(String symbol, String timeframe, EnhancedSlidingWindowManager.TimeframeConfig config) {
        this.symbol = symbol;
        this.timeframe = timeframe;
        this.config = config;
    }

    public void addTick(TickData tick) {
        ticks.offer(tick);
        while (ticks.size() > config.maxTicks) {
            ticks.poll();
        }
    }

    public void calculateIfDue() {
        long now = System.currentTimeMillis();
        if (now - lastCalcMs < config.calculationInterval.toMillis()) return;
        calculate();
        lastCalcMs = now;
    }

    private void calculate() {
        List<TickData> list = new ArrayList<>(ticks);
        if (list.isEmpty()) return;
        list.sort(Comparator.comparingLong(t -> t.timestamp));
        double open = list.get(0).price;
        double high = list.stream().mapToDouble(t -> t.price).max().orElse(open);
        double low = list.stream().mapToDouble(t -> t.price).min().orElse(open);
        double close = list.get(list.size() - 1).price;
        long volume = list.stream().mapToLong(t -> t.volume).sum();
        double priceChange = open == 0 ? 0 : ((close - open) / open) * 100.0;
        current = new OHLCVData(symbol, timeframe, open, high, low, close, volume, priceChange, System.currentTimeMillis());
    }

    public Optional<OHLCVData> getCurrentOHLCV() {
        return Optional.ofNullable(current);
    }

    public static final class TickData {
        public final double price;
        public final long volume;
        public final long timestamp;
        public final boolean isBid;
        public TickData(double price, long volume, long timestamp, boolean isBid) {
            this.price = price;
            this.volume = volume;
            this.timestamp = timestamp;
            this.isBid = isBid;
        }
    }

    public static final class OHLCVData {
        public final String symbol;
        public final String timeframe;
        public final double open, high, low, close;
        public final long volume;
        public final double priceChange;
        public final long timestamp;
        public OHLCVData(String symbol, String timeframe, double open, double high, double low, double close, long volume, double priceChange, long timestamp) {
            this.symbol = symbol;
            this.timeframe = timeframe;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.volume = volume;
            this.priceChange = priceChange;
            this.timestamp = timestamp;
        }
    }
}



