package velox.api.layer1.simpledemo.actions;

public class PriceLevel {
    private double price;
    private int size;

    public PriceLevel(double price, int size) {
        this.price = price;
        this.size = size;
    }

    public double getPrice() {
        return price;
    }

    public int getSize() {
        return size;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
