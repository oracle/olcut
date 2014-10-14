package com.sun.labs.util.props;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A file property.
 *
 * @see ConfigurationManager
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ConfigProperty
public @interface ConfigFile {

    String fileName();

    boolean mandatory() default true;
    
    boolean exists() default true;
    
    boolean canRead() default true;
    
    boolean canWrite() default true;
    
    boolean isDirectory() default false;
    
}
