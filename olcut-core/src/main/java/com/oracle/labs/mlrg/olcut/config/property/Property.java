package com.oracle.labs.mlrg.olcut.config.property;

import java.io.Serializable;

/**
 * Tag interface for the types extracted from a configuration file.
 *
 * Property implementations should be immutable and final.
 *
 * One day this interface will be sealed, at the moment it is implemented
 * by {@link SimpleProperty}, {@link ListProperty}, and {@link MapProperty}.
 * Other subclasses will not be recognised by the configuration processing machinery.
 */
public interface Property extends Serializable {
    public Property copy();
}
