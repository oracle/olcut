package com.sun.labs.util.props;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A configuration type for Java enums.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ConfigProperty
public @interface ConfigEnum {
    Class<? extends Enum> type();

    String defaultValue() default "";
    
    boolean mandatory() default true;

}
