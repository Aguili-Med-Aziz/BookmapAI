package velox.bookmap.pu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bookmap API Layer1Attachable annotation
 * This is the actual annotation that Bookmap expects for entry points
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Layer1Attachable {
} 