package velox.api.layer1.simpledemo;

import velox.api.layer1.data.*;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator.GraphType;

public class SimpleDemoAdapter implements CustomModule {
    private final String alias;
    private final InstrumentInfo info;
    private final Api api;
    
    public SimpleDemoAdapter(String alias, InstrumentInfo info, Api api) {
        this.alias = alias;
        this.info = info;
        this.api = api;
    }
    
    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        // Implementation
    }
    
    @Override
    public void stop() {
        // Implementation
    }
} 