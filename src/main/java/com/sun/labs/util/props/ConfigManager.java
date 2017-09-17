package com.sun.labs.util.props;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation that denotes where the ConfigurationManager should be stored.
 *
 * Use this sparingly.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ConfigProperty
public @interface ConfigManager {

}
