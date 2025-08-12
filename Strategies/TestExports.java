import com.bookmaai.core.DataExportManager;
import com.bookmaai.core.RealDataSlidingWindow;
import java.util.ArrayList;
import java.util.List;

public class TestExports {
    public static void main(String[] args) {
        System.out.println("🧪 Testing Exports Functionality...");
        
        // Test 1: DataExportManager initialization
        System.out.println("\n1. Testing DataExportManager initialization:");
        DataExportManager exportManager = new DataExportManager();
        
        // Test 2: RealDataSlidingWindow initialization
        System.out.println("\n2. Testing RealDataSlidingWindow initialization:");
        RealDataSlidingWindow slidingWindow = new RealDataSlidingWindow();
        
        // Test 3: Add some test data
        System.out.println("\n3. Adding test market data:");
        slidingWindow.addRealData("EURUSD", 1.0850, 100.0, "BUY");
        slidingWindow.addRealData("EURUSD", 1.0851, 150.0, "SELL");
        slidingWindow.addRealData("EURUSD", 1.0852, 200.0, "BUY");
        slidingWindow.addRealData("EURUSD", 1.0853, 175.0, "SELL");
        slidingWindow.addRealData("EURUSD", 1.0854, 125.0, "BUY");
        
        // Add more data to trigger export threshold
        for (int i = 0; i < 10; i++) {
            slidingWindow.addRealData("EURUSD", 1.0850 + (i * 0.0001), 100.0 + i, "BUY");
        }
        
        // Test 4: Manual export trigger
        System.out.println("\n4. Triggering manual export:");
        slidingWindow.exportData();
        
        // Test 5: Wait a moment and check
        try {
            Thread.sleep(2000);
            System.out.println("\n5. Export test completed. Check for 'exports' folder in current directory.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Cleanup
        slidingWindow.shutdown();
        System.out.println("\n✅ Test completed!");
    }
}
