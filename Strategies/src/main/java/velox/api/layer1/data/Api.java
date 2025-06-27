package velox.api.layer1.data;

public interface Api {
    void addTradeDataListener(TradeDataListener listener);
    void addTimeListener(TimeListener listener);
    void addDepthDataListener(DepthDataListener listener);
    void removeTradeDataListener(TradeDataListener listener);
    void removeTimeListener(TimeListener listener);
    void removeDepthDataListener(DepthDataListener listener);
} 