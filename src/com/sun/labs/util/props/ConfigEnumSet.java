package com.sun.labs.util.props;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A set of enums property.
 *
 * @see ConfigurationManager
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ConfigProperty
public @interface ConfigEnumSet {

    /**
     * The type of the enum.
     */
    Class<? extends Enum> type();

    /**
     * A default list of <code>enums</code>s used to configure this component.
     */
    String[] defaultList() default {};

    boolean mandatory() default false;
}
