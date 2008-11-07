package com.sun.labs.util.props;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A tag which superclasses all sphinx property annotations. Because there is no real inheritance for annotations all
 * child classes are annotated by this general property annotation.
 *
 * @author Holger Brandl
 * @see ConfigComponent
 * @see ConfigInteger
 * @see ConfigComponentList
 * @see ConfigDouble
 * @see ConfigBoolean
 * @see ConfigString
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigProperty {

}
