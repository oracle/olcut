/*
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

/**
 * <p>
 * Indicates that a problem occurred while setting one or more properties for this component. This includes errors as
 * improper type for component(-lists) properties, out-of-range-problems for double-, int- and ranged string-properties,
 * instantiation errors and undefined mandatory properties.
 * </p>
 * <p>
 * This exception is instantiable only by the configuration management classes itself. In order to indicate problems
 * within Configurable.newProperties which are not coped by types or ranges (eg file-not-found, complex configuration
 * logic problems, etc.) <code>PropertyException</code> (which superclasses this class) can be used.
 * </p>
 * <p>
 * The intention of the class is to make a clear distinction between core configuration errors and high level user
 * specific problems.
 * </p>
 */
public final class InternalConfigurationException extends PropertyException {

    InternalConfigurationException(String instanceName, String propertyName, String msg) {
        super(instanceName, propertyName, msg);
    }


    InternalConfigurationException(Throwable cause, String instanceName, String propertyName, String msg) {
        super(cause, instanceName, propertyName, msg);
    }
}
