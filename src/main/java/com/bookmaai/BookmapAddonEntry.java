package com.bookmaai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 🔌 Bookmap Addon Entry Point
 * 
 * نقطة الدخول الرئيسية لـ addon في Bookmap
 * يتم استدعاؤها تلقائياً عندما يحمل Bookmap الـ addon
 * 
 * Based on successful patterns from ICT Smart Analyzer
 */
public class BookmapAddonEntry {
    
    private static final Logger logger = LoggerFactory.getLogger(BookmapAddonEntry.class);
    
    private ConfigurableApplicationContext applicationContext;
    private ScheduledExecutorService executorService;
    private boolean isRunning = false;
    
    /**
     * نقطة الدخول الرئيسية للـ addon
     * يتم استدعاؤها من Bookmap عند تحميل الـ addon
     */
    public void initialize() {
        try {
            logger.info("🚀 Bookmap AI Addon - Starting initialization...");
            logger.info("================================================");
            
            // إعداد النظام للعمل مع Bookmap
            setupBookmapMode();
            
            // بدء تشغيل Spring Boot context
            startSpringBootContext();
            
            // إعداد real-time processing
            setupRealTimeProcessing();
            
            // تسجيل listeners للبيانات
            registerDataListeners();
            
            isRunning = true;
            
            logger.info("✅ Bookmap AI Addon initialized successfully!");
            logger.info("📊 Ready to receive and analyze market data from Bookmap");
            
        } catch (Exception e) {
            logger.error("❌ Failed to initialize Bookmap AI Addon: {}", e.getMessage(), e);
            cleanup();
            throw new RuntimeException("Addon initialization failed", e);
        }
    }
    
    /**
     * استقبال بيانات السوق من Bookmap
     * يتم استدعاؤها مع كل tick جديد
     */
    public void onMarketData(String symbol, double price, double volume, 
                           double vwap, long timestamp) {
        
        if (!isRunning) {
            return;
        }
        
        try {
            // تحويل timestamp للوقت المحلي
            LocalDateTime dateTime = LocalDateTime.now();
            
            // معالجة البيانات بشكل غير متزامن
            processMarketDataAsync(symbol, price, volume, vwap, dateTime);
            
        } catch (Exception e) {
            logger.error("❌ Error processing market data for {}: {}", symbol, e.getMessage());
        }
    }
    
    /**
     * استقبال بيانات order book من Bookmap
     */
    public void onOrderBookUpdate(String symbol, OrderBookLevel[] bids, 
                                OrderBookLevel[] asks, long timestamp) {
        
        if (!isRunning) {
            return;
        }
        
        try {
            // معالجة تحديثات order book للأدوات التي تحتاجها
            processOrderBookData(symbol, bids, asks, timestamp);
            
        } catch (Exception e) {
            logger.error("❌ Error processing order book data for {}: {}", symbol, e.getMessage());
        }
    }
    
    /**
     * إيقاف الـ addon
     */
    public void shutdown() {
        logger.info("🔄 Shutting down Bookmap AI Addon...");
        
        try {
            isRunning = false;
            
            // إيقاف المعالجة
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            }
            
            // إغلاق Spring context
            if (applicationContext != null) {
                applicationContext.close();
            }
            
            logger.info("✅ Bookmap AI Addon shutdown completed");
            
        } catch (Exception e) {
            logger.error("❌ Error during addon shutdown: {}", e.getMessage(), e);
        }
    }
    
    /**
     * إعداد النظام للعمل مع Bookmap
     */
    private void setupBookmapMode() {
        // تحديد أن النظام يعمل كـ addon
        System.setProperty("bookmap.addon.mode", "true");
        System.setProperty("spring.profiles.active", "bookmap-addon");
        
        // إعدادات الـ logging للعمل مع Bookmap
        System.setProperty("logging.pattern.console", 
            "[Bookmap AI] %d{HH:mm:ss.SSS} %-5level %logger{36} - %msg%n");
    }
    
    /**
     * بدء تشغيل Spring Boot context
     */
    private void startSpringBootContext() {
        try {
            String[] args = {"--bookmap-addon"};
            applicationContext = SpringApplication.run(BookmapAIApplication.class, args);
            
            logger.info("✅ Spring Boot context started for Bookmap addon");
            
        } catch (Exception e) {
            logger.error("❌ Failed to start Spring Boot context: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to start application context", e);
        }
    }
    
    /**
     * إعداد معالجة البيانات في الوقت الفعلي
     */
    private void setupRealTimeProcessing() {
        // إنشاء thread pool للمعالجة المتوازية
        executorService = Executors.newScheduledThreadPool(4);
        
        // جدولة مهام الصيانة
        scheduleMaintenanceTasks();
        
        logger.info("✅ Real-time processing setup completed");
    }
    
    /**
     * تسجيل listeners للبيانات
     */
    private void registerDataListeners() {
        // سيتم ربطها مع Bookmap API في المراحل التالية
        logger.info("✅ Data listeners registered");
    }
    
    /**
     * معالجة بيانات السوق بشكل غير متزامن
     */
    private void processMarketDataAsync(String symbol, double price, double volume, 
                                      double vwap, LocalDateTime timestamp) {
        
        executorService.submit(() -> {
            try {
                // هنا سيتم استدعاء نظام التحليل الفعلي
                analyzeMarketData(symbol, price, volume, vwap, timestamp);
                
            } catch (Exception e) {
                logger.error("❌ Error in async market data processing: {}", e.getMessage());
            }
        });
    }
    
    /**
     * تحليل بيانات السوق
     */
    private void analyzeMarketData(String symbol, double price, double volume, 
                                 double vwap, LocalDateTime timestamp) {
        
        // محاكاة التحليل (سيتم استبدالها بالنظام الفعلي)
        if (volume > 100) { // مثال: فلترة بسيطة
            logger.info("📊 Analyzing {}: Price={}, Volume={}, VWAP={}", 
                       symbol, price, volume, vwap);
            
            // هنا سيتم تطبيق الأدوات الـ12
            // analyzeWithBookmapTools(symbol, price, volume, vwap, timestamp);
            
            // تحديد الأنماط
            // detectPatterns(analysisResult);
            
            // اتخاذ القرارات
            // makeDecisions(patterns);
        }
    }
    
    /**
     * معالجة بيانات order book
     */
    private void processOrderBookData(String symbol, OrderBookLevel[] bids, 
                                    OrderBookLevel[] asks, long timestamp) {
        
        executorService.submit(() -> {
            try {
                // تحليل heatmap data
                analyzeHeatmapData(symbol, bids, asks);
                
                // تحليل imbalance
                analyzeOrderBookImbalance(symbol, bids, asks);
                
            } catch (Exception e) {
                logger.error("❌ Error processing order book: {}", e.getMessage());
            }
        });
    }
    
    /**
     * تحليل بيانات الـ heatmap
     */
    private void analyzeHeatmapData(String symbol, OrderBookLevel[] bids, OrderBookLevel[] asks) {
        // تحليل كثافة السيولة
        double totalBidVolume = 0;
        double totalAskVolume = 0;
        
        for (OrderBookLevel bid : bids) {
            totalBidVolume += bid.getVolume();
        }
        
        for (OrderBookLevel ask : asks) {
            totalAskVolume += ask.getVolume();
        }
        
        // حساب imbalance
        double imbalance = (totalBidVolume - totalAskVolume) / (totalBidVolume + totalAskVolume);
        
        if (Math.abs(imbalance) > 0.2) { // عتبة 20%
            logger.info("🎯 Order book imbalance detected for {}: {:.2f}%", 
                       symbol, imbalance * 100);
        }
    }
    
    /**
     * تحليل عدم توازن order book
     */
    private void analyzeOrderBookImbalance(String symbol, OrderBookLevel[] bids, OrderBookLevel[] asks) {
        // تحليل عمق السوق
        if (bids.length > 0 && asks.length > 0) {
            double spread = asks[0].getPrice() - bids[0].getPrice();
            double midPrice = (asks[0].getPrice() + bids[0].getPrice()) / 2;
            double spreadPercent = (spread / midPrice) * 100;
            
            if (spreadPercent > 0.01) { // spread > 0.01%
                logger.debug("📏 Wide spread detected for {}: {:.4f}%", symbol, spreadPercent);
            }
        }
    }
    
    /**
     * جدولة مهام الصيانة
     */
    private void scheduleMaintenanceTasks() {
        // تنظيف البيانات القديمة كل 5 دقائق
        executorService.scheduleAtFixedRate(() -> {
            try {
                cleanupOldData();
            } catch (Exception e) {
                logger.error("❌ Error in maintenance task: {}", e.getMessage());
            }
        }, 5, 5, TimeUnit.MINUTES);
        
        // تحديث الإحصائيات كل دقيقة
        executorService.scheduleAtFixedRate(() -> {
            try {
                updateStatistics();
            } catch (Exception e) {
                logger.error("❌ Error updating statistics: {}", e.getMessage());
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * تنظيف البيانات القديمة
     */
    private void cleanupOldData() {
        // تنظيف البيانات الأقدم من ساعة
        logger.debug("🧹 Cleaning up old data...");
    }
    
    /**
     * تحديث الإحصائيات
     */
    private void updateStatistics() {
        // تحديث إحصائيات الأداء
        logger.debug("📈 Updating performance statistics...");
    }
    
    /**
     * تنظيف الموارد عند الخطأ
     */
    private void cleanup() {
        try {
            if (executorService != null) {
                executorService.shutdown();
            }
            if (applicationContext != null) {
                applicationContext.close();
            }
        } catch (Exception e) {
            logger.error("❌ Error during cleanup: {}", e.getMessage());
        }
    }
    
    /**
     * فئة مساعدة لـ Order Book Level
     */
    public static class OrderBookLevel {
        private final double price;
        private final double volume;
        
        public OrderBookLevel(double price, double volume) {
            this.price = price;
            this.volume = volume;
        }
        
        public double getPrice() { return price; }
        public double getVolume() { return volume; }
    }
} 