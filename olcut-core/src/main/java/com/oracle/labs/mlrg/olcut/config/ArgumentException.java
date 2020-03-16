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

/** Indicates that a problem occurred while parsing arguments. */
public class ArgumentException extends RuntimeException {

    private final String argumentName;
    private final String otherArgumentName;
    protected final String msg;

    /**
     * Creates a new argument exception.
     *
     * @param argumentName The component this exception is related to.
     * @param msg          a description of the problem.
     */
    public ArgumentException(String argumentName, String msg) {
        this(null, argumentName, null, msg);
    }

    /**
     * Creates a new argument exception.
     *
     * @param argumentName The argument this exception is related to.
     * @param otherArgumentName The name of the conflicting argument.
     * @param msg          a description of the problem.
     */
    public ArgumentException(String argumentName, String otherArgumentName, String msg) {
        this(null, argumentName, otherArgumentName, msg);
    }


    /**
     * Creates a new argument exception.
     *
     * @param cause        The cause of exception. (or <code>null</code> if unknown)
     * @param msg          a description of the problem.
     */
    public ArgumentException(Throwable cause, String msg) {
        this(cause,null,null,msg);
    }

    /**
     * Creates a new argument exception.
     *
     * @param cause        The cause of the exception. (or <code>null</code> if unknown)
     * @param argumentName The argument this exception is related to.  (or <code>null</code> if unknown)
     * @param msg          a description of the problem.
     */
    public ArgumentException(Throwable cause, String argumentName, String msg) {
        this(cause,argumentName,null,msg);
    }

    /**
     * Creates a new argument exception.
     *
     * @param cause        The cause of the exception. (or <code>null</code> if unknown)
     * @param argumentName The argument this exception is related to.
     * @param otherArgumentName The name of the conflicting argument.
     * @param msg          a description of the problem.
     */
    public ArgumentException(Throwable cause, String argumentName, String otherArgumentName, String msg) {
        super(cause);

        this.argumentName = argumentName;
        this.otherArgumentName = otherArgumentName;
        this.msg = msg;
    }

    /** @return Returns the msg. */
    @Deprecated
    public String getMsg() {
        return msg;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if ((argumentName != null) && !argumentName.isEmpty()) {
            sb.append("Argument: ");
            sb.append(argumentName);
            sb.append(", ");
        }
        if ((otherArgumentName != null) && !otherArgumentName.isEmpty()) {
            sb.append("Other argument: ");
            sb.append(otherArgumentName);
            sb.append(", ");
        }
        sb.append(msg);
        return sb.toString();
    }

    /**
     * Retrieves the name of the offending argument
     *
     * @return the name of the offending argument
     */
    public String getArgument() {
        return otherArgumentName;
    }

    /**
     * Returns a string representation of this object
     *
     * @return the string representation of the object.
     */
    public String toString() {
        if (argumentName != null) {
            if (otherArgumentName != null) {
                return "Argument Exception argument:'" + argumentName + "' other argument:'" + otherArgumentName + "' - " + msg + '\n'
                        + super.toString();
            } else {
                return "Argument Exception argument:'" + argumentName + "' - " + msg + '\n' + super.toString();
            }
        } else {
            return "Argument Exception - " + msg + '\n' + super.toString();
        }

    }
}
