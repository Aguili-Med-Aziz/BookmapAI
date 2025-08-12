package velox.api.layer1.data;

/**
 * Stub class for InstrumentInfo
 * This is a placeholder for the actual Bookmap API
 */
public class InstrumentInfo {
    private String symbol;
    private String exchange;
    
    public InstrumentInfo(String symbol, String exchange) {
        this.symbol = symbol;
        this.exchange = exchange;
    }
    
    public String getSymbol() { return symbol; }
    public String getExchange() { return exchange; }
} 