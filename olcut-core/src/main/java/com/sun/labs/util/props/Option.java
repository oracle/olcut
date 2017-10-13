package com.sun.labs.util.props;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can be added directly to variables that are command line arguments.
 *
 * Defaults to the value set during construction. Supports all the types supported
 * by the {@link Config} annotation.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Option {

    char charName() default '\0';

    String longName();

    String usage();

    /**
     * By default, options aren't mandatory.
     */
    boolean mandatory() default false;

}
