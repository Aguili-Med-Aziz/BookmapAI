package com.bookmaai.services;

/**
 * 🔄 Pattern Carry State - حالة متابعة الأنماط عبر النوافذ
 * 
 * يخزن حالة تطور النمط الجزئية عبر النوافذ الزمنية:
 * - stage: رقم المرحلة الحالية (0 = لم يبدأ، 1 = تراكم جزئي، 2 = تلاعب/اختبار، 3 = توزيع/استكمال)
 * - stageDescription: وصف نصي للمرحلة
 * - confidence: مستوى الثقة في المرحلة الحالية
 * - lastUpdateWindow: آخر نافذة تم تحديث الحالة فيها
 */
public class PatternCarryState {
    private int stage;
    private String stageDescription;
    private double confidence;
    private long lastUpdateWindow;
    
    public PatternCarryState() {
        this.stage = 0;
        this.stageDescription = "لم يبدأ";
        this.confidence = 0.0;
        this.lastUpdateWindow = 0;
    }
    
    public int getStage() { 
        return stage; 
    }
    
    public String getStageDescription() { 
        return stageDescription; 
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public long getLastUpdateWindow() {
        return lastUpdateWindow;
    }
    
    public void setStage(int stage) {
        this.stage = stage;
        this.lastUpdateWindow = System.currentTimeMillis();
        
        switch(stage) {
            case 0: 
                stageDescription = "لم يبدأ";
                confidence = 0.0;
                break;
            case 1: 
                stageDescription = "مرحلة تجميع جزئي";
                confidence = 0.3;
                break;
            case 2: 
                stageDescription = "مرحلة تلاعب/اختبار";
                confidence = 0.6;
                break;
            case 3: 
                stageDescription = "مرحلة التوزيع/استكمال";
                confidence = 0.8;
                break;
            default: 
                stageDescription = "غير معروف";
                confidence = 0.0;
                break;
        }
    }
    
    public void updateConfidence(double newConfidence) {
        this.confidence = Math.max(0.0, Math.min(1.0, newConfidence));
        this.lastUpdateWindow = System.currentTimeMillis();
    }
    
    /**
     * إعادة تعيين الحالة لبداية نمط جديد
     */
    public void reset() {
        this.stage = 0;
        this.stageDescription = "لم يبدأ";
        this.confidence = 0.0;
        this.lastUpdateWindow = System.currentTimeMillis();
    }
    
    /**
     * التحقق من انتهاء صلاحية الحالة (مثلاً بعد ساعة)
     */
    public boolean isExpired(long currentTime, long expirationMs) {
        return (currentTime - lastUpdateWindow) > expirationMs;
    }
    
    @Override
    public String toString() {
        return String.format("PatternCarryState{stage=%d, description='%s', confidence=%.2f}", 
                           stage, stageDescription, confidence);
    }
} 