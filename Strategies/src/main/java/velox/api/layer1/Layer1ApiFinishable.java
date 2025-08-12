package velox.api.layer1;

/**
 * Stub interface for Layer1ApiFinishable
 * This interface marks classes that need cleanup when the Layer1 API shuts down
 */
public interface Layer1ApiFinishable {
    /**
     * Called when the strategy should clean up resources
     */
    void finish();
} 