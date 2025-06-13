package com.bookmaai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 🧪 Basic System Test
 * 
 * اختبار أساسي للتأكد من تشغيل النظام
 */
@SpringBootTest
public class BookmapAIApplicationTest {

    @Test
    public void contextLoads() {
        // إذا وصل هنا، فالنظام يعمل بنجاح
        System.out.println("✅ Bookmap AI System loaded successfully!");
    }
    
    @Test 
    public void systemInfo() {
        // اختبار معلومات النظام
        String javaVersion = System.getProperty("java.version");
        int processors = Runtime.getRuntime().availableProcessors();
        
        System.out.println("📊 System Info:");
        System.out.println("   Java Version: " + javaVersion);
        System.out.println("   Processors: " + processors);
        
        // التأكد من Java 21
        assert javaVersion.startsWith("21") : "Expected Java 21, but got: " + javaVersion;
        
        System.out.println("✅ System requirements verified!");
    }
} 