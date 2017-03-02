package com.sun.labs.util.props;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * * A double property.
 *
 * @author Holger Brandl
 * @see ConfigurationManager
 */
@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ConfigProperty
public @interface ConfigDouble {

    public static final double NOT_DEFINED = -918273645.12345;


    double defaultValue() default NOT_DEFINED;


    double[] range() default {-Double.MAX_VALUE, Double.MAX_VALUE};


    boolean mandatory() default true;
}
