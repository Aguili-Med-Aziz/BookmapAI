package velox.bookmap.pu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bookmap API Layer1ApiVersion annotation
 * This is the actual annotation that Bookmap expects
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Layer1ApiVersion {
    String value() default "1.0";
} 