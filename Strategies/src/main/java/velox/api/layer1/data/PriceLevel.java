package velox.api.layer1.data;

public class PriceLevel {
    private double price;
    private int size;
    private long timestamp;

    public PriceLevel(double price, int size, long timestamp) {
        this.price = price;
        this.size = size;
        this.timestamp = timestamp;
    }

    public double getPrice() {
        return price;
    }

    public int getSize() {
        return size;
    }

    public long getTimestamp() {
        return timestamp;
    }   
    
}
