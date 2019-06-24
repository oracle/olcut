package com.oracle.labs.mlrg.olcut.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that denotes where the ConfigurationManager should be stored.
 *
 * Use this sparingly.
 *
 * ConfigManager should not be applied to a static field.
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ConfigProperty
public @interface ConfigManager { }
