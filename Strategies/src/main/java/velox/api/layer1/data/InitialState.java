package velox.api.layer1.data;

public class InitialState {
    private final long timestamp;
    private final double lastPrice;
    private final int lastSize;
    
    public InitialState(long timestamp, double lastPrice, int lastSize) {
        this.timestamp = timestamp;
        this.lastPrice = lastPrice;
        this.lastSize = lastSize;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public double getLastPrice() {
        return lastPrice;
    }
    
    public int getLastSize() {
        return lastSize;
    }
} 