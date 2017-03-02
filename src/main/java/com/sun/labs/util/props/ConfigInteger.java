package com.sun.labs.util.props;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An integer property.
 *
 * @author Holger Brandl
 * @see ConfigurationManager
 */
@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ConfigProperty
public @interface ConfigInteger {

    public static final int NOT_DEFINED = -918273645;


    int defaultValue() default NOT_DEFINED;


    int[] range() default {-Integer.MAX_VALUE, Integer.MAX_VALUE};


    boolean mandatory() default true;
}
