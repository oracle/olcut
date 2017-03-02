package com.sun.labs.util.props;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A logical property.
 *
 * @author Holger Brandl
 * @see ConfigurationManager
 */
@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ConfigProperty
public @interface ConfigBoolean {

    boolean defaultValue();

    boolean isNotDefined() default false;
}
