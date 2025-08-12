package com.bookmaai.core;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * PatternRecorder writes detected patterns to per-instrument CSV files safely.
 * Thread-safe, buffered, and lifecycle-aware.
 */
public final class PatternRecorder implements RealTimeMarketDataStore.BookmapWindowListener {

    private static final PatternRecorder INSTANCE = new PatternRecorder();
    public static PatternRecorder getInstance() { return INSTANCE; }

    private final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);
    private final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    private final Map<String, WriterBundle> symbolToWriters = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<String> writeQueue = new LinkedBlockingQueue<>(10000);
    private final AtomicBoolean writerActive = new AtomicBoolean(false);
    private Thread writerThread;

    private volatile boolean initialized;

    private PatternRecorder() {}

    public void writePattern(String symbol, String patternName, double confidence, String detailsJson) {
        ensureInitialized();
        Objects.requireNonNull(symbol, "symbol");
        Objects.requireNonNull(patternName, "patternName");
        if (detailsJson == null) detailsJson = "{}";

        String timestamp = ISO.format(Instant.now());
        String safeSymbol = sanitize(symbol);
        String day = DATE.format(Instant.now());
        String dir = getExportsBaseDir() + "/" + safeSymbol;
        String file = safeSymbol + "_" + day + "_patterns.csv";

        ensureWriter(safeSymbol, Paths.get(dir, file));

        // CSV schema: timestamp_iso8601,instrument,pattern_name,confidence,details_json
        String line = String.format(Locale.US, "%s,%s,%s,%.4f,%s%n",
                escape(timestamp), escape(safeSymbol), escape(patternName), confidence, escape(detailsJson));
        offer(line);
    }

    private String getExportsBaseDir() {
        // Prefer explicit Bookmap home if set
        String bmHome = System.getenv("BOOKMAP_HOME");
        if (bmHome != null && !bmHome.trim().isEmpty()) {
            return bmHome.replace('\\','/') + "/exports";
        }
        // If C:\\Bookmap exists, put exports under it
        try {
            java.nio.file.Path bmPath = java.nio.file.Paths.get("C:/Bookmap");
            if (java.nio.file.Files.exists(bmPath)) {
                return "C:/Bookmap/exports";
            }
        } catch (Throwable ignored) {}
        // Fallback to current working directory
        return "exports";
    }

    public void closeWriter(String symbol) {
        WriterBundle bundle = symbolToWriters.remove(sanitize(symbol));
        if (bundle != null) bundle.closeQuietly();
    }

    public void closeAll() {
        symbolToWriters.values().forEach(WriterBundle::closeQuietly);
        symbolToWriters.clear();
        stopWriter();
    }

    private synchronized void ensureInitialized() {
        if (initialized) return;
        RealTimeMarketDataStore.getInstance().registerBookmapWindowListener(this);
        startWriter();
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeAll, "pattern-recorder-shutdown"));
        initialized = true;
    }

    private void ensureWriter(String symbol, Path filePath) {
        symbolToWriters.computeIfAbsent(symbol, s -> {
            try {
                Files.createDirectories(filePath.getParent());
                boolean newFile = Files.notExists(filePath);
                FileWriter fw = new FileWriter(filePath.toFile(), true);
                if (newFile) {
                    fw.write("timestamp_iso8601,instrument,pattern_name,confidence,details_json\n");
                    fw.flush();
                }
                return new WriterBundle(fw);
            } catch (IOException e) {
                throw new RuntimeException("Failed to open writer for " + filePath + ": " + e.getMessage(), e);
            }
        });
    }

    private void startWriter() {
        if (writerActive.compareAndSet(false, true)) {
            writerThread = new Thread(() -> {
                while (writerActive.get() || !writeQueue.isEmpty()) {
                    try {
                        String line = writeQueue.poll(250, TimeUnit.MILLISECONDS);
                        if (line != null) {
                            // Line format prefix: [symbol]|<csv>
                            int bar = line.indexOf('|');
                            String symbolKey = line.substring(0, bar);
                            String csv = line.substring(bar + 1);
                            WriterBundle bundle = symbolToWriters.get(symbolKey);
                            if (bundle != null) bundle.write(csv);
                        }
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    } catch (IOException ioe) {
                        System.err.println("PatternRecorder write error: " + ioe.getMessage());
                    }
                }
                symbolToWriters.values().forEach(WriterBundle::flushQuietly);
            }, "pattern-recorder-writer");
            writerThread.setDaemon(true);
            writerThread.start();
        }
    }

    private void stopWriter() {
        writerActive.set(false);
        if (writerThread != null) writerThread.interrupt();
    }

    private void offer(String csvLine) {
        // Prefix with symbol key for routing to the right writer
        // csvLine already escaped and ends with newline
        String symbol = extractSymbol(csvLine);
        if (!writeQueue.offer(symbol + '|' + csvLine)) {
            System.err.println("PatternRecorder queue full; dropping line");
        }
    }

    private static String extractSymbol(String csvLine) {
        // timestamp,instrument,...  -> extract column 2
        int firstComma = csvLine.indexOf(',');
        int secondComma = csvLine.indexOf(',', firstComma + 1);
        return csvLine.substring(firstComma + 1, secondComma);
    }

    private static String sanitize(String s) {
        return s.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static String escape(String s) {
        if (s.indexOf(',') >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }

    @Override
    public void onWindowOpened(RealTimeMarketDataStore.BookmapWindow window) {
        // No-op; writer is created lazily on first write
        System.out.println("PatternRecorder: window opened for " + window.getSymbol());
    }

    @Override
    public void onWindowClosed(String windowId, String symbol) {
        System.out.println("PatternRecorder: window closed for " + symbol + ", closing writer");
        closeWriter(symbol);
    }

    private static final class WriterBundle {
        private final FileWriter writer;
        private WriterBundle(FileWriter writer) { this.writer = writer; }
        void write(String line) throws IOException { synchronized (writer) { writer.write(line); writer.flush(); } }
        void flushQuietly() { try { synchronized (writer) { writer.flush(); } } catch (IOException ignored) {} }
        void closeQuietly() { try { synchronized (writer) { writer.flush(); writer.close(); } } catch (IOException ignored) {} }
    }
}


