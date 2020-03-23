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

package com.oracle.labs.mlrg.olcut.command;

import org.jline.reader.Completer;

/**
 * A CommandInterface for commands that can provide tab-completion for their
 * arguments.  A Completor takes a partial string as its input and returns
 * all possibly completions starting with that string.  Several <a
 * href="http://jline.sourceforge.net/apidocs/index.html">flexible Completors
 * </a> are provided by the <a href="http://jline.sourceforge.net">JLine</a>
 * library.  Additionally, this package provides {@link CommandCompleter} that
 * will complete with the name of a defined command.  The order of Completors
 * in the array returned from {@link #getCompleters} matches the arguments
 * provided to this command.  The last Completor in the array will be used
 * for any arguments that extend beyond the length of the array.
 */
public interface CompleterCommandInterface extends CommandInterface {
    /**
     * Gets an array of Completors for the arguments to a command.  Each
     * component of the array is used with the corresponding argument to the
     * command.  The first argument is completed with the first element of the
     * array, and so on.  The last element is used to complete all arguments
     * beyond the length of the array.  Unless the same kind of completion
     * is desired on all arguments, a NullCompletor should be included as the
     * last element of the array.
     * 
     * @return an array of Completors, one for each expected argument to this
     *         command
     */
    public Completer[] getCompleters();
}
