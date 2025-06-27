package velox.api.layer1.simplified;

public interface CustomModule {
    void initialize(String alias, Object instrumentInfo, Object api, Object initialState);
    void stop();
}
