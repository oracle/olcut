package com.oracle.labs.mlrg.olcut.config;

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
 *
 * Option should not be applied to a static field.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Option {

    char charName() default EMPTY_CHAR;

    String longName();

    String usage();

    public static char EMPTY_CHAR = '\0';
    public static char SPACE_CHAR = ' ';

}
