/*
 * Copyright 1999-2002 Carnegie Mellon University.  
 * Portions Copyright 2002-2004 Sun Microsystems, Inc.
 * Portions Copyright 2002 Mitsubishi Electric Research Laboratories.
 * Copyright (c) 2004-2020, Oracle and/or its affiliates.
 *
 * Licensed under the 2-clause BSD license.
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

/**
 * An interface implemented by command functions typically
 * added to a command interpreter. You should generally not use this
 * directly. Rather, tag any method that takes a CommandInterpreter as its
 * first argument and returns a String with the @Command annotation. Put those
 * methods in a class that implements CommandGroup and add them to a
 * CommandInterpreter to expose them to the shell.
 *
 * @see CommandInterpreter
 */
public interface CommandInterface {

    /**
     * Execute the given command.
     *  
     * @param ci    the command interpreter that invoked this command.
     * @param args  command line arguments (just like main).
     * @return      a command result
     * @throws Exception Can throw an exception.
     */
    public String execute(CommandInterpreter ci, String[] args) throws Exception;

    /**
     * Returns a one line description of the command
     *
     * @return a one-liner help message
     */
    public String getHelp();
}
