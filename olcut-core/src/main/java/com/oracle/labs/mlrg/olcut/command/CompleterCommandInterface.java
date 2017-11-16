package com.oracle.labs.mlrg.olcut.command;

import jline.console.completer.Completer;

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
 * 
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
