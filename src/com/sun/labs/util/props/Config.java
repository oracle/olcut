package com.sun.labs.util.props;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation that can be added directly to variables that should be
 * configurable. Unlike the other configuration annotations, we won't require a
 * default value, as we can assign that default during construction by declaring
 * the annotated value with a value.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ConfigProperty
public @interface Config {

    /**
     * By default, configuration variables are mandatory.
     */
    boolean mandatory() default true;

}
