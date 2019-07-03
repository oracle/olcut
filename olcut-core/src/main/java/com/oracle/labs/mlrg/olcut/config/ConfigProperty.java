package com.oracle.labs.mlrg.olcut.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A tag which superclasses all olcut property annotations. Because there is no real inheritance for annotations all
 * child classes are annotated by this general property annotation.
 *
 * @see Config
 * @see ConfigurableName
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigProperty { }
