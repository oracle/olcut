package com.sun.labs.util.props;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can be added directly to variables that should be
 * configurable. We don't require a default value, as we can assign that
 * default during construction by declaring the annotated value with a value.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ConfigProperty
public @interface Config {

    /**
     * By default, configuration variables aren't mandatory.
     */
    boolean mandatory() default false;

}
