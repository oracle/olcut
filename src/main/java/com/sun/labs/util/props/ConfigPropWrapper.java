package com.sun.labs.util.props;

import java.lang.reflect.Proxy;

/**
 * Wraps annotations
 *
 * @author Holger Brandl
 */
public class ConfigPropWrapper {

    private final Proxy annotation;


    public ConfigPropWrapper(Proxy annotation) {
        this.annotation = annotation;
    }


    public Proxy getAnnotation() {
        return annotation;
    }
}
