package com.oracle.labs.mlrg.olcut.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that denotes where the String value of the component's name should be stored.
 *
 * ConfigurableName should not be applied to a static field.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ConfigProperty
public @interface ConfigurableName { }
