package com.bookmaai.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 📊 Bookmap Data Processor - Simplified Version
 * 
 * معالج البيانات المبسط الذي يستقبل البيانات من Bookmap
 * ويطبق الفلترة والتطبيع المطلوب قبل التحليل
 */
@Service
public class BookmapDataProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(BookmapDataProcessor.class);
    
    // إحصائيات المعالجة
    private final AtomicLong totalProcessedTicks = new AtomicLong(0);
    private final AtomicLong filteredTicks = new AtomicLong(0);
    private final AtomicLong significantTicks = new AtomicLong(0);
    
    // بيانات السوق المؤقتة لكل رمز
    private final Map<String, SimpleMarketBuffer> symbolBuffers = new ConcurrentHashMap<>();
    
    // إعدادات الفلترة
    private static final double MIN_PRICE_MOVEMENT_PERCENT = 0.0001; // 0.01%
    private static final double MIN_VOLUME_THRESHOLD = 10.0;    // حد أدنى للحجم
    
    /**
     * معالجة بيانات السوق الواردة من Bookmap
     */
    public SimpleMarketData processMarketData(String symbol, double price, 
                                           double volume, double vwap, 
                                           LocalDateTime timestamp) {
        
        totalProcessedTicks.incrementAndGet();
        
        try {
            // تطبيق الفلترة الأساسية
            if (!passesBasicFilters(symbol, price, volume, vwap)) {
                filteredTicks.incrementAndGet();
                return null;
            }
            
            // الحصول على buffer للرمز
            SimpleMarketBuffer buffer = getOrCreateBuffer(symbol);
            
            // تحديث البيانات
            buffer.addTick(price, volume, vwap, timestamp);
            
            // إنشاء البيانات المعالجة
            SimpleMarketData processedData = createProcessedData(symbol, price, volume, vwap, timestamp, buffer);
            
            if (processedData.isSignificant()) {
                significantTicks.incrementAndGet();
                logSignificantData(symbol, processedData);
            }
            
            return processedData;
            
        } catch (Exception e) {
            logger.error("❌ Error processing market data for {}: {}", symbol, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * تطبيق الفلترة الأساسية
     */
    private boolean passesBasicFilters(String symbol, double price, double volume, double vwap) {
        
        // فلترة الحجم
        if (volume < MIN_VOLUME_THRESHOLD) {
            logger.debug("🔽 Low volume filtered for {}: {}", symbol, volume);
            return false;
        }
        
        // فلترة الأسعار غير المنطقية
        if (price <= 0 || Double.isNaN(price) || Double.isInfinite(price)) {
            logger.warn("⚠️ Invalid price for {}: {}", symbol, price);
            return false;
        }
        
        // فلترة VWAP غير المنطقي
        if (vwap <= 0 || Math.abs(price - vwap) / vwap > 0.1) { // 10% deviation
            logger.debug("⚠️ Unusual VWAP deviation for {}: Price={}, VWAP={}", 
                        symbol, price, vwap);
        }
        
        // فحص الحركة السعرية
        SimpleMarketBuffer buffer = symbolBuffers.get(symbol);
        if (buffer != null && buffer.hasRecentData()) {
            double lastPrice = buffer.getLastPrice();
            double priceChangePercent = Math.abs(price - lastPrice) / lastPrice;
            
            if (priceChangePercent < MIN_PRICE_MOVEMENT_PERCENT) {
                logger.debug("🔽 Small price movement filtered for {}: {:.4f}%", 
                           symbol, priceChangePercent * 100);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * الحصول على أو إنشاء buffer للرمز
     */
    private SimpleMarketBuffer getOrCreateBuffer(String symbol) {
        return symbolBuffers.computeIfAbsent(symbol, k -> new SimpleMarketBuffer(symbol));
    }
    
    /**
     * إنشاء البيانات المعالجة
     */
    private SimpleMarketData createProcessedData(String symbol, double price, double volume, 
                                               double vwap, LocalDateTime timestamp, 
                                               SimpleMarketBuffer buffer) {
        
        // حساب المؤشرات المشتقة
        double momentum = calculateMomentum(buffer, price);
        double volatility = calculateVolatility(buffer);
        double volumeProfile = calculateVolumeProfile(buffer, volume);
        
        // تحديد مستوى الأهمية
        boolean significant = determineSignificance(momentum, volatility, volumeProfile);
        
        return new SimpleMarketData(
            symbol, price, volume, vwap, timestamp,
            momentum, volatility, volumeProfile, significant
        );
    }
    
    /**
     * حساب الزخم
     */
    private double calculateMomentum(SimpleMarketBuffer buffer, double currentPrice) {
        if (buffer.getTickCount() < 3) return 0.0;
        
        double lastPrice = buffer.getLastPrice();
        if (lastPrice == 0.0) return 0.0;
        
        return (currentPrice - lastPrice) / lastPrice;
    }
    
    /**
     * حساب التقلبات
     */
    private double calculateVolatility(SimpleMarketBuffer buffer) {
        if (buffer.getTickCount() < 5) return 0.0;
        
        // حساب بسيط للتقلبات
        double[] recentPrices = buffer.getRecentPrices(5);
        double mean = 0;
        for (double price : recentPrices) {
            mean += price;
        }
        mean /= recentPrices.length;
        
        double variance = 0;
        for (double price : recentPrices) {
            variance += Math.pow(price - mean, 2);
        }
        variance /= recentPrices.length;
        
        return Math.sqrt(variance) / mean; // نسبة التقلبات
    }
    
    /**
     * حساب ملف الحجم
     */
    private double calculateVolumeProfile(SimpleMarketBuffer buffer, double currentVolume) {
        double averageVolume = buffer.getAverageVolume();
        if (averageVolume == 0.0) return 1.0;
        
        return currentVolume / averageVolume; // نسبة الحجم للمتوسط
    }
    
    /**
     * تحديد مستوى الأهمية
     */
    private boolean determineSignificance(double momentum, double volatility, double volumeProfile) {
        double score = 0;
        
        // إضافة نقاط للزخم
        score += Math.abs(momentum) * 1000; // تحويل للنسبة المئوية
        
        // إضافة نقاط للحجم
        if (volumeProfile > 2.0) score += 25;
        else if (volumeProfile > 1.5) score += 15;
        
        // إضافة نقاط للتقلبات
        if (volatility > 0.001) score += 20;
        
        return score > 30; // عتبة الأهمية
    }
    
    /**
     * تسجيل البيانات المهمة
     */
    private void logSignificantData(String symbol, SimpleMarketData data) {
        logger.info("🎯 Significant data for {}: Price={}, Volume={}, Momentum={:.4f}", 
                   symbol, data.getPrice(), data.getVolume(), data.getMomentum());
    }
    
    /**
     * الحصول على إحصائيات المعالجة
     */
    public String getProcessingStats() {
        return String.format("ProcessingStats{total=%d, filtered=%d, significant=%d, symbols=%d}", 
                           totalProcessedTicks.get(), filteredTicks.get(), 
                           significantTicks.get(), symbolBuffers.size());
    }
    
    /**
     * تنظيف البيانات القديمة
     */
    public void cleanupOldData() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
        
        symbolBuffers.values().forEach(buffer -> {
            buffer.removeOldData(cutoffTime);
        });
        
        // إزالة buffers فارغة
        symbolBuffers.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        
        logger.debug("🧹 Cleaned up old data. Active symbols: {}", symbolBuffers.size());
    }
    
    // ===== Supporting Classes =====
    
    /**
     * بيانات السوق المبسطة
     */
    public static class SimpleMarketData {
        private final String symbol;
        private final double price;
        private final double volume;
        private final double vwap;
        private final LocalDateTime timestamp;
        private final double momentum;
        private final double volatility;
        private final double volumeProfile;
        private final boolean significant;
        
        public SimpleMarketData(String symbol, double price, double volume, double vwap,
                              LocalDateTime timestamp, double momentum, double volatility,
                              double volumeProfile, boolean significant) {
            this.symbol = symbol;
            this.price = price;
            this.volume = volume;
            this.vwap = vwap;
            this.timestamp = timestamp;
            this.momentum = momentum;
            this.volatility = volatility;
            this.volumeProfile = volumeProfile;
            this.significant = significant;
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public double getVolume() { return volume; }
        public double getVwap() { return vwap; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public double getMomentum() { return momentum; }
        public double getVolatility() { return volatility; }
        public double getVolumeProfile() { return volumeProfile; }
        public boolean isSignificant() { return significant; }
        
        @Override
        public String toString() {
            return String.format("SimpleMarketData{symbol='%s', price=%.5f, volume=%.2f, significant=%s}", 
                               symbol, price, volume, significant);
        }
    }
    
    /**
     * Buffer بسيط للبيانات
     */
    private static class SimpleMarketBuffer {
        private double lastPrice = 0.0;
        private double totalVolume = 0.0;
        private int tickCount = 0;
        private final double[] recentPrices = new double[10];
        private int priceIndex = 0;
        private LocalDateTime lastUpdate;
        
        private static final int MAX_HISTORY = 10;
        
        public SimpleMarketBuffer(String symbol) {
            // constructor parameter for identification only
        }
        
        public void addTick(double price, double volume, double vwap, LocalDateTime timestamp) {
            lastPrice = price;
            totalVolume += volume;
            tickCount++;
            lastUpdate = timestamp;
            
            // إضافة للتاريخ
            recentPrices[priceIndex] = price;
            priceIndex = (priceIndex + 1) % MAX_HISTORY;
        }
        
        public boolean hasRecentData() {
            return tickCount > 0;
        }
        
        public double getLastPrice() {
            return lastPrice;
        }
        
        public double getAverageVolume() {
            if (tickCount == 0) return 0.0;
            return totalVolume / tickCount;
        }
        
        public int getTickCount() {
            return tickCount;
        }
        
        public double[] getRecentPrices(int count) {
            int actualCount = Math.min(count, Math.min(tickCount, MAX_HISTORY));
            double[] result = new double[actualCount];
            
            for (int i = 0; i < actualCount; i++) {
                int index = (priceIndex - actualCount + i + MAX_HISTORY) % MAX_HISTORY;
                result[i] = recentPrices[index];
            }
            
            return result;
        }
        
        public void removeOldData(LocalDateTime cutoffTime) {
            if (lastUpdate != null && lastUpdate.isBefore(cutoffTime)) {
                // إعادة تعيين البيانات القديمة
                tickCount = 0;
                totalVolume = 0.0;
                lastPrice = 0.0;
            }
        }
        
        public boolean isEmpty() {
            return tickCount == 0;
        }
    }
} 