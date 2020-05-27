/*
 * Copyright 1999-2004 Carnegie Mellon University.
 * Portions Copyright 2004 Sun Microsystems, Inc.
 * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
 * Copyright (c) 2004-2020, Oracle and/or its affiliates.
 *
 * Licensed under the 2-clause BSD license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.oracle.labs.mlrg.olcut.config;

import com.oracle.labs.mlrg.olcut.config.property.Property;

import java.io.IOException;

/**
 * Defines the interface that must be implemented by any configurable component.  The life cycle of a
 * {@link Configurable} is as follows:
 * <ul><li> <b>Class Parsing</b> The class file is parsed in order to determine all its configurable properties.  These
 * are defined using {@link Config} annotations on fields. Only types defined in {@link FieldType} are recognised. Only
 * names of annotated properties will be allowed by the configuration system later on. Optionally the user can
 * annotate a {@link String} field with {@link ConfigurableName} which will have the name from the xml file written
 * into it. If required the {@link ConfigurationManager} can be stored by annotating an appropriate field with
 * {@link ConfigManager}.</li>
 * <li> <b>Construction</b> - The (empty, optionally private) constructor is called in order to instantiate the component.
 * Typically the constructor does little, if any work, since the component has not been configured yet. </li>
 * <li> <b>Configuration</b> - Shortly after instantiation, the component's fields are written by inserting parsed
 * values from a {@link PropertySheet}. The PropertySheet is usually derived from an external configuration file, but
 * can be constructed programmatically as a {@link java.util.Map} from String to {@link Property}.
 * If some properties
 * defined for a component does not fulfill the property definition given by the annotation (type, range, etc.) a
 * <code>PropertyException</code> is thrown. </li>
 * <li> <b>Post Config</b> - After the fields have been initialised, the system calls the {@link Configurable#postConfig()}
 * method. There other setup can be performed, such as deserialising types which are not configurable. </li>
 * </ul>
 */
public interface Configurable {

    /**
     * Uses the configured variables, which are set up by the configuration
     * system before this method is called, to do any post variable configuration
     * setup.
     * @throws PropertyException if the object is misconfigured or violates class invariants.
     * @throws IOException As it may be a remote component, and RemoteException is a subclass of IOException.
     */
    default public void postConfig() throws PropertyException, IOException {
        
    }

}
