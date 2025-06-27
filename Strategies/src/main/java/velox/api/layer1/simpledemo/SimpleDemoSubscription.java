package velox.api.layer1.simpledemo;

import velox.api.layer1.data.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimpleDemoSubscription {
    private final CopyOnWriteArrayList<SimpleDemoSubscriptionListener> listeners = new CopyOnWriteArrayList<>();
    
    public interface SimpleDemoSubscriptionListener {
        void onTrade(double price, int size, TradeInfo tradeInfo);
        void onDepth(boolean isBid, int price, int size);
        void onTimestamp(long timestamp);
    }
    
    public void addListener(SimpleDemoSubscriptionListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(SimpleDemoSubscriptionListener listener) {
        listeners.remove(listener);
    }
    
    public void notifyTrade(double price, int size, TradeInfo tradeInfo) {
        for (SimpleDemoSubscriptionListener listener : listeners) {
            listener.onTrade(price, size, tradeInfo);
        }
    }
    
    public void notifyDepth(boolean isBid, int price, int size) {
        for (SimpleDemoSubscriptionListener listener : listeners) {
            listener.onDepth(isBid, price, size);
        }
    }
    
    public void notifyTimestamp(long timestamp) {
        for (SimpleDemoSubscriptionListener listener : listeners) {
            listener.onTimestamp(timestamp);
        }
    }
} 