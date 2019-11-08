package com.oracle.labs.mlrg.olcut.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can be added directly to variables that should be
 * configurable. We don't require a default value, as we can assign that
 * default during construction by declaring the annotated field with a value.
 *
 * Config should not be applied to a static field.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ConfigProperty
public @interface Config {

    /**
     * By default, configuration variables aren't mandatory.
     * @return true if the field must appear in all configurations of this object.
     */
    boolean mandatory() default false;

    /**
     * A plain text description of the field, used to describe the configurable parts of an object.
     * @return A description of this field.
     */
    String description() default "";

    /**
     * By default fields aren't redacted from saved configs or provenance.
     * @return true if the field must be redacted from saved configs or provenance.
     */
    boolean redact() default false;

}
