package velox.api.layer1.data;

/**
 * Stub class for TradeInfo
 * This is a placeholder for the actual Bookmap API
 */
public class TradeInfo {
    private String tradeId;
    private long timestamp;
    
    public TradeInfo(String tradeId, long timestamp) {
        this.tradeId = tradeId;
        this.timestamp = timestamp;
    }
    
    public String getTradeId() { return tradeId; }
    public long getTimestamp() { return timestamp; }
} 