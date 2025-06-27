package velox.api.layer1.data;

public interface CustomModule {
    void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState);
    void stop();
} 