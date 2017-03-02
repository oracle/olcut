package com.sun.labs.util.props;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A string property.
 *
 * @author Holger Brandl
 * @see ConfigurationManager
 */
@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ConfigProperty
public @interface ConfigString {

    public static final String NOT_DEFINED = "nullnullnull";


    String defaultValue() default NOT_DEFINED; // this default value will be mapped to zero by the configuration manager


    String[] range() default {};


    boolean mandatory() default true;
}
