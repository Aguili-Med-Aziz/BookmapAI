package velox.api.layer1.simpledemo;

import velox.api.layer1.data.InstrumentInfo;

public class SimpleDemoInstrumentInfo extends InstrumentInfo {
    public SimpleDemoInstrumentInfo(String symbol, String description, String exchange) {
        super(symbol, description, exchange, 0.01, 0.01, "USD", false);
    }
} 