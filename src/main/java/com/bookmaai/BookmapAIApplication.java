package com.bookmaai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 🚀 BookmapAI Application Main Class
 * 
 * نقطة دخول التطبيق الرئيسية مع دعم:
 * - تحليل الأنماط المتقدم مع 12 أداة
 * - النافذة المتحركة والتعلم التكيفي
 * - إدارة التضارب والمخاطر
 * - إشعارات التلجرام المتقدمة
 */
@SpringBootApplication
@EnableScheduling
public class BookmapAIApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Starting BookmapAI Trading System...");
        
        try {
            SpringApplication.run(BookmapAIApplication.class, args);
            System.out.println("✅ BookmapAI System Started Successfully!");
            System.out.println("📊 Ready for advanced pattern analysis with 12 Bookmap tools");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to start BookmapAI: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 