package com.sun.labs.util.props;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation that denotes where the ConfigurationManager should be stored.
 *
 * You should never need to use this, it's only for testing.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ConfigProperty
public @interface ConMan {

}
