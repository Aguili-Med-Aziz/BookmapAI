
package velox.api.layer1.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Layer1StrategyName {
    String value();
}
