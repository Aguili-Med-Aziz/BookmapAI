package velox.api.layer1.data;

public interface TradeDataListener {
    void onTrade(double price, int size, TradeInfo tradeInfo);
} 