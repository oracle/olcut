/*
 * 
 * Copyright 1999-2004 Carnegie Mellon University.  
 * Portions Copyright 2004 Sun Microsystems, Inc.  
 * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 *
 */
package com.oracle.labs.mlrg.olcut.config;

import java.io.IOException;

/**
 * Defines the interface that must be implemented by any configurable component.  The life cycle of a
 * {@link Configurable} is as follows:
 * <p/>
 * <ul><li> <b>Class Parsing</b> The class file is parsed in order to determine all its configurable properties.  These
 * are defined using {@link Config} annotations on fields. Only types defined in {@link FieldType} are recognised. Only
 * names of annotated properties will be allowed by the configuration system later on. Optionally the user can
 * annotate a {@link String} field with {@link ConfigurableName} which will have the name from the xml file written
 * into it. If required the {@link ConfigurationManager} can be stored by annotating an appropriate field with
 * {@link ConfigManager}.</li>
 * <p/>
 * <li> <b>Construction</b> - The (empty, optionally private) constructor is called in order to instantiate the component.
 * Typically the constructor does little, if any work, since the component has not been configured yet. </li>
 * <p/>
 * <li> <b>Configuration</b> - Shortly after instantiation, the component's fields are written by inserting parsed
 * values from a {@link PropertySheet}. The PropertySheet is usually derived from an external configuration file, but
 * can be constructed programmatically as a {@link java.util.Map} from String to Object. If some properties
 * defined for a component does not fulfill the property definition given by the annotation (type, range, etc.) a
 * <code>PropertyException</code> is thrown. </li>
 * <p/>
 * <li> <b>Post Config</b> - After the fields have been initialised, the system calls the {@link Configurable#postConfig()}
 * method. There other setup can be performed, such as deserialising types which are not configurable. </li>
 * </ul>
 */
public interface Configurable {

    /**
     * Uses the configured variables, which are set up by the configuration
     * system before this method is called, to do any post variable configuration
     * setup.
     * @throws PropertyException
     * @throws IOException As it may be a remote component, and RemoteException is a subclass of IOException.
     */
    default public void postConfig() throws PropertyException, IOException {
        
    }

}
