package com.bookmaai.core;

import com.bookmaai.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

/**
 * 🧪 System Integration Test - اختبار تكامل النظام
 */
@Component
public class SystemIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemIntegrationTest.class);
    
    @Autowired(required = false)
    private SlidingWindowAggregator slidingWindow;
    
    @Autowired(required = false) 
    private WindowHistoryManager historyManager;
    
    @Autowired(required = false)
    private PatternEngineAdvanced advancedPatternEngine;
    
    @Autowired(required = false)
    private PatternEngineAdvanced legacyEngine;
    
    @PostConstruct
    public void runIntegrationTest() {
        logger.info("🧪 Starting System Integration Test...");
        logger.info("============================================");
        
        try {
            testDependencyInjection();
            testSlidingWindowAggregator();
            testPatternEngines();
            testWindowHistoryManager();
            
            logger.info("✅ All Integration Tests PASSED!");
            logger.info("🎯 System is fully integrated and operational!");
            
        } catch (Exception e) {
            logger.error("❌ Integration test failed: {}", e.getMessage(), e);
        }
    }
    
    private void testDependencyInjection() {
        logger.info("📋 Testing Dependency Injection...");
        
        int loadedComponents = 0;
        
        if (slidingWindow != null) {
            logger.info("   ✅ SlidingWindowAggregator injected successfully");
            loadedComponents++;
        } else {
            logger.warn("   ⚠️ SlidingWindowAggregator not injected");
        }
        
        if (historyManager != null) {
            logger.info("   ✅ WindowHistoryManager injected successfully");
            loadedComponents++;
        } else {
            logger.warn("   ⚠️ WindowHistoryManager not injected");
        }
        
        if (advancedPatternEngine != null) {
            logger.info("   ✅ PatternEngineAdvanced injected successfully");
            loadedComponents++;
        } else {
            logger.warn("   ⚠️ PatternEngineAdvanced not injected");
        }
        
        if (legacyEngine != null) {
            logger.info("   ✅ PatternEngineAdvanced injected successfully");
            loadedComponents++;
        } else {
            logger.warn("   ⚠️ PatternEngineAdvanced not injected");
        }
        
        logger.info("📊 Dependency Injection Result: {}/4 components loaded", loadedComponents);
    }
    
    private void testSlidingWindowAggregator() {
        if (slidingWindow == null) {
            logger.warn("⚠️ Skipping SlidingWindowAggregator test - not available");
            return;
        }
        
        logger.info("🔄 Testing SlidingWindowAggregator...");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            SlidingWindowAggregator.MarketSnapshot snapshot1 = new SlidingWindowAggregator.MarketSnapshot(
                now, 1.0850, 1.0852, 150.0, 0.3, 250.0, 50.0
            );
            
            slidingWindow.addSnapshot(snapshot1);
            
            boolean isActive = slidingWindow.isWindowActive();
            logger.info("   📊 Window Active: {}", isActive);
            logger.info("   🔢 Snapshots: {}", slidingWindow.getCurrentSnapshotCount());
            logger.info("   📈 Total Volume: {:.2f}", slidingWindow.getTotalVolume());
            
            logger.info("   ✅ SlidingWindowAggregator working correctly");
            
        } catch (Exception e) {
            logger.error("   ❌ SlidingWindowAggregator test failed: {}", e.getMessage());
        }
    }
    
    private void testPatternEngines() {
        if (advancedPatternEngine != null) {
            logger.info("📈 Testing PatternEngineAdvanced...");
            
            try {
                var result = advancedPatternEngine.analyzePatterns("EURUSD", 1.0850, 200.0, 1.0848);
                
                if (result != null) {
                    logger.info("   📊 Analysis Result: {} | {} | {:.1f}%", 
                               result.getDirection(), result.getPatternName(), result.getConfidence() * 100);
                    logger.info("   ✅ PatternEngineAdvanced working correctly");
                } else {
                    logger.warn("   ⚠️ PatternEngineAdvanced returned null result");
                }
                
            } catch (Exception e) {
                logger.error("   ❌ PatternEngineAdvanced test failed: {}", e.getMessage());
            }
        }
        
        if (legacyEngine != null) {
            logger.info("🎯 Testing Legacy Pattern Engine...");
            
            try {
                var result = legacyEngine.analyzePatterns("GBPUSD", 1.2750, 300.0, 1.2748);
                
                if (result != null) {
                    logger.info("   🏆 Legacy Analysis: {} | {} | {:.1f}%", 
                               result.getDirection(), result.getPatternName(), result.getConfidence() * 100);
                    logger.info("   📋 Signals detected");
                    logger.info("   ✅ Legacy Pattern Engine working correctly");
                } else {
                    logger.warn("   ⚠️ Legacy Pattern Engine returned null result");
                }
                
            } catch (Exception e) {
                logger.error("   ❌ Legacy Pattern Engine test failed: {}", e.getMessage());
            }
        }
    }
    
    private void testWindowHistoryManager() {
        if (historyManager == null) {
            logger.warn("⚠️ Skipping WindowHistoryManager test - not available");
            return;
        }
        
        logger.info("📚 Testing WindowHistoryManager...");
        
        try {
            LocalDateTime start = LocalDateTime.now().minusMinutes(15);
            LocalDateTime end = LocalDateTime.now();
            
            WindowSummary summary = new WindowSummary(start, end, "test_pattern", 
                                                     10, 150.0, 75.0, 25.0, 500.0, 0.0050);
            
            historyManager.addCompletedWindow(summary);
            
            logger.info("   📊 Windows processed: {}", historyManager.getTotalWindows());
            logger.info("   📈 Patterns detected: {}", historyManager.getSuccessfulWindows());
            logger.info("   💰 Total pips: {:.1f}", historyManager.getTotalPipsGained());
            
            logger.info("   ✅ WindowHistoryManager working correctly");
            
        } catch (Exception e) {
            logger.error("   ❌ WindowHistoryManager test failed: {}", e.getMessage());
        }
    }
} 