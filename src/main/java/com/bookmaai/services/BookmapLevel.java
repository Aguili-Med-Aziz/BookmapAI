package com.bookmaai.services;

/**
 * 🎯 Bookmap Level - مستوى واحد من Bookmap
 * 
 * يمثل مستوى دعم أو مقاومة مستخرج من أدوات Bookmap
 */
public class BookmapLevel {
    private final double price;
    private final String type;
    private final double strength;
    private final String description;
    private final long timestamp;
    
    public BookmapLevel(double price, String type, double strength, String description) {
        this.price = price;
        this.type = type;
        this.strength = strength;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters
    public double getPrice() { return price; }
    public String getType() { return type; }
    public double getStrength() { return strength; }
    public String getDescription() { return description; }
    public long getTimestamp() { return timestamp; }
    
    /**
     * تحديد إذا كان المستوى مقاومة أم دعم
     */
    public boolean isResistance() {
        return type.contains("RESISTANCE");
    }
    
    public boolean isSupport() {
        return type.contains("SUPPORT");
    }
    
    /**
     * حساب المسافة من السعر الحالي
     */
    public double getDistanceFrom(double currentPrice) {
        return Math.abs(price - currentPrice) / currentPrice;
    }
    
    /**
     * تحديد أولوية المستوى (كلما قل الرقم، زادت الأولوية)
     */
    public int getPriority() {
        if (type.contains("POC")) return 1;          // أعلى أولوية
        if (type.contains("VWAP")) return 2;         
        if (type.contains("HEATMAP")) return 3;      
        if (type.contains("ICEBERG")) return 4;      
        if (type.contains("ABSORPTION")) return 5;   
        if (type.contains("VAH") || type.contains("VAL")) return 6;
        if (type.contains("HVN")) return 7;          
        return 8; // أقل أولوية
    }
    
    @Override
    public String toString() {
        return String.format("%.5f (%s - %.1f%% strength)", price, type, strength * 100);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BookmapLevel other = (BookmapLevel) obj;
        return Double.compare(other.price, price) == 0 && type.equals(other.type);
    }
    
    @Override
    public int hashCode() {
        return Double.hashCode(price) + type.hashCode();
    }
} 