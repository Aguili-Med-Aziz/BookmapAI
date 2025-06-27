
package velox.api.layer1.data;

public interface DepthDataListener {
    void onDepth(boolean isBid, int price, int size);
}
