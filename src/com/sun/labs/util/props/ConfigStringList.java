package com.sun.labs.util.props;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A list of string property.
 *
 * @author Holger Brandl
 * @see ConfigurationManager
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ConfigProperty
public @interface ConfigStringList {

    /**
     * A default list of <code>Configurable</code>s used to configure this component list given the case that no
     * component list was defined (via xml or during runtime).
     */
    String[] defaultList() default {};

    String[] range() default {};

    boolean mandatory() default true;
}
