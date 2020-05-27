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

/** Indicates that a problem occurred while setting one or more properties for this component */
public class PropertyException extends RuntimeException {

    private final String instanceName;
    private final String propertyName;
    private final String msg;

    /**
     * Creates a new property exception.
     *
     * @param instanceName The component this exception is related to.  (or <code>null</code> if unknown)
     * @param msg          a description of the problem.
     */
    public PropertyException(String instanceName, String msg) {
        this(null, instanceName, null, msg);
    }

    /**
     * Creates a new property exception.
     *
     * @param instanceName The component this exception is related to.  (or <code>null</code> if unknown)
     * @param propertyName The name of the component-property which the problem is related. (or <code>null</code> if
     *                     unknown)
     * @param msg          a description of the problem.
     */
    public PropertyException(String instanceName, String propertyName, String msg) {
        this(null, instanceName, propertyName, msg);
    }


    /**
     * Creates a new property exception.
     *
     * @param cause        The cause of exception. (or <code>null</code> if unknown)
     * @param msg          a description of the problem.
     */
    public PropertyException(Throwable cause, String msg) {
        this(cause,null,null,msg);
    }

    /**
     * Creates a new property exception.
     *
     * @param cause        The cause of exception. (or <code>null</code> if unknown)
     * @param instanceName The component this exception is related to.  (or <code>null</code> if unknown)
     * @param msg          a description of the problem.
     */
    public PropertyException(Throwable cause, String instanceName, String msg) {
        this(cause,instanceName,null,msg);
    }

    /**
     * Creates a new property exception.
     *
     * @param cause        The cause of exception. (or <code>null</code> if unknown)
     * @param instanceName The component this exception is related to.  (or <code>null</code> if unknown)
     * @param propertyName The name of the component-property which the problem is related. (or <code>null</code> if
     *                     unknown)
     * @param msg          a description of the problem.
     */
    public PropertyException(Throwable cause, String instanceName, String propertyName, String msg) {
        super(cause);

        this.instanceName = instanceName;
        this.propertyName = propertyName;
        if (cause != null) {
            this.msg = msg + " (caused by " + cause.getMessage() + ")";
        } else {
            this.msg = msg;
        }
    }

    /** @return Returns the msg. */
    public String getMsg() {
        return msg;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if ((instanceName != null) && !instanceName.isEmpty()) {
            sb.append("Component: ");
            sb.append(instanceName);
            sb.append(", ");
        }
        if ((propertyName != null) && !propertyName.isEmpty()) {
            sb.append("Property: ");
            sb.append(propertyName);
            sb.append(", ");
        }
        sb.append(msg);
        return sb.toString();
    }

    /**
     * Retrieves the name of the offending property
     *
     * @return the name of the offending property
     */
    public String getProperty() {
        return propertyName;
    }

    /**
     * Returns a string representation of this object
     *
     * @return the string representation of the object.
     */
    public String toString() {
        if (instanceName != null) {
            if (propertyName != null) {
                return "Property Exception component:'" + instanceName + "' property:'" + propertyName + "' - " + msg + '\n'
                        + super.toString();
            } else {
                return "Property Exception component:'" + instanceName + "' - " + msg + '\n' + super.toString();
            }
        } else {
            return "Property Exception - " + msg + '\n' + super.toString();
        }

    }
}
