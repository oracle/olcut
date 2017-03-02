package com.sun.labs.util.props;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A component property.
 *
 * @author Holger Brandl
 * @see ConfigurationManager
 */
@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ConfigProperty
public @interface ConfigComponent {

    Class<? extends Component> type();


    Class<? extends Component> defaultClass() default Component.class;


    boolean mandatory() default true;
}
